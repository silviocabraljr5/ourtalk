package br.com.ourtalk.app

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ceil

private const val MAX_FIX_AGE_MS = 120_000L

internal fun PositionFix.canShareAt(now: Long): Boolean =
    !mock && latitude.isFinite() && latitude in -90.0..90.0 &&
        longitude.isFinite() && longitude in -180.0..180.0 &&
        accuracy.isFinite() && accuracy > 0f && time in (now - MAX_FIX_AGE_MS)..now

internal fun helpRequestText(message: String, position: PositionFix?, now: Long): String {
    val text = "Preciso de ajuda.\n${message.trim()}"
    if (position == null || !position.canShareAt(now)) return text
    val coordinates = String.format(Locale.US, "%.6f, %.6f", position.latitude, position.longitude)
    val readAt = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM).format(Date(position.time))
    return "$text\n\nMinha localização no GPS: $coordinates\nPrecisão estimada: ${ceil(position.accuracy).toInt()} m\nLeitura: $readAt"
}

@Composable
internal fun HelpRequestScreen() {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val reader = remember(context) { GpsReader(context) }
    var message by remember { mutableStateOf("") }
    var position by remember { mutableStateOf<PositionFix?>(null) }
    var reading by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf("") }

    fun requestPosition() {
        position = null
        reading = true
        feedback = "Buscando posição por satélite…"
        reader.request { fix, error ->
            reading = false
            position = fix?.takeIf { it.canShareAt(System.currentTimeMillis()) }
            feedback = when {
                position != null -> ""
                error != null -> error
                else -> "A leitura recebida não pode ser anexada. Tente novamente ou informe uma referência no texto."
            }
        }
    }

    val permissions = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (result[Manifest.permission.ACCESS_FINE_LOCATION] == true) requestPosition()
        else feedback = "Sem localização precisa. Você pode informar seu endereço ou um ponto de referência no texto."
    }
    DisposableEffect(lifecycle, reader) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                reader.cancel()
                if (reading) feedback = "Busca interrompida. Você pode tentar novamente."
                reading = false
                position = null
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            reader.cancel()
            lifecycle.removeObserver(observer)
        }
    }
    LaunchedEffect(position) {
        val fix = position ?: return@LaunchedEffect
        delay((fix.time + MAX_FIX_AGE_MS - System.currentTimeMillis() + 1).coerceAtLeast(0))
        position = null
        feedback = "A posição venceu. Faça uma nova leitura para anexá-la."
    }

    ScreenTitle("Pedir ajuda")
    InfoCard("Emergência de saúde no Brasil", "Para atendimento de urgência, abra o discador e ligue para o SAMU 192.")
    OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = {
        try {
            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:192")))
        } catch (_: ActivityNotFoundException) {
            feedback = "Discador indisponível neste aparelho. O número do SAMU é 192."
        }
    }) { Text("Abrir discador para SAMU 192") }

    Text("Avise alguém de confiança", style = MaterialTheme.typography.titleLarge)
    Text("Escreva o que aconteceu, diga onde você está e escolha por onde enviar.")
    OutlinedTextField(
        value = message,
        onValueChange = { if (it.length <= 1000) message = it },
        label = { Text("O que aconteceu?") },
        placeholder = { Text("Inclua um endereço ou ponto de referência") },
        modifier = Modifier.fillMaxWidth()
    )
    if (position == null) {
        OutlinedButton(modifier = Modifier.fillMaxWidth(), enabled = !reading, onClick = {
            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                requestPosition()
            } else permissions.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }) { Text("Anexar minha posição atual") }
    } else {
        Text("Posição GPS anexada à mensagem.")
        TextButton(onClick = { position = null }) { Text("Remover posição") }
    }
    if (feedback.isNotEmpty()) Text(feedback, style = MaterialTheme.typography.bodySmall)
    Button(enabled = message.isNotBlank(), modifier = Modifier.fillMaxWidth(), onClick = {
        val fix = position
        val now = System.currentTimeMillis()
        if (fix != null && !fix.canShareAt(now)) {
            position = null
            feedback = "A posição venceu. Confira o texto ou faça outra leitura antes de compartilhar."
        } else try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, helpRequestText(message, fix, now))
            }
            context.startActivity(Intent.createChooser(intent, "Compartilhar pedido de ajuda"))
        } catch (_: ActivityNotFoundException) {
            feedback = "Nenhum app disponível para compartilhar o pedido."
        }
    }) { Text("Escolher como compartilhar") }
    Text("O envio e a confirmação de entrega dependem do app escolhido.", style = MaterialTheme.typography.bodySmall)
}
