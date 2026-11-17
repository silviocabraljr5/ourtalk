package br.com.ourtalk.app

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Composable
internal fun ContactInviteFlow(
    repository: LocalRepository,
    showCreate: Boolean,
    closeCreate: () -> Unit,
    deleting: Boolean,
    closeDelete: () -> Unit,
    inContacts: Boolean,
    onSync: () -> Unit
) {
    val context = LocalContext.current
    val service = remember { ContactInvites(context) }
    val scope = rememberCoroutineScope()
    val storageLock = remember { Mutex() }
    var pending by remember { mutableStateOf(service.pending) }
    var name by remember { mutableStateOf(service.name) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var receiving by remember { mutableStateOf(false) }
    var pasted by remember { mutableStateOf("") }

    suspend fun sync() {
        try {
            storageLock.withLock {
                if (service.hasIdentity) {
                    repository.syncInvitedContacts(service.contacts())
                    onSync()
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            // The cached contact list remains available while offline.
        }
    }

    LaunchedEffect(Unit) { pending = service.readInstallInvite() }
    LaunchedEffect(inContacts) { if (inContacts) sync() }

    LaunchedEffect(deleting) { if (deleting) error = "" }
    if (deleting) {
        AlertDialog(
            onDismissRequest = { if (!busy) closeDelete() },
            title = { Text("Excluir dados dos convites?") },
            text = {
                Column {
                    Text("Remove os contatos sincronizados deste aparelho e do servidor e cancela os convites pendentes. A outra pessoa verá a remoção ao sincronizar. Conversas e contatos salvos manualmente ficam no aparelho.")
                    if (error.isNotEmpty()) Text(error)
                }
            },
            confirmButton = {
                TextButton(enabled = !busy, onClick = {
                    busy = true
                    error = ""
                    scope.launch {
                        try {
                            storageLock.withLock {
                                service.deleteData { repository.syncInvitedContacts(emptyList()) }
                            }
                            name = ""
                            pending = ""
                            closeDelete()
                            onSync()
                        } catch (cancelled: CancellationException) {
                            throw cancelled
                        } catch (_: Exception) {
                            error = "Não foi possível excluir agora. Confira sua conexão e tente novamente."
                        } finally {
                            busy = false
                        }
                    }
                }) { Text(if (busy) "Excluindo…" else "Excluir dados") }
            },
            dismissButton = { TextButton(onClick = closeDelete, enabled = !busy) { Text("Cancelar") } }
        )
        return
    }
    if (!showCreate && pending.isEmpty()) return
    val redeeming = pending.isNotEmpty()
    fun close() {
        if (busy) return
        error = ""
        receiving = false
        pasted = ""
        if (redeeming) {
            service.discard()
            pending = ""
        }
        closeCreate()
    }
    AlertDialog(
        onDismissRequest = ::close,
        title = { Text(if (redeeming) "Você recebeu um convite" else if (receiving) "Usar convite recebido" else "Convidar uma pessoa") },
        text = {
            Column {
                Text(if (redeeming)
                    "Informe o nome que a outra pessoa verá. Ao continuar, vocês entram na lista de contatos um do outro."
                else if (receiving)
                    "Cole o link pessoal ou código que recebeu. Depois você confirma seu nome para entrar nos contatos da outra pessoa."
                else
                    "Escolha seu nome e compartilhe o link pessoal. Quem já tem o app pode abrir Convidar pessoa e escolher Usar convite recebido.")
                if (!redeeming) {
                    TextButton(enabled = !busy, onClick = { receiving = !receiving; error = "" }) {
                        Text(if (receiving) "Quero enviar um convite" else "Usar convite recebido")
                    }
                }
                if (receiving && !redeeming) OutlinedTextField(pasted, { if (it.length <= 2048) pasted = it },
                    label = { Text("Link ou código do convite") }, enabled = !busy)
                OutlinedTextField(name, { if (it.length <= 60) name = it },
                    label = { Text("Seu nome") }, singleLine = true, enabled = !busy)
                if (error.isNotEmpty()) Text(error)
            }
        },
        confirmButton = {
            TextButton(enabled = !busy && name.isNotBlank() && (!receiving || redeeming || pasted.isNotBlank()), onClick = {
                if (receiving && !redeeming) {
                    val token = runCatching { service.receive(pasted) }.getOrNull()
                    if (token == null) error = "Convite inválido. Cole o link completo do OurTalk ou o código recebido."
                    else { pending = token; error = "" }
                    return@TextButton
                }
                busy = true
                error = ""
                scope.launch {
                    try {
                        if (redeeming) {
                            service.redeem(name.trim(), pending)
                            pending = ""
                            receiving = false
                            pasted = ""
                            closeCreate()
                            sync()
                        } else {
                            val link = service.create(name.trim())
                            context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Vamos nos conectar no OurTalk! Instale pelo meu convite: $link\n\nJá tem o app? Em Conversas, toque em Convidar pessoa → Usar convite recebido e cole o link acima.")
                            }, "Compartilhar convite"))
                            closeCreate()
                        }
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: ContactInvites.UnavailableInvite) {
                        error = "Este convite expirou ou já foi usado."
                    } catch (_: ContactInvites.ServiceNotReady) {
                        error = "O serviço de convites está temporariamente indisponível. Seu contato não foi adicionado. Tente novamente mais tarde."
                    } catch (_: Exception) {
                        error = "Não foi possível concluir. Confira sua conexão e tente novamente."
                    } finally {
                        busy = false
                    }
                }
            }) { Text(if (busy) "Aguarde…" else if (redeeming) "Adicionar contato" else if (receiving) "Conferir convite" else "Compartilhar convite") }
        },
        dismissButton = { TextButton(onClick = ::close, enabled = !busy) { Text("Cancelar") } }
    )
}
