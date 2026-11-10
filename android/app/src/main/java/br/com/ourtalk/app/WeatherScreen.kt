package br.com.ourtalk.app

import android.content.Intent
import android.net.Uri
import android.util.AtomicFile
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import java.net.URL
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.net.ssl.HttpsURLConnection
import kotlin.math.roundToInt

internal data class WeatherDay(val date: LocalDate, val low: Double, val high: Double, val rain: Double, val code: Int)
internal data class WeatherSnapshot(val fetchedAt: Instant, val days: List<WeatherDay>)
private val cityZone = ZoneId.of("America/Sao_Paulo")
private val weatherLocale = Locale.forLanguageTag("pt-BR")
private const val MAX_FORECAST_BYTES = 16_384

internal fun parseWeather(data: ByteArray): WeatherSnapshot {
    require(data.size in 1..MAX_FORECAST_BYTES)
    val json = JSONObject(data.toString(Charsets.UTF_8))
    require(json.getInt("version") == 1 && json.getString("timezone") == cityZone.id)
    val timestamp = Instant.parse(json.getString("fetchedAt"))
    val entries = json.getJSONArray("days")
    require(entries.length() == 3)
    val start = timestamp.atZone(cityZone).toLocalDate()
    val days = List(entries.length()) { i ->
        val day = entries.getJSONObject(i)
        val date = LocalDate.parse(day.getString("date"))
        val low = day.getDouble("low")
        val high = day.getDouble("high")
        val rain = day.getDouble("rain")
        val code = day.getInt("code")
        require(date == start.plusDays(i.toLong()) && low in -60.0..65.0 &&
            high in low..65.0 && rain in 0.0..100.0 && code in 0..99)
        WeatherDay(date, low, high, rain, code)
    }
    return WeatherSnapshot(timestamp, days)
}

private fun condition(code: Int) = when (code) {
    0 -> "Céu limpo"
    1, 2 -> "Parcialmente nublado"
    3 -> "Encoberto"
    45, 48 -> "Neblina"
    in 51..57 -> "Garoa"
    in 61..67 -> "Chuva"
    in 71..77, 85, 86 -> "Neve"
    in 80..82 -> "Pancadas de chuva"
    95, 96, 99 -> "Trovoadas"
    else -> "Condição sem descrição"
}

internal fun <T> downloadCitySnapshot(
    filename: String, cache: AtomicFile, parse: (ByteArray) -> T, fetchedAt: (T) -> Instant
): T {
    val connection = URL("https://ourtalk.com.br/$filename").openConnection() as HttpsURLConnection
    try {
        connection.instanceFollowRedirects = false
        connection.connectTimeout = 6_000
        connection.readTimeout = 6_000
        require(connection.responseCode == 200)
        val bytes = connection.inputStream.use { input ->
            val output = java.io.ByteArrayOutputStream()
            val buffer = ByteArray(4096)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                require(output.size() + count <= MAX_FORECAST_BYTES)
                output.write(buffer, 0, count)
            }
            output.toByteArray()
        }
        val snapshot = parse(bytes)
        require(fetchedAt(snapshot) <= Instant.now().plusSeconds(300))
        val previous = runCatching { parse(cache.readFully()) }.getOrNull()
        require(previous == null || fetchedAt(snapshot) >= fetchedAt(previous))
        val output = cache.startWrite()
        try {
            output.write(bytes)
            cache.finishWrite(output)
        } catch (error: Exception) {
            cache.failWrite(output)
            throw error
        }
        return snapshot
    } finally {
        connection.disconnect()
    }
}

@Composable
internal fun WeatherScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val cache = remember(context) { AtomicFile(File(context.filesDir, "ribeirao-weather-v1.json")) }
    var snapshot by remember { mutableStateOf<WeatherSnapshot?>(null) }
    var loading by remember { mutableStateOf(true) }
    var feedback by remember { mutableStateOf("") }
    var now by remember { mutableStateOf(Instant.now()) }
    LaunchedEffect(cache) {
        snapshot = withContext(Dispatchers.IO) { runCatching { parseWeather(cache.readFully()) }.getOrNull() }
        loading = false
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            now = Instant.now()
        }
    }
    ScreenTitle("Previsão do tempo")
    Text("Ribeirão Preto · previsão para a cidade")
    snapshot?.let { forecast ->
        val today = now.atZone(cityZone).toLocalDate()
        val available = forecast.days.filter { it.date >= today }
        val stale = forecast.fetchedAt > now.plusSeconds(300) ||
            Duration.between(forecast.fetchedAt, now) >= Duration.ofHours(12)
        val formatted = DateTimeFormatter.ofPattern("dd/MM 'às' HH:mm", weatherLocale).withZone(cityZone)
        Text("Previsão salva de ${formatted.format(forecast.fetchedAt)} (horário local).")
        if (stale || available.isEmpty()) {
            InfoCard("Previsão desatualizada", "Conecte à internet e atualize para consultar uma previsão recente.")
        } else available.forEach { day ->
            val dayLabel = day.date.format(DateTimeFormatter.ofPattern("EEEE, dd/MM", weatherLocale))
            InfoCard(dayLabel, "${condition(day.code)}\n${day.low.roundToInt()} °C a ${day.high.roundToInt()} °C · chuva: ${day.rain.roundToInt()}%")
        }
    } ?: if (!loading) { Text("Nenhuma previsão salva. Atualize com internet para consultar depois sem conexão.") } else Unit
    Button(enabled = !loading, modifier = Modifier.fillMaxWidth(), onClick = {
        loading = true
        feedback = ""
        scope.launch {
            try {
                snapshot = withContext(Dispatchers.IO) {
                    downloadCitySnapshot("weather-ribeirao.json", cache, ::parseWeather, WeatherSnapshot::fetchedAt)
                }
                now = Instant.now()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                feedback = if (snapshot == null) "Não foi possível baixar a previsão. Tente novamente quando tiver conexão."
                    else "Não foi possível atualizar. A previsão anterior continua salva."
            } finally {
                loading = false
            }
        }
    }) { Text(if (loading) "Carregando…" else "Atualizar previsão") }
    if (feedback.isNotEmpty()) Text(feedback, style = MaterialTheme.typography.bodySmall)
    TextButton(onClick = {
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://open-meteo.com/"))) }
    }) { Text("Dados de previsão: Open-Meteo (CC BY 4.0)") }
    Text("Previsão regional. Consulte os canais oficiais para alertas meteorológicos.", style = MaterialTheme.typography.bodySmall)
}
