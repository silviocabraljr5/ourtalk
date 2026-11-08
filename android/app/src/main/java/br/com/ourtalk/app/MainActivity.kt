package br.com.ourtalk.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Lime = Color(0xFFA9EA1C)
private val Dark = Color(0xFF0B1216)
private enum class Screen(val label: String) { HOME("Início"), CHATS("Conversas"), TALK("TalK"), MAP("Mapa"), RESOURCES("Recursos"), PREPARE("Preparar"), HELP("Pedir ajuda") }
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme(colorScheme = darkColorScheme(primary = Lime, onPrimary = Dark, background = Dark)) { OurTalk() } }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun OurTalk() {
    var route by rememberSaveable { mutableStateOf(Screen.HOME.name) }
    val screen = Screen.valueOf(route)
    val go: (Screen) -> Unit = { route = it.name }
    val tabs = listOf(Screen.HOME, Screen.CHATS, Screen.TALK, Screen.MAP)
    val icons = listOf(Icons.Outlined.Home, Icons.Outlined.ChatBubbleOutline, Icons.Outlined.AutoAwesome, Icons.Outlined.Map)
    Scaffold(topBar = { TopAppBar(title = { Text("OurTalk", color = Lime) }, actions = { TextButton(onClick = { go(Screen.RESOURCES) }) { Icon(Icons.Outlined.GridView, null); Text(" Recursos") } }) }, bottomBar = {
        NavigationBar { tabs.forEachIndexed { i, tab -> NavigationBarItem(selected = screen == tab, onClick = { go(tab) }, icon = { Icon(icons[i], tab.label) }, label = { Text(tab.label) }) } }
    }) { insets ->
        Column(Modifier.fillMaxSize().padding(insets).verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            when (screen) {
                Screen.HOME -> {
                    Text("Olá!")
                    Text("Como podemos\najudar?", fontSize = 34.sp, lineHeight = 38.sp)
                    Button(onClick = { go(Screen.CHATS) }, modifier = Modifier.fillMaxWidth().height(58.dp)) { Text("Conversar") }
                    OutlinedButton(onClick = { go(Screen.HELP) }, modifier = Modifier.fillMaxWidth().height(58.dp)) { Text("SOS · Pedir ajuda") }
                    Info("Sua preparação começa aqui", "Organize o que precisa levar e os contatos que quer ter por perto.")
                    OutlinedButton(onClick = { go(Screen.PREPARE) }, modifier = Modifier.fillMaxWidth()) { Text("Abrir meu checklist") }
                }
                Screen.CHATS -> {
                    Text("Conversas", fontSize = 32.sp)
                    var nearby by rememberSaveable { mutableStateOf(false) }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(selected = !nearby, onClick = { nearby = false }, label = { Text("Meus contatos") })
                        FilterChip(selected = nearby, onClick = { nearby = true }, label = { Text("Por perto") })
                    }
                    if (nearby) Info("Você está invisível", "A descoberta de aparelhos ainda será implementada. Nenhuma busca está ativa.")
                    else Info("Suas pessoas. Suas conversas.", "Convites seguros e mensagens serão desenvolvidos na próxima etapa.")
                }
                Screen.TALK -> Info("TalK", "A assistente consultará os conteúdos salvos, mostrando fontes. O modelo de IA ainda não está conectado.")
                Screen.MAP -> Info("Leve sua cidade com você", "Vamos começar por Ribeirão Preto. Mapas, GPS e rotas offline ainda estão em desenvolvimento. Nenhuma localização está sendo coletada.")
                Screen.RESOURCES -> {
                    Text("Recursos", fontSize = 32.sp)
                    listOf("Preparação familiar" to Screen.PREPARE, "Mapas e locais" to Screen.MAP, "Conversas e contatos" to Screen.CHATS).forEach { (label, target) ->
                        OutlinedButton(onClick = { go(target) }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text(label) }
                    }
                    Info("Em construção", "Biblioteca, saúde e cofre serão adicionados por etapas. Esta versão não coleta documentos ou informações de saúde.")
                }
                Screen.PREPARE -> Checklist()
                Screen.HELP -> Info("Nenhum pedido será enviado", "O envio e a confirmação ainda precisam ser implementados. Esta versão inicial não aciona serviços de emergência.")
            }
        }
    }
}
@Composable private fun Info(title: String, body: String) {
    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { Text(title, fontSize = 23.sp); Text(body) } }
}
@Composable private fun Checklist() {
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences("preparation", Context.MODE_PRIVATE) }
    val items = listOf("contacts" to "Separar contatos importantes", "meeting" to "Combinar um ponto de encontro", "power" to "Conferir bateria e carregador", "water" to "Organizar água e itens essenciais")
    Text("Meu preparo", fontSize = 32.sp)
    Text("Suas marcações ficam salvas neste aparelho.")
    items.forEach { (key, label) ->
        var checked by rememberSaveable(key) { mutableStateOf(preferences.getBoolean(key, false)) }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Checkbox(checked = checked, onCheckedChange = { value -> checked = value; preferences.edit().putBoolean(key, value).apply() })
            Text(label, modifier = Modifier.padding(top = 12.dp))
        }
    }
}
