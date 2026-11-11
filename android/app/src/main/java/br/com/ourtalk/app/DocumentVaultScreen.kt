package br.com.ourtalk.app

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

@Composable
internal fun DocumentVaultScreen() {
    val context = LocalContext.current
    val vault = remember(context) { DocumentVault(context) }
    val scope = rememberCoroutineScope()
    var documents by remember { mutableStateOf<List<VaultDocument>>(emptyList()) }
    var busy by remember { mutableStateOf(true) }
    var status by remember { mutableStateOf("") }
    var deleting by remember { mutableStateOf<VaultDocument?>(null) }
    var exportId by rememberSaveable { mutableStateOf<String?>(null) }

    fun perform(success: String, failure: String, action: () -> Unit) {
        busy = true
        status = ""
        scope.launch {
            try {
                documents = withContext(Dispatchers.IO) {
                    action()
                    vault.documents()
                }
                status = success
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                status = failure
            } finally {
                busy = false
            }
        }
    }

    LaunchedEffect(vault) {
        documents = withContext(Dispatchers.IO) { vault.documents() }
        busy = false
    }
    val importPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) perform("Documento guardado.", "Não foi possível importar. Confira o formato e o limite de 5 MB.") {
            vault.importDocument(uri)
        }
    }
    val exportPicker = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val id = exportId?.let(UUID::fromString)
        exportId = null
        val uri = result.data?.data
        if (result.resultCode == Activity.RESULT_OK && uri != null && id != null) {
            perform("Cópia exportada.", "Não foi possível exportar a cópia. Confira o destino escolhido.") {
                vault.export(id, uri)
            }
        }
    }

    ScreenTitle("Meu cofre")
    Text("Guarde até 10 documentos PDF, JPEG ou PNG, com até 5 MB cada. As cópias do cofre ficam criptografadas neste aparelho.")
    Button(enabled = !busy && documents.size < 10, modifier = Modifier.fillMaxWidth(), onClick = {
        runCatching { importPicker.launch(arrayOf("application/pdf", "image/jpeg", "image/png")) }
            .onFailure { status = "Seletor de arquivos indisponível." }
    }) { Text("Importar documento") }
    if (!busy && documents.isEmpty()) Text("Nenhum documento no cofre.")
    if (busy) Text("Aguarde…")
    documents.forEach { document ->
        OutlinedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(document.title, style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (document.mime != null) TextButton(enabled = !busy, onClick = {
                        exportId = document.id.toString()
                        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                            type = document.mime
                            addCategory(Intent.CATEGORY_OPENABLE)
                            putExtra(Intent.EXTRA_TITLE, document.title)
                        }
                        runCatching { exportPicker.launch(intent) }.onFailure {
                            exportId = null
                            status = "Seletor de destino indisponível."
                        }
                    }) { Text("Exportar cópia") }
                    TextButton(enabled = !busy, onClick = { deleting = document }) { Text("Apagar") }
                }
            }
        }
    }
    if (documents.any { it.mime != null }) {
        Text("Ao exportar, o arquivo é gravado sem a proteção do cofre no destino que você escolher.",
            style = MaterialTheme.typography.bodySmall)
    }
    if (status.isNotEmpty()) Text(status)
    deleting?.let { document ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Apagar documento do cofre?") },
            text = { Text(document.title) },
            confirmButton = {
                TextButton(onClick = {
                    deleting = null
                    perform("Documento apagado.", "Não foi possível apagar o documento.") { vault.delete(document.id) }
                }) { Text("Apagar") }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancelar") } }
        )
    }
}
