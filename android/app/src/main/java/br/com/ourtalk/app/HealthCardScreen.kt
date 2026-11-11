package br.com.ourtalk.app

import android.app.Activity
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.view.WindowManager
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext

private enum class HealthField(val label: String, val limit: Int) {
    NAME("Nome", 100),
    ALLERGIES("Alergias conhecidas", 500),
    MEDICATION("Medicamentos em uso", 500),
    NOTES("Condições de saúde e observações", 500),
    CONTACT("Contato de emergência", 100),
    PHONE("Telefone do contato", 32)
}

@Composable
internal fun HealthCardScreen() {
    val context = LocalContext.current
    val window = remember(context) {
        generateSequence(context) { (it as? ContextWrapper)?.baseContext }
            .filterIsInstance<Activity>().firstOrNull()?.window
    }
    DisposableEffect(window) {
        val alreadyProtected = (window?.attributes?.flags ?: 0) and WindowManager.LayoutParams.FLAG_SECURE != 0
        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose {
            if (!alreadyProtected) window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
    val stored = remember(context) {
        runCatching {
            val store = SecureStore(context, "ourtalk-health-card-v1")
            store to HealthField.entries.associateWith { store.getString(it.name, "").orEmpty() }
        }.getOrNull()
    }
    ScreenTitle("Minha ficha de saúde")
    if (stored == null) {
        Text("Não foi possível abrir a ficha protegida. Feche e abra o app para tentar novamente.")
        return
    }
    val (store, initial) = stored
    var saved by remember { mutableStateOf(initial) }
    var draft by remember { mutableStateOf(initial) }
    var editing by remember { mutableStateOf(false) }
    var confirmingDelete by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    val hasCard = saved.values.any(String::isNotBlank)

    Text("Disponível sem internet. Os dados ficam criptografados neste aparelho.")
    if (editing) {
        HealthField.entries.forEach { field ->
            OutlinedTextField(
                value = draft.getValue(field),
                onValueChange = { if (it.length <= field.limit) draft = draft + (field to it) },
                label = { Text(field.label) },
                singleLine = field in setOf(HealthField.NAME, HealthField.CONTACT, HealthField.PHONE),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Button(enabled = draft.values.any(String::isNotBlank), modifier = Modifier.fillMaxWidth(), onClick = {
            val next = draft.mapValues { it.value.trim() }
            runCatching {
                val editor = store.edit()
                next.forEach { (field, value) -> editor.putString(field.name, value) }
                editor.apply()
            }.onSuccess {
                saved = next
                draft = next
                editing = false
                status = "Ficha salva."
            }.onFailure { status = "Não foi possível salvar a ficha. Confira o armazenamento do aparelho." }
        }) { Text("Salvar ficha") }
        TextButton(onClick = { draft = saved; editing = false; status = "" }) { Text("Cancelar edição") }
    } else {
        if (hasCard) {
            saved.filterValues(String::isNotBlank).forEach { (field, value) -> InfoCard(field.label, value) }
            val phone = saved.getValue(HealthField.PHONE).filterNot { it in " ()-." }
            if (phone.matches(Regex("\\+?[0-9]{8,15}"))) {
                OutlinedButton(onClick = {
                    runCatching { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null))) }
                        .onFailure { status = "Discador indisponível neste aparelho." }
                }) { Text("Abrir discador para o contato") }
            }
        } else Text("Preencha apenas as informações que deseja guardar.")
        Button(modifier = Modifier.fillMaxWidth(), onClick = { draft = saved; editing = true; status = "" }) {
            Text(if (hasCard) "Editar ficha" else "Criar ficha")
        }
        if (hasCard) TextButton(onClick = { confirmingDelete = true }) { Text("Apagar ficha") }
    }
    if (status.isNotEmpty()) Text(status)
    if (confirmingDelete) AlertDialog(
        onDismissRequest = { confirmingDelete = false },
        title = { Text("Apagar a ficha de saúde?") },
        text = { Text("As informações desta ficha serão removidas deste aparelho.") },
        confirmButton = {
            TextButton(onClick = {
                runCatching { store.edit().clear().apply() }.onSuccess {
                    saved = HealthField.entries.associateWith { "" }
                    draft = saved
                    status = "Ficha apagada."
                }.onFailure { status = "Não foi possível apagar a ficha." }
                confirmingDelete = false
            }) { Text("Apagar") }
        },
        dismissButton = { TextButton(onClick = { confirmingDelete = false }) { Text("Cancelar") } }
    )
}
