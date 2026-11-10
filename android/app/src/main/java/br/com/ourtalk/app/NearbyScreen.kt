package br.com.ourtalk.app

import android.Manifest
import android.os.Build
import android.content.pm.PackageManager
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import java.text.DateFormat
import java.util.Date

fun nearbyPermissions():Array<String> = buildList {
    if(Build.VERSION.SDK_INT>=31){add(Manifest.permission.BLUETOOTH_SCAN);add(Manifest.permission.BLUETOOTH_CONNECT);add(Manifest.permission.BLUETOOTH_ADVERTISE)}
    if(Build.VERSION.SDK_INT>=33)add(Manifest.permission.NEARBY_WIFI_DEVICES)
    // Android 12 still requires precise location for Nearby; GPS is never read by this feature.
    if(Build.VERSION.SDK_INT<=32){add(Manifest.permission.ACCESS_FINE_LOCATION);add(Manifest.permission.ACCESS_COARSE_LOCATION)}
}.toTypedArray()

@Composable fun NearbyControls(session:NearbySession,onOpen:()->Unit){
    val context=LocalContext.current
    var denied by remember{mutableStateOf(false)}
    var showTerms by remember { mutableStateOf(false) }
    var showUnblock by remember { mutableStateOf(false) }
    var blockedCount by remember(session.active) { mutableIntStateOf(session.blockedCount()) }
    val permissions=rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){result->
        val granted=nearbyPermissions().all{result[it]==true||context.checkSelfPermission(it)==PackageManager.PERMISSION_GRANTED}
        denied=!granted
        if(granted)session.start()
    }
    InfoCard(if(session.active)"Por perto" else "Descoberta desligada",if(session.status=="Descoberta desligada")"Ative quando quiser conversar com alguém por perto." else session.status)
    if(!session.active){
        Text("Ao ativar, seu aparelho anuncia apenas um apelido temporário. Não compartilha seu nome, contatos ou GPS. Você pode conversar sem adicionar a pessoa.")
        Text("Conexão experimental com Google Play Services, Bluetooth e Wi-Fi. O SDK pode coletar diagnósticos de uso conforme as configurações do Google no aparelho.",style=MaterialTheme.typography.bodySmall,color=Color(0xFFADBDC2))
        val start = {
            if(!session.available()){session.start()}else if(nearbyPermissions().all{context.checkSelfPermission(it)==PackageManager.PERMISSION_GRANTED}){denied=false;session.start()}else permissions.launch(nearbyPermissions())
        }
        Button(modifier=Modifier.fillMaxWidth().testTag("nearby-start"),onClick={
            if (session.acceptedTerms()) start() else showTerms = true
        }){Text("Ativar por 10 minutos")}
        if (blockedCount > 0) TextButton(onClick = { showUnblock = true }) {
            Text("Limpar bloqueios ($blockedCount)")
        }
        if (showTerms) AlertDialog(
            onDismissRequest = { showTerms = false },
            title = { Text("Regras das conversas por perto") },
            text = {
                Column {
                    Text("Ameaças, assédio, discriminação, golpes, conteúdo sexual exploratório e divulgação de dados pessoais de terceiros são proibidos. Você pode bloquear um aparelho e preparar uma denúncia na própria conversa.")
                    TextButton(onClick = {
                        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://ourtalk.com.br/termos-conversas.html"))) }
                    }) { Text("Ler os termos completos") }
                }
            },
            confirmButton = { TextButton(onClick = { session.acceptTerms(); showTerms = false; start() }) { Text("Aceitar e continuar") } },
            dismissButton = { TextButton(onClick = { showTerms = false }) { Text("Cancelar") } }
        )
        if (showUnblock) AlertDialog(
            onDismissRequest = { showUnblock = false },
            title = { Text("Limpar todos os bloqueios?") },
            text = { Text("Os aparelhos desbloqueados poderão voltar a iniciar pedidos de conexão.") },
            confirmButton = { TextButton(onClick = { session.unblockAll(); blockedCount = 0; showUnblock = false }) { Text("Limpar bloqueios") } },
            dismissButton = { TextButton(onClick = { showUnblock = false }) { Text("Cancelar") } }
        )
    }else{
        if(session.alias.isNotEmpty())Text("Seu apelido: ${session.alias}",color=TalkLime)
        Text("Encerrar, sair do app ou passar 10 minutos apaga a conversa temporária deste aparelho. O outro participante pode copiar o que recebeu.",style=MaterialTheme.typography.bodySmall)
        if(session.connected)Button(onClick=onOpen,modifier=Modifier.fillMaxWidth()){Text("Abrir conversa próxima")}
        if(!session.busy&&!session.connected&&session.peers.isEmpty())Text("Nenhuma pessoa encontrada ainda. No outro aparelho, ative Por perto também.")
        session.peers.forEach{peer->OutlinedButton(enabled=!session.busy&&!session.connected,onClick={session.request(peer.id)},modifier=Modifier.fillMaxWidth()){Text("Conversar com ${peer.alias}")}}
        OutlinedButton(onClick={session.stop()},modifier=Modifier.fillMaxWidth()){Text("Encerrar e apagar conversa temporária")}
    }
    if(denied)Text("Permissões não concedidas. Biblioteca, checklist e conversas locais continuam disponíveis.",style=MaterialTheme.typography.bodySmall)
    Text("Esta etapa conecta duas pessoas próximas. Não oferece grupos de rede, retransmissão, alcance ilimitado ou envio pela internet.",style=MaterialTheme.typography.bodySmall,color=Color(0xFFADBDC2))
}

