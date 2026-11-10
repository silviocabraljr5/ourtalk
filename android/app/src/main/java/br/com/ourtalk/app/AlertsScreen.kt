package br.com.ourtalk.app

import android.content.Intent
import android.net.Uri
import android.util.AtomicFile
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

internal data class CityAlert(val id: Long, val title: String, val severity: String, val start: Instant, val end: Instant)
internal data class CityAlertSnapshot(val fetchedAt: Instant, val alerts: List<CityAlert>)
private val alertTime = DateTimeFormatter.ofPattern("dd/MM 'às' HH:mm", Locale.forLanguageTag("pt-BR"))
    .withZone(ZoneId.of("America/Sao_Paulo"))

internal fun parseCityAlerts(bytes: ByteArray): CityAlertSnapshot {
    require(bytes.size in 1..16_384)
    val root = JSONObject(bytes.toString(Charsets.UTF_8))
    require(root.getInt("version") == 1 && root.getString("source") == "INMET" &&
        root.getString("municipality") == "3543402")
    val fetched = Instant.parse(root.getString("fetchedAt"))
    val alerts = root.getJSONArray("alerts")
    require(alerts.length() <= 40)
    val ids = mutableSetOf<Long>()
    return CityAlertSnapshot(fetched, List(alerts.length()) { i ->
        val row = alerts.getJSONObject(i)
        val id = row.getLong("id")
        val title = row.getString("title").trim()
        val severity = row.getString("severity").trim()
        val start = Instant.parse(row.getString("startsAt"))
        val end = Instant.parse(row.getString("endsAt"))
        require(id > 0 && ids.add(id) && title.length in 1..140 && severity.length in 1..80 && end > start)
        CityAlert(id, title, severity, start, end)
    })
}

@Composable
internal fun AlertsScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val cache = remember(context) { AtomicFile(File(context.filesDir, "ribeirao-alerts-v1.json")) }
    var snapshot by remember { mutableStateOf<CityAlertSnapshot?>(null) }
    var busy by remember { mutableStateOf(true) }
    var feedback by remember { mutableStateOf("") }
    var now by remember { mutableStateOf(Instant.now()) }
    LaunchedEffect(cache) {
        snapshot = withContext(Dispatchers.IO) { runCatching { parseCityAlerts(cache.readFully()) }.getOrNull() }
        busy = false
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            now = Instant.now()
        }
    }
    ScreenTitle("Comunicados")
    Text("Avisos meteorológicos do INMET · Ribeirão Preto")
    val feed = snapshot
    if (feed == null && !busy) {
        Text("Nenhuma consulta salva neste aparelho.")
    } else if (feed != null) {
        Text("Fonte consultada em ${alertTime.format(feed.fetchedAt)}.")
        val current = now >= feed.fetchedAt.minusSeconds(300) && now < feed.fetchedAt.plusSeconds(2700)
        if (!current) {
            InfoCard("Consulta desatualizada", "Conecte-se e atualize. O app não sabe se há avisos vigentes agora.")
        } else {
            val remaining = feed.alerts.filter { it.end > now }
            if (remaining.isEmpty()) Text("Nenhum aviso para o município na última consulta.")
            remaining.forEach { alert ->
                val label = if (alert.start > now) "Previsto" else "Vigente na última consulta"
                InfoCard(alert.title, "${alert.severity} · $label\n" +
                    "De ${alertTime.format(alert.start)} até ${alertTime.format(alert.end)}")
                TextButton(onClick = {
                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://avisos.inmet.gov.br/${alert.id}"))) }
                }) { Text("Ver o comunicado original") }
            }
        }
    }
    Button(enabled = !busy, modifier = Modifier.fillMaxWidth(), onClick = {
        busy = true
        feedback = ""
        scope.launch {
            try {
                snapshot = withContext(Dispatchers.IO) {
                    downloadCitySnapshot("alerts-ribeirao.json", cache, ::parseCityAlerts, CityAlertSnapshot::fetchedAt)
                }
                now = Instant.now()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                feedback = "Não foi possível consultar a fonte. Confira a conexão ou tente novamente."
            } finally {
                busy = false
            }
        }
    }) { Text(if (busy) "Carregando…" else "Atualizar avisos") }
    if (feedback.isNotEmpty()) Text(feedback)
    TextButton(onClick = {
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://avisos.inmet.gov.br/"))) }
    }) { Text("Abrir avisos e instruções oficiais do INMET") }
    Text("Esta tela não envia notificações e não substitui os alertas oficiais.")
}
