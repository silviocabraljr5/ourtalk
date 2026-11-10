package br.com.ourtalk.app

import android.content.Intent
import android.os.BatteryManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.ourtalk.core.Guide
import br.com.ourtalk.core.OfflineSearch
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.UUID

val TalkLime=Color(0xFFA9EA1C)
val TalkDark=Color(0xFF0B1216)
private enum class Destination(val title:String) { HOME("Início"),CONTACTS("Conversas"),TALK("TalK"),MAP("Mapa"),RESOURCES("Recursos"),CHECKLIST("Preparar"),LIBRARY("Biblioteca"),GUIDE("Leitura"),CHAT("Conversa"),PEERCHAT("Conversa próxima"),HELP("Pedir ajuda"),ABOUT("Sobre o OurTalk") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun OurTalkApp() {
 val context=LocalContext.current
 val stored=remember { runCatching{LocalRepository(context)} }
 val repository=stored.getOrNull()
 if(repository==null){Column(Modifier.fillMaxSize().statusBarsPadding().padding(24.dp),verticalArrangement=Arrangement.spacedBy(20.dp)){ScreenTitle("Dados locais indisponíveis");Text("Não foi possível abrir os dados protegidos deste aparelho. Feche e abra o app para tentar novamente. Nenhum histórico foi apagado automaticamente.")};return}
 val nearby=remember { NearbySession(context) }
 val lifecycleOwner=LocalLifecycleOwner.current
 DisposableEffect(lifecycleOwner,nearby){val observer=LifecycleEventObserver{_,event->if(event==Lifecycle.Event.ON_STOP)nearby.stop("A conexão foi encerrada ao sair do app.")};lifecycleOwner.lifecycle.addObserver(observer);onDispose{lifecycleOwner.lifecycle.removeObserver(observer);nearby.stop()}}
 NearbyVerification(nearby)
 var route by rememberSaveable { mutableStateOf(Destination.HOME.name) }
 var previousRoute by rememberSaveable { mutableStateOf(Destination.HOME.name) }
 var selectedGuide by rememberSaveable { mutableStateOf("") }
 var selectedThread by rememberSaveable { mutableStateOf("") }
 val screen=Destination.valueOf(route)
 var showInvite by rememberSaveable { mutableStateOf(false) }
 var showInvitePrivacy by rememberSaveable { mutableStateOf(false) }
 var inviteRevision by remember { mutableIntStateOf(0) }
 ContactInviteFlow(repository, showInvite, { showInvite = false }, showInvitePrivacy, { showInvitePrivacy = false }, screen == Destination.CONTACTS) {
  inviteRevision++
 }
 val go:(Destination)->Unit={ next->previousRoute=route;route=next.name }
 val back:()->Unit={ route=when(screen){Destination.GUIDE->if(previousRoute==Destination.TALK.name)Destination.TALK.name else Destination.LIBRARY.name;Destination.CHAT,Destination.PEERCHAT->Destination.CONTACTS.name;else->Destination.HOME.name} }
 BackHandler(screen!=Destination.HOME) { back() }
 val tabs=listOf(Destination.HOME,Destination.CONTACTS,Destination.TALK,Destination.MAP)
 val icons=listOf(Icons.Outlined.Home,Icons.Outlined.ChatBubbleOutline,Icons.Outlined.AutoAwesome,Icons.Outlined.Map)
 val scrollState=rememberScrollState()
 LaunchedEffect(route){scrollState.scrollTo(0)}
 if(screen==Destination.CHAT){ChatThreadScreen(repository,selectedThread,onBack=back);return}
 if(screen==Destination.PEERCHAT){NearbyChatScreen(nearby,onBack=back);return}
 Scaffold(
  topBar={TopAppBar(title={Row(verticalAlignment=Alignment.CenterVertically){Image(painterResource(R.drawable.brand_symbol),"OurTalk",Modifier.size(40.dp));Text("Our",fontWeight=FontWeight.Bold);Text("Talk",color=TalkLime,fontWeight=FontWeight.Bold)}},actions={TextButton(onClick={go(Destination.RESOURCES)}){Icon(Icons.Outlined.GridView,null);Text(" Recursos")}})},
  bottomBar={NavigationBar(containerColor=TalkDark){tabs.forEachIndexed { i,tab->NavigationBarItem(modifier=Modifier.testTag("nav-${tab.name}"),selected=screen==tab||(tab==Destination.CONTACTS&&screen==Destination.CHAT),onClick={go(tab)},icon={Icon(icons[i],null)},label={Text(tab.title)})}}}
 ) { padding ->
  Column(Modifier.fillMaxSize().padding(padding).verticalScroll(scrollState).padding(horizontal=24.dp,vertical=20.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
   if(screen !in tabs) TextButton(onClick=back){Icon(Icons.AutoMirrored.Outlined.ArrowBack,null);Text(" Voltar")}
   when(screen){
    Destination.HOME->{
     Text("Olá!",color=Color(0xFF9DACB0))
     Text("Como podemos\najudar?",fontSize=34.sp,lineHeight=39.sp,fontWeight=FontWeight.Medium)
     Button(onClick={go(Destination.CONTACTS)},modifier=Modifier.fillMaxWidth().height(58.dp)){Icon(Icons.Outlined.ChatBubbleOutline,null);Spacer(Modifier.width(14.dp));Text("Conversar")}
     OutlinedButton(onClick={go(Destination.HELP)},modifier=Modifier.fillMaxWidth().height(58.dp)){Text("SOS",color=Color(0xFFFFA08D));Spacer(Modifier.width(14.dp));Text("Pedir ajuda")}
     val battery=remember { context.getSystemService(BatteryManager::class.java)?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1 }
     InfoCard("Leve o essencial com você",if(battery in 0..100)"Bateria do aparelho: $battery%. Seus guias e checklist já estão disponíveis sem internet." else "Seus guias e checklist já estão disponíveis sem internet.")
     Text("Seus recursos",style=MaterialTheme.typography.titleMedium)
     ResourceButton("Minha preparação","Checklist salvo neste aparelho",Icons.Outlined.Checklist){go(Destination.CHECKLIST)}
     ResourceButton("Biblioteca","Guias para consultar offline",Icons.AutoMirrored.Outlined.MenuBook){go(Destination.LIBRARY)}
     ResourceButton("Localização","Consulte a posição por satélite",Icons.Outlined.MyLocation){go(Destination.MAP)}
    }
    Destination.CONTACTS->InboxScreen(repository,nearby,inviteRevision,{showInvite=true},{showInvitePrivacy=true},onNearbyOpen={go(Destination.PEERCHAT)}){id->selectedThread=id;go(Destination.CHAT)}
    Destination.CHAT,Destination.PEERCHAT->Unit
    Destination.TALK->LibraryScreen(repository,true){id->selectedGuide=id;go(Destination.GUIDE)}
    Destination.LIBRARY->LibraryScreen(repository,false){id->selectedGuide=id;go(Destination.GUIDE)}
    Destination.GUIDE->repository.guides.find{it.id==selectedGuide}?.let{GuideScreen(repository,it)}
    Destination.MAP->PositionScreen()
    Destination.CHECKLIST->ChecklistScreen(repository)
    Destination.RESOURCES->{
     ScreenTitle("Recursos")
     ResourceButton("Preparar","Organize seu checklist",Icons.Outlined.Checklist){go(Destination.CHECKLIST)}
     ResourceButton("Biblioteca","Leituras e favoritos offline",Icons.AutoMirrored.Outlined.MenuBook){go(Destination.LIBRARY)}
     ResourceButton("Contatos","Conversas e grupos locais",Icons.Outlined.PeopleOutline){go(Destination.CONTACTS)}
     ResourceButton("Sobre e privacidade","Conheça esta versão",Icons.Outlined.Info){go(Destination.ABOUT)}
     OutlinedButton(onClick={context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply{type="text/plain";putExtra(Intent.EXTRA_TEXT,"Conheça o OurTalk: um projeto de comunicação, informação e apoio entre pessoas. https://ourtalk.com.br")},"Convidar para o OurTalk"))},modifier=Modifier.fillMaxWidth()){Text("Convidar para conhecer o projeto")}
    }
    Destination.HELP->HelpRequestScreen()
    Destination.ABOUT->{
     ScreenTitle("Feito para aproximar")
     InfoCard("OurTalk · ${BuildConfig.VERSION_NAME}","Biblioteca, busca por palavras, favoritos, checklist, contatos e grupos locais. O mapa municipal offline de Ribeirão Preto inclui hospitais e unidades de pronto atendimento. A conexão individual por perto é experimental e depende de Google Play Services, permissões e verificação do código nos dois aparelhos.")
     InfoCard("Privacidade","Históricos locais e posição salva usam AES-GCM com chave do Android Keystore. A conexão próxima usa a criptografia do Nearby; não anuncia GPS nem agenda. Conversas próximas ficam só na memória e são apagadas ao encerrar. O SDK Google pode coletar diagnósticos conforme as configurações do aparelho. Não há criptografia de grupos ou identidade permanente verificada nesta etapa.")
     OutlinedButton(onClick={runCatching { context.startActivity(Intent(Intent.ACTION_VIEW,android.net.Uri.parse("https://ourtalk.com.br/privacidade-app.html"))) }},modifier=Modifier.fillMaxWidth()){Text("Ler política de privacidade")}
     InfoCard("O que vem a seguir","Comunicação entre plataformas, ampliação dos mapas, rotas offline, pacotes de conteúdo e TalK com IA. Os recursos precisam ser validados em aparelhos reais antes do lançamento.")
    }
   }
  }
 }
}
@Composable fun ScreenTitle(text:String){Text(text,fontSize=30.sp,lineHeight=36.sp,fontWeight=FontWeight.Medium)}
@Composable fun InfoCard(title:String,body:String){Card(Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=Color(0xFF142129))){Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text(title,style=MaterialTheme.typography.titleLarge);Text(body,color=Color(0xFFADBDC2),style=MaterialTheme.typography.bodyMedium)}}}
@Composable private fun ResourceButton(title:String,description:String,icon:androidx.compose.ui.graphics.vector.ImageVector,onClick:()->Unit){OutlinedCard(onClick=onClick,modifier=Modifier.fillMaxWidth()){Row(Modifier.padding(18.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(16.dp)){Icon(icon,null,tint=TalkLime);Column(Modifier.weight(1f)){Text(title,style=MaterialTheme.typography.titleMedium);Text(description,color=Color(0xFF9DACB0),style=MaterialTheme.typography.bodySmall)};Icon(Icons.Outlined.ChevronRight,null)}}}

@Composable private fun LibraryScreen(repository:LocalRepository,talk:Boolean,onOpen:(String)->Unit){
 var query by remember{mutableStateOf("")};var favoritesOnly by rememberSaveable{mutableStateOf(false)}
 val favorites=repository.favorites();val matches=remember(query){if(query.isBlank())repository.guides else OfflineSearch.find(repository.guides,query)}
 ScreenTitle(if(talk)"TalK" else "Biblioteca")
 Text(if(talk)"Pesquise nos conteúdos que já estão no aparelho." else "Leituras para levar com você.",color=Color(0xFFADBDC2))
 if(talk)Text("Busca offline · sem respostas geradas por IA",style=MaterialTheme.typography.labelMedium,color=TalkLime)
 OutlinedTextField(value=query,onValueChange={if(it.length<=160)query=it},singleLine=true,modifier=Modifier.fillMaxWidth(),label={Text("Buscar conteúdo salvo")},leadingIcon={Icon(Icons.Outlined.Search,null)})
 FilterChip(selected=favoritesOnly,onClick={favoritesOnly=!favoritesOnly},label={Text("Favoritos")},leadingIcon={Icon(Icons.Outlined.BookmarkBorder,null,Modifier.size(18.dp))})
 val results=matches.filter{!favoritesOnly||it.id in favorites};if(results.isEmpty())InfoCard("Nenhum conteúdo encontrado","Tente outras palavras ou consulte todos os guias salvos.")
 results.forEach{guide->OutlinedCard(onClick={onOpen(guide.id)},modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Text(guide.category,color=TalkLime,style=MaterialTheme.typography.labelSmall);Text(guide.title,style=MaterialTheme.typography.titleLarge);Text(guide.source,color=Color(0xFF9DACB0),style=MaterialTheme.typography.bodySmall)}}}
}
@Composable private fun GuideScreen(repository:LocalRepository,guide:Guide){var favorite by remember(guide.id){mutableStateOf(guide.id in repository.favorites())};Text(guide.category,color=TalkLime);ScreenTitle(guide.title);Text(guide.body,lineHeight=26.sp);HorizontalDivider();Text("Fonte: ${guide.source}",style=MaterialTheme.typography.bodySmall);Text("Revisado em ${guide.reviewedAt}",style=MaterialTheme.typography.bodySmall);Text(guide.sourceUrl,style=MaterialTheme.typography.bodySmall,color=Color(0xFF9DACB0));OutlinedButton(onClick={repository.toggleFavorite(guide.id);favorite=!favorite}){Icon(if(favorite)Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,null);Text(if(favorite)" Remover dos favoritos" else " Salvar nos favoritos")}}