@Composable fun NearbyVerification(session:NearbySession){
    val challenge=session.challenge?:return
    var matches by remember(challenge.endpoint){mutableStateOf(false)}
    AlertDialog(onDismissRequest={session.reject()},title={Text("Confirme com a outra pessoa")},text={Column(verticalArrangement=Arrangement.spacedBy(14.dp)){
        Text(challenge.alias)
        Text(challenge.digits,style=MaterialTheme.typography.headlineLarge,color=TalkLime,modifier=Modifier.testTag("nearby-code"))
        Text("Compare pessoalmente ou por um canal confiável. Os dois aparelhos devem mostrar o mesmo código. O apelido não comprova a identidade da pessoa.")
        Row(verticalAlignment=Alignment.CenterVertically){Checkbox(checked=matches,onCheckedChange={matches=it});Text("Conferi: os códigos são iguais")}
    }},confirmButton={TextButton(enabled=matches,onClick={session.confirm()}){Text("Aceitar conexão")}},dismissButton={TextButton(onClick={session.reject()}){Text("Recusar")}})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun NearbyChatScreen(session:NearbySession,onBack:()->Unit){
    val context = LocalContext.current
    var safetyOpen by remember { mutableStateOf(false) }
    var showBlock by remember { mutableStateOf(false) }
    var showReport by remember { mutableStateOf(false) }
    var reportError by remember { mutableStateOf(false) }
    val state=rememberLazyListState()
    LaunchedEffect(session.messages.size){if(session.messages.isNotEmpty())state.animateScrollToItem(session.messages.lastIndex)}
    Scaffold(topBar={TopAppBar(title={Column{Text(if(session.peerAlias.isEmpty())"Conversa próxima" else session.peerAlias);Text(if(session.connected)"Código verificado nesta sessão" else "Conexão encerrada",style=MaterialTheme.typography.labelSmall)}},navigationIcon={IconButton(onClick=onBack){Icon(Icons.AutoMirrored.Outlined.ArrowBack,"Voltar às conversas")}},actions={
        if (session.connected) Box {
            TextButton(onClick = { safetyOpen = true }) { Text("Segurança") }
            DropdownMenu(expanded = safetyOpen, onDismissRequest = { safetyOpen = false }) {
                DropdownMenuItem(text = { Text("Bloquear aparelho") }, onClick = { safetyOpen = false; showBlock = true })
                DropdownMenuItem(text = { Text("Denunciar conversa") }, onClick = { safetyOpen = false; reportError = false; showReport = true })
            }
        }
    })},bottomBar={Surface(modifier=Modifier.imePadding().navigationBarsPadding(),color=Color(0xFF142129)){
        Row(Modifier.padding(12.dp),verticalAlignment=Alignment.Bottom,horizontalArrangement=Arrangement.spacedBy(8.dp)){
            OutlinedTextField(value=session.draft,onValueChange=session::editDraft,placeholder={Text("Mensagem")},modifier=Modifier.weight(1f).testTag("nearby-composer"),maxLines=5,enabled=session.connected)
            FilledIconButton(enabled=session.connected&&session.draft.isNotBlank()&&session.messages.size<300,onClick={session.send(session.draft)}){Icon(Icons.AutoMirrored.Outlined.Send,"Enviar para a pessoa conectada")}
        }
    }}){padding->Column(Modifier.fillMaxSize().padding(padding)){
        Text("Conversa temporária · apagada ao encerrar a sessão neste aparelho. Recebido confirma chegada ao app, sem indicar leitura.",modifier=Modifier.padding(16.dp),style=MaterialTheme.typography.bodySmall)
        if(!session.connected)Text(session.status,modifier=Modifier.padding(16.dp),color=TalkLime)
        if(session.error.isNotEmpty())Text(session.error,modifier=Modifier.padding(16.dp),color=MaterialTheme.colorScheme.error)
        LazyColumn(state=state,modifier=Modifier.weight(1f).fillMaxWidth(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
            items(session.messages,key={"${it.delivery==LiveDelivery.INCOMING}:${it.id}"}){message->val incoming=message.delivery==LiveDelivery.INCOMING
                Row(Modifier.fillMaxWidth(),horizontalArrangement=if(incoming)Arrangement.Start else Arrangement.End){
                    Surface(shape=RoundedCornerShape(16.dp),color=if(incoming)Color(0xFF142129) else Color(0xFF223D2D),modifier=Modifier.widthIn(max=300.dp)){
                        Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){
                            Text(message.text)
                            Text(DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(message.time))+" · "+when(message.delivery){LiveDelivery.SENDING->"Enviando";LiveDelivery.RECEIVED->"Recebido no outro app";LiveDelivery.UNCONFIRMED->"Recebimento não confirmado";LiveDelivery.INCOMING->"Recebida"},style=MaterialTheme.typography.labelSmall,color=Color(0xFFADBDC2))
                        }
                    }
                }
            }
        }
        if(session.messages.size>=300)Text("Limite desta sessão atingido.",modifier=Modifier.padding(16.dp))
    }}
    if (showBlock) AlertDialog(
        onDismissRequest = { showBlock = false },
        title = { Text("Bloquear este aparelho?") },
        text = { Text("A conversa será encerrada e novas sessões com esta instalação serão recusadas.") },
        confirmButton = { TextButton(onClick = { session.blockPeer(); showBlock = false }) { Text("Bloquear") } },
        dismissButton = { TextButton(onClick = { showBlock = false }) { Text("Cancelar") } }
    )
    if (showReport) AlertDialog(
        onDismissRequest = { showReport = false },
        title = { Text("Denunciar conversa") },
        text = { Text(if (reportError)
            "Nenhum aplicativo de e-mail disponível. Escreva para contato@ourtalk.com.br."
            else "Abra um rascunho para contato@ourtalk.com.br. Você pode descrever o ocorrido e decidir o que enviar. Nenhuma mensagem da conversa é anexada automaticamente.") },
        confirmButton = { TextButton(onClick = {
            runCatching {
                val address = Uri.parse("mailto:contato@ourtalk.com.br?subject=Den%C3%BAncia%20OurTalk")
                context.startActivity(Intent(Intent.ACTION_SENDTO, address))
            }.onSuccess { showReport = false }.onFailure { reportError = true }
        }) { Text("Abrir e-mail") } },
        dismissButton = { TextButton(onClick = { showReport = false }) { Text("Cancelar") } }
    )
}
