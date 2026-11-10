package br.com.ourtalk.app

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import br.com.ourtalk.core.WalkGraph
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.Normalizer
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos

private data class MapBounds(val south: Double, val west: Double, val north: Double, val east: Double) {
    val heightMeters = ((north - south) * 111_195).toFloat()
    val aspectRatio = ((east - west) * cos(Math.toRadians((north + south) / 2)) / (north - south)).toFloat()

    fun project(lat: Double, lon: Double) =
        Offset(((lon - west) / (east - west)).toFloat(), ((north - lat) / (north - south)).toFloat())
}

private fun Offset.insideMap() = x in 0f..1f && y in 0f..1f

private data class HealthPlace(
    val id: String,
    val name: String,
    val category: String,
    val address: String,
    val municipal: Boolean,
    val point: Offset?,
    val coordinates: WalkGraph.Point?
)

private data class HealthMap(
    val bounds: MapBounds,
    val snapshot: String,
    val outlines: List<List<Offset>>,
    val places: List<HealthPlace>
)

private fun loadHealthMap(context: Context): HealthMap {
    val json = context.assets.open("ribeirao-saude.json").bufferedReader(Charsets.UTF_8)
        .use { JSONObject(it.readText()) }
    require(json.getInt("version") == 1)
    val boundsData = json.getJSONArray("bounds")
    val bounds = MapBounds(boundsData.getDouble(0), boundsData.getDouble(1),
        boundsData.getDouble(2), boundsData.getDouble(3))
    val outlines = json.getJSONArray("outlines")
    val places = json.getJSONArray("places")
    return HealthMap(
        bounds,
        json.getString("snapshot"),
        List(outlines.length()) { index ->
            val ring = outlines.getJSONArray(index)
            List(ring.length()) { point ->
                val pair = ring.getJSONArray(point)
                bounds.project(pair.getDouble(1), pair.getDouble(0))
            }
        },
        List(places.length()) { index ->
            val place = places.getJSONObject(index)
            val coordinates = if (place.has("lat") && place.has("lon")) {
                WalkGraph.Point(place.getDouble("lat"), place.getDouble("lon"))
            } else null
            val point = coordinates?.let { bounds.project(it.latitude(), it.longitude()) }?.takeIf { it.insideMap() }
            HealthPlace(place.getString("id"), place.getString("name"), place.getString("category"),
                place.getString("address"), place.getBoolean("municipal"), point, coordinates)
        }
    )
}

private val accents = Regex("\\p{M}+")
private fun searchable(text: String) =
    Normalizer.normalize(text, Normalizer.Form.NFD).replace(accents, "").lowercase()

