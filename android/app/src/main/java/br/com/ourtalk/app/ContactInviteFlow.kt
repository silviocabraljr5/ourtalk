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

@Composable
internal fun ContactInviteFlow(
    repository: LocalRepository,
    showCreate: Boolean,
    closeCreate: () -> Unit,
    inContacts: Boolean,
    onSync: () -> Unit
) {
    val context = LocalContext.current
    val service = remember { ContactInvites(context) }
    val scope = rememberCoroutineScope()
    var pending by remember { mutableStateOf(service.pending) }
    var name by remember { mutableStateOf(service.name) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    suspend fun sync() {
        if (!service.hasIdentity) return
        try {
            repository.syncInvitedContacts(service.contacts())
            onSync()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            // The cached contact list remains available while offline.
        }
    }

    LaunchedEffect(Unit) { pending = service.readInstallInvite() }
    LaunchedEffect(inContacts) { if (inContacts) sync() }

    if (!showCreate && pending.isEmpty()) return
    val redeeming = pending.isNotEmpty()
    fun close() {
        if (busy) return
        error = ""
        if (redeeming) {
            service.discard()
            pending = ""
        } else closeCreate()
    }
    AlertDialog(
        onDismissRequest = ::close,
        title = { Text(if (redeeming) "Você recebeu um convite" else "Convidar uma pessoa") },
        text = {
            Column {
                Text(if (redeeming)
                    "Informe o nome que a outra pessoa verá. Ao continuar, vocês entram na lista de contatos um do outro."
                else
                    "Escolha seu nome e compartilhe o link pessoal. Ele adiciona a primeira pessoa que instalar pelo convite e abrir o OurTalk.")
                OutlinedTextField(name, { if (it.length <= 60) name = it },
                    label = { Text("Seu nome") }, singleLine = true, enabled = !busy)
                if (error.isNotEmpty()) Text(error)
            }
        },
        confirmButton = {
            TextButton(enabled = !busy && name.isNotBlank(), onClick = {
                busy = true
                error = ""
                scope.launch {
                    try {
                        if (redeeming) {
                            service.redeem(name.trim(), pending)
                            pending = ""
                            sync()
                        } else {
                            val link = service.create(name.trim())
                            context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Instale o OurTalk pelo meu convite para entrarmos na lista de contatos: $link")
                            }, "Compartilhar convite"))
                            closeCreate()
                        }
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: ContactInvites.UnavailableInvite) {
                        error = "Este convite expirou ou já foi usado."
                    } catch (_: Exception) {
                        error = "Não foi possível concluir. Confira sua conexão e tente novamente."
                    } finally {
                        busy = false
                    }
                }
            }) { Text(if (busy) "Aguarde…" else if (redeeming) "Continuar" else "Compartilhar convite") }
        },
        dismissButton = { TextButton(onClick = ::close, enabled = !busy) { Text("Cancelar") } }
    )
}