@Composable private fun ChecklistScreen(repository:LocalRepository){
 var tasks by remember{mutableStateOf(repository.tasks())};var title by remember{mutableStateOf("")}
 ScreenTitle("Meu preparo");Text("${tasks.count{it.done}} de ${tasks.size} itens concluídos",color=TalkLime)
 tasks.forEach{task->Row(Modifier.fillMaxWidth().clickable{tasks=tasks.map{if(it.id==task.id)it.copy(done=!it.done)else it};repository.saveTasks(tasks)},verticalAlignment=Alignment.CenterVertically){Checkbox(checked=task.done,onCheckedChange={value->tasks=tasks.map{if(it.id==task.id)it.copy(done=value)else it};repository.saveTasks(tasks)});Text(task.title,modifier=Modifier.weight(1f))}}
 OutlinedTextField(value=title,onValueChange={if(it.length<=160)title=it},modifier=Modifier.fillMaxWidth(),label={Text("Adicionar ao meu preparo")})
 OutlinedButton(enabled=title.isNotBlank()&&tasks.size<100,onClick={tasks=tasks+PreparationTask(UUID.randomUUID().toString(),title.trim(),false);repository.saveTasks(tasks);title=""},modifier=Modifier.fillMaxWidth()){Text("Adicionar item")}
 Text("Salvo somente neste aparelho.",style=MaterialTheme.typography.bodySmall)
}