@Composable
internal fun OfflineCityMap(position: PositionFix?, recent: Boolean) {
    val context = LocalContext.current
    var dialUnavailable by remember { mutableStateOf(false) }
    ScreenTitle("Hospitais e pronto atendimento")
    Text("Ribeirão Preto · mapa municipal offline")
    OutlinedButton(onClick = {
        try {
            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:192")))
        } catch (_: ActivityNotFoundException) {
            dialUnavailable = true
        }
    }) { Text("Abrir discador para SAMU 192") }
    if (dialUnavailable) Text("Discador indisponível neste aparelho. O número do SAMU é 192.")
    val city = remember(context) { runCatching { loadHealthMap(context) }.getOrNull() }
    if (city == null) {
        Text("Não foi possível abrir o cadastro offline de saúde.")
        return
    }
    var query by remember { mutableStateOf("") }
    var municipalOnly by remember { mutableStateOf(false) }
    var visibleCount by remember { mutableIntStateOf(6) }
    var selected by remember { mutableStateOf<HealthPlace?>(null) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var center by remember { mutableStateOf(Offset(0.5f, 0.5f)) }
    var graph by remember { mutableStateOf<WalkGraph?>(null) }
    var graphLoaded by remember { mutableStateOf(false) }
    var route by remember { mutableStateOf<WalkGraph.Route?>(null) }
    var routeStatus by remember { mutableStateOf("") }
    var request by remember { mutableIntStateOf(0) }
    val currentPosition by rememberUpdatedState(position)
    LaunchedEffect(context) {
        graph = withContext(Dispatchers.IO) {
            runCatching { context.assets.open("ribeirao-caminhada.odbl").buffered().use(::WalkGraph) }.getOrNull()
        }
        graphLoaded = true
    }
    val matches = remember(query, municipalOnly, city) {
        val term = searchable(query.trim())
        city.places.filter {
            (!municipalOnly || it.municipal) &&
                searchable("${it.name} ${it.category} ${it.address} ${it.id}").contains(term)
        }
    }
    val gps = position?.let { city.bounds.project(it.latitude, it.longitude) }
    fun bounded(point: Offset, scale: Float): Offset {
        val margin = 0.5f / scale
        return Offset(point.x.coerceIn(margin, 1f - margin), point.y.coerceIn(margin, 1f - margin))
    }
    fun select(place: HealthPlace) {
        selected = place
        route = null
        routeStatus = ""
        place.point?.let {
            zoom = maxOf(zoom, 6f)
            center = bounded(it, zoom)
        }
    }
    LaunchedEffect(request) {
        if (request == 0) return@LaunchedEffect
        val place = selected ?: return@LaunchedEffect
        val destination = place.coordinates ?: return@LaunchedEffect
        val fix = position ?: return@LaunchedEffect
        val network = graph ?: return@LaunchedEffect
        if (!recent || !fix.canShareAt(System.currentTimeMillis()) || fix.accuracy > 75f) {
            route = null
            routeStatus = "Consulte uma posição GPS recente e com melhor precisão."
            return@LaunchedEffect
        }
        route = null
        routeStatus = "Calculando no aparelho…"
        val origin = WalkGraph.Point(fix.latitude, fix.longitude)
        val result = withContext(Dispatchers.Default) { network.route(origin, destination) }
        if (selected != place || currentPosition != fix) return@LaunchedEffect
        route = result
        routeStatus = if (result == null) "Não há um caminho conectado na malha disponível." else ""
        if (result != null) {
            val path = result.points().map { city.bounds.project(it.latitude(), it.longitude()) }
            val minX = path.minOf { it.x }
            val maxX = path.maxOf { it.x }
            val minY = path.minOf { it.y }
            val maxY = path.maxOf { it.y }
            zoom = (0.75f / maxOf(maxX - minX, maxY - minY, 0.02f)).coerceIn(1f, 18f)
            center = bounded(Offset((minX + maxX) / 2, (minY + maxY) / 2), zoom)
        }
    }
    val routeLine = remember(route) {
        route?.points()?.map { city.bounds.project(it.latitude(), it.longitude()) }.orEmpty()
    }
    Canvas(
        Modifier.fillMaxWidth().aspectRatio(city.bounds.aspectRatio)
            .semantics { contentDescription = "Mapa municipal de saúde de Ribeirão Preto. As unidades também estão na lista abaixo." }
            .then(if (zoom == 1f) Modifier else Modifier.pointerInput(zoom) {
                detectDragGestures { change, delta ->
                    change.consume()
                    center = bounded(center - Offset(delta.x / size.width, delta.y / size.height) / zoom, zoom)
                }
            })
            .pointerInput(matches, zoom) {
                detectTapGestures { tap ->
                    val point = center + Offset(tap.x / size.width - 0.5f, tap.y / size.height - 0.5f) / zoom
                    val radius = 20.dp.toPx()
                    matches.filter { it.point != null }.minByOrNull {
                        val candidate = requireNotNull(it.point)
                        abs(candidate.x - point.x) * size.width + abs(candidate.y - point.y) * size.height
                    }?.takeIf {
                        val candidate = requireNotNull(it.point)
                        abs(candidate.x - point.x) * size.width * zoom < radius &&
                            abs(candidate.y - point.y) * size.height * zoom < radius
                    }?.let(::select)
                }
            }
    ) {
        fun pixel(point: Offset) = Offset(
            ((point.x - center.x) * zoom + 0.5f) * size.width,
            ((point.y - center.y) * zoom + 0.5f) * size.height
        )
        drawRect(Color(0xFF101A1F))
        clipRect {
            city.outlines.forEach { ring ->
                val path = Path()
                ring.forEachIndexed { index, vertex ->
                    val point = pixel(vertex)
                    if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
                }
                path.close()
                drawPath(path, Color(0xFF223537))
                drawPath(path, Color(0xFF9CAFAA), style = Stroke(1.5.dp.toPx()))
            }
            if (routeLine.size > 1) {
                val path = Path()
                routeLine.forEachIndexed { index, vertex ->
                    val point = pixel(vertex)
                    if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
                }
                drawPath(path, Color(0xFF64BEFF), style = Stroke(3.dp.toPx()))
            }
            matches.forEach { place ->
                place.point?.let {
                    val color = when {
                        place == selected -> Color.White
                        place.municipal -> Color(0xFFFFB46C)
                        else -> TalkLime
                    }
                    drawCircle(color, if (place == selected) 6.dp.toPx() else 4.dp.toPx(), pixel(it))
                }
            }
            if (gps != null && gps.insideMap()) {
                val marker = pixel(gps)
                val color = if (recent) Color(0xFF64BEFF) else Color.LightGray
                val radius = position.accuracy.coerceIn(0f, city.bounds.heightMeters) /
                    city.bounds.heightMeters * size.height * zoom
                if (radius > 0) drawCircle(color.copy(alpha = 0.2f), radius, marker)
                drawCircle(color, 5.dp.toPx(), marker)
            }
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = {
            zoom = 1f
            center = Offset(0.5f, 0.5f)
        }, enabled = zoom > 1f) { Text("Ver município") }
        if (gps?.insideMap() == true) {
            TextButton(onClick = {
                zoom = maxOf(zoom, 6f)
                center = bounded(gps, zoom)
            }) { Text(if (recent) "Minha posição" else "Última posição") }
        }
    }
    if (gps != null && !gps.insideMap()) Text("A leitura do GPS está fora da área exibida.")
    if (gps?.insideMap() == true) {
        Text(if (recent) "Azul · leitura do GPS" else "Cinza · última leitura, sem confirmação atual")
    }
    Text("Laranja · pronto atendimento municipal. Verde · demais unidades cadastradas.",
        style = MaterialTheme.typography.bodySmall)
    Text("Contorno: IBGE. Unidades: CNES ${city.snapshot} e Prefeitura. Ponto do HC UE: © OpenStreetMap contributors (ODbL).",
        style = MaterialTheme.typography.bodySmall)
    Text("O mapa mostra a localização cadastrada. Para saber se a unidade recebe seu caso, confirme com o serviço.",
        style = MaterialTheme.typography.bodySmall)
    TextButton(onClick = {
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.openstreetmap.org/copyright"))) }
    }) { Text("Malha de caminhada: © OpenStreetMap contributors (ODbL)") }
    FilterChip(selected = municipalOnly, onClick = {
        municipalOnly = !municipalOnly
        selected = null
        route = null
        routeStatus = ""
        visibleCount = 6
    }, label = { Text("Só pronto atendimento municipal") })
    OutlinedTextField(
        value = query, onValueChange = {
            query = it
            selected = null
            route = null
            routeStatus = ""
            visibleCount = 6
        },
        label = { Text("Buscar unidade ou endereço") }, singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    selected?.let { place ->
        val details = listOfNotNull(place.category, place.address.takeIf { it.isNotBlank() },
            if (place.municipal) "24 horas segundo a Prefeitura" else null,
            if (place.point == null) "Localização no mapa indisponível" else null,
            if (place.id.all { it.isDigit() }) "CNES ${place.id}" else null)
        InfoCard(place.name, details.joinToString("\n"))
        val network = graph
        val origin = position?.let { WalkGraph.Point(it.latitude, it.longitude) }
        val destination = place.coordinates
        when {
            !graphLoaded -> Text("Carregando a malha de caminhada…")
            network == null -> Text("Malha de caminhada indisponível.")
            destination == null || !network.covers(destination) ->
                Text("Esta unidade está fora da cobertura de rotas a pé.")
            origin == null || !recent || position.mock || position.accuracy > 75f ->
                Text("Consulte sua posição atual no GPS para calcular uma rota a pé.")
            !network.covers(origin) -> Text("Sua posição está fora da cobertura de rotas a pé.")
            else -> OutlinedButton(onClick = { request++ }) { Text("Traçar rota a pé sem internet") }
        }
        if (routeStatus.isNotEmpty()) Text(routeStatus)
        route?.let {
            val km = String.format(Locale.forLanguageTag("pt-BR"), "%.2f", it.meters() / 1000)
            InfoCard("Rota estimada a pé · $km km",
                "A linha azul mostra o caminho cadastrado. A posição inicial e a unidade ficam a cerca de " +
                    "${it.startGap().toInt()} m e ${it.endGap().toInt()} m da malha. " +
                    "Confira calçadas, travessias e a entrada no local. A malha não informa interdições em tempo real.")
        }
    }
    Text("${matches.size} unidades encontradas")
    matches.take(visibleCount).forEach { place ->
        TextButton(onClick = { select(place) }, modifier = Modifier.fillMaxWidth()) {
            Text("${place.name} · ${place.category}")
        }
    }
    if (matches.size > visibleCount) {
        TextButton(onClick = { visibleCount += 6 }) { Text("Mostrar mais unidades") }
    }
}
