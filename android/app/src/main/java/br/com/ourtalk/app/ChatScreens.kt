package br.com.ourtalk.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.text.DateFormat
import java.util.Date

@Composable fun InboxScreen(repository:LocalRepository,nearby:NearbySession,inviteRevision:Int=0,onInvite:()->Unit={},onNearbyOpen:()->Unit,onOpen:(String)->Unit){
 var tab by rememberSaveable{mutableStateOf("conversations")};var query by remember{mutableStateOf("")};var groupsOnly by rememberSaveable{mutableStateOf(false)}
 var contacts by remember{mutableStateOf(repository.contacts())};var threads by remember{mutableStateOf(repository.threads())}
 LaunchedEffect(inviteRevision) { contacts = repository.contacts() }
 var addPerson by rememberSaveable{mutableStateOf(false)};var newGroup by rememberSaveable{mutableStateOf(false)};var newChat by rememberSaveable{mutableStateOf(false)};var error by remember{mutableStateOf("")}
 ScreenTitle("Conversas")
 OutlinedTextField(value=query,onValueChange={if(it.length<=80)query=it},label={Text("Buscar conversa ou pessoa")},leadingIcon={Icon(Icons.Outlined.Search,null)},modifier=Modifier.fillMaxWidth(),singleLine=true)
 ScrollableTabRow(selectedTabIndex=when(tab){"contacts"->1;"nearby"->2;else->0},edgePadding=0.dp){listOf("conversations" to "Conversas","contacts" to "Contatos","nearby" to "Por perto").forEach{(key,title)->Tab(modifier=Modifier.testTag("chat-tab-$key"),selected=tab==key,onClick={tab=key},text={Text(title)})}}
 when(tab){
  "nearby"->{
   NearbyControls(nearby,onNearbyOpen)
  }
  "contacts"->{
   Button(onClick=onInvite,modifier=Modifier.fillMaxWidth()){Text("Convidar para meus contatos")}
   OutlinedButton(onClick={addPerson=true},enabled=contacts.size<100,modifier=Modifier.fillMaxWidth()){Icon(Icons.Outlined.PersonAdd,null);Text(" Adicionar pessoa")}
   val filtered=contacts.filter{it.name.contains(query,true)}.sortedBy{it.name.lowercase()}
   if(filtered.isEmpty())Text("Seus contatos adicionados aparecerão aqui.",color=Color(0xFF9DACB0))
   filtered.forEach{person->OutlinedCard(onClick={runCatching{repository.directThread(person)}.onSuccess{onOpen(it.id)}.onFailure{error="Não foi possível criar outra conversa nesta versão."}},modifier=Modifier.fillMaxWidth()){Row(Modifier.padding(18.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)){Icon(Icons.Outlined.PersonOutline,null,tint=TalkLime);Column{Text(person.name,style=MaterialTheme.typography.titleMedium);Text("Contato salvo",style=MaterialTheme.typography.bodySmall,color=Color(0xFF9DACB0))}}}}
  }
  else->{
   Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){FilterChip(selected=!groupsOnly,onClick={groupsOnly=false},label={Text("Todas")});FilterChip(selected=groupsOnly,onClick={groupsOnly=true},label={Text("Grupos")});TextButton(onClick={newGroup=true}){Icon(Icons.Outlined.GroupAdd,null);Text(" Novo grupo")}}
   val filtered=threads.filter{(!groupsOnly||it.group)&&it.title.contains(query,true)}
   if(filtered.isEmpty())InfoCard(if(groupsOnly)"Crie seu primeiro grupo" else "A primeira conversa começa aqui",if(groupsOnly)"Reúna seus contatos em uma conversa de grupo." else "Abra uma conversa com um contato ou crie um grupo para organizar as pessoas importantes.")
   filtered.forEach{thread->val last=repository.messages(thread.id).lastOrNull();OutlinedCard(onClick={onOpen(thread.id)},modifier=Modifier.fillMaxWidth()){Row(Modifier.padding(18.dp),horizontalArrangement=Arrangement.spacedBy(14.dp),verticalAlignment=Alignment.CenterVertically){Icon(if(thread.group)Icons.Outlined.Groups else Icons.Outlined.PersonOutline,null,tint=TalkLime);Column(Modifier.weight(1f)){Text(thread.title,style=MaterialTheme.typography.titleMedium,maxLines=1,overflow=TextOverflow.Ellipsis);Text(last?.body?:if(thread.group)"Grupo criado neste aparelho" else "Comece uma conversa",style=MaterialTheme.typography.bodySmall,color=Color(0xFF9DACB0),maxLines=1,overflow=TextOverflow.Ellipsis)};if(last!=null)Text(DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(last.createdAt)),style=MaterialTheme.typography.labelSmall)}}}
   Button(onClick={newChat=true},modifier=Modifier.fillMaxWidth().height(54.dp)){Icon(Icons.Outlined.ChatBubbleOutline,null);Text(" Nova conversa")}
  }
 }
 if(error.isNotEmpty())Text(error,color=MaterialTheme.colorScheme.error)
 if(addPerson){var name by remember{mutableStateOf("")};AlertDialog(onDismissRequest={addPerson=false},title={Text("Adicionar pessoa")},text={Column(verticalArrangement=Arrangement.spacedBy(12.dp)){Text("Salve uma referência neste aparelho. Conectar e confirmar a identidade serão etapas separadas.");OutlinedTextField(value=name,onValueChange={if(it.length<=80)name=it},label={Text("Nome")},singleLine=true)}},confirmButton={TextButton(enabled=name.isNotBlank(),onClick={runCatching{repository.addContact(name)}.onSuccess{contacts=repository.contacts();addPerson=false}.onFailure{error="Não foi possível salvar esse contato."}}){Text("Salvar")}},dismissButton={TextButton(onClick={addPerson=false}){Text("Cancelar")}})}
 if(newChat){AlertDialog(onDismissRequest={newChat=false},title={Text("Nova conversa")},text={Column(verticalArrangement=Arrangement.spacedBy(8.dp)){if(contacts.isEmpty())Text("Adicione uma pessoa para começar.");LazyColumn(Modifier.heightIn(max=300.dp)){items(contacts,key={it.id}){person->TextButton(onClick={runCatching{repository.directThread(person)}.onSuccess{newChat=false;onOpen(it.id)}.onFailure{error="Não foi possível abrir a conversa."}}){Text(person.name)}}}}},confirmButton={TextButton(onClick={newChat=false;addPerson=true}){Text("Adicionar pessoa")}},dismissButton={TextButton(onClick={newChat=false}){Text("Voltar")}})}
 if(newGroup)GroupCreationDialog(contacts,onDismiss={newGroup=false},onCreate={name,members->runCatching{repository.createGroup(name,members)}.onSuccess{threads=repository.threads();newGroup=false;onOpen(it.id)}.onFailure{error="Confira o nome e os participantes do grupo."}})
}

@Composable private fun GroupCreationDialog(contacts:List<SavedContact>,onDismiss:()->Unit,onCreate:(String,List<String>)->Unit){
 var name by remember{mutableStateOf("")};var selected by remember{mutableStateOf(emptySet<String>())}
 AlertDialog(onDismissRequest=onDismiss,title={Text("Novo grupo")},text={Column(verticalArrangement=Arrangement.spacedBy(12.dp)){
  OutlinedTextField(value=name,onValueChange={if(it.length<=60)name=it},label={Text("Nome do grupo")},singleLine=true)
  Text("Participantes",style=MaterialTheme.typography.labelLarge)
  if(contacts.isEmpty())Text("Adicione contatos antes de criar um grupo.")
  LazyColumn(Modifier.heightIn(max=260.dp)){items(contacts,key={it.id}){person->Row(verticalAlignment=Alignment.CenterVertically){Checkbox(modifier=Modifier.semantics{contentDescription="Selecionar ${person.name}"},checked=person.id in selected,onCheckedChange={checked->if(checked&&selected.size<63)selected=selected+person.id else if(!checked)selected=selected-person.id});Text(person.name)}}}
  Text("O grupo e as mensagens ficam somente neste aparelho nesta versão de teste.",style=MaterialTheme.typography.bodySmall)
 }},confirmButton={TextButton(enabled=name.isNotBlank()&&selected.isNotEmpty(),onClick={onCreate(name,selected.toList())}){Text("Criar grupo")}},dismissButton={TextButton(onClick=onDismiss){Text("Cancelar")}})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun ChatThreadScreen(repository:LocalRepository,threadId:String,onBack:()->Unit){
 val thread=remember(threadId){repository.threads().find{it.id==threadId}}
 if(thread==null){onBack();return}
 var messages by remember(threadId){mutableStateOf(repository.messages(threadId))};var text by remember(threadId){mutableStateOf(repository.draft(threadId))};var info by rememberSaveable{mutableStateOf(false)};var error by remember{mutableStateOf("")};val listState=rememberLazyListState()
 LaunchedEffect(messages.size){if(messages.isNotEmpty())listState.animateScrollToItem(messages.lastIndex)}
 Scaffold(topBar={TopAppBar(navigationIcon={IconButton(onClick=onBack){Icon(Icons.AutoMirrored.Outlined.ArrowBack,"Voltar às conversas")}},title={Column{Text(thread.title,maxLines=1,overflow=TextOverflow.Ellipsis);Text(if(thread.group)"Grupo · ${thread.memberIds.size+1} participantes locais" else "Conversa local",style=MaterialTheme.typography.labelSmall,color=Color(0xFF9DACB0))}},actions={if(thread.group)IconButton(onClick={info=true}){Icon(Icons.Outlined.Group,"Participantes")}})},bottomBar={Surface(color=Color(0xFF142129),modifier=Modifier.imePadding().navigationBarsPadding()){Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){if(error.isNotEmpty())Text(error,color=MaterialTheme.colorScheme.error,style=MaterialTheme.typography.bodySmall);Row(verticalAlignment=Alignment.Bottom,horizontalArrangement=Arrangement.spacedBy(8.dp)){OutlinedTextField(value=text,onValueChange={if(it.length<=2000){text=it;repository.saveDraft(threadId,it)}},placeholder={Text("Mensagem")},modifier=Modifier.weight(1f).testTag("message-composer"),maxLines=5);FilledIconButton(modifier=Modifier.testTag("store-message"),enabled=text.isNotBlank(),onClick={runCatching{repository.saveLocalMessage(threadId,text)}.onSuccess{messages=repository.messages(threadId);text="";repository.saveDraft(threadId,"");error=""}.onFailure{error="Não foi possível salvar outra mensagem local."}}){Icon(Icons.AutoMirrored.Outlined.Send,"Salvar mensagem neste aparelho")}}}}}){padding->
  Column(Modifier.fillMaxSize().padding(padding)){
   Text("Versão de teste: as mensagens abaixo ficam neste aparelho. Ainda não são enviadas aos participantes.",modifier=Modifier.fillMaxWidth().background(Color(0xFF243025)).padding(14.dp),style=MaterialTheme.typography.bodySmall,color=Color(0xFFB7CCAD))
   if(messages.isEmpty())Box(Modifier.weight(1f).fillMaxWidth(),contentAlignment=Alignment.Center){Text("Comece uma conversa.",color=Color(0xFF9DACB0))}
   else LazyColumn(state=listState,modifier=Modifier.weight(1f).fillMaxWidth(),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
    items(messages,key={it.id}){message->Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.End){Surface(shape=RoundedCornerShape(18.dp,4.dp,18.dp,18.dp),color=Color(0xFF223D2D),modifier=Modifier.widthIn(max=300.dp)){Column(Modifier.padding(horizontal=14.dp,vertical=10.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){Text(message.body);Row(Modifier.align(Alignment.End),horizontalArrangement=Arrangement.spacedBy(5.dp),verticalAlignment=Alignment.CenterVertically){Text(DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(message.createdAt)),style=MaterialTheme.typography.labelSmall,color=Color(0xFFABBDAF));Icon(Icons.Outlined.Schedule,null,Modifier.size(12.dp),tint=Color(0xFFABBDAF));Text("Neste aparelho",style=MaterialTheme.typography.labelSmall,color=Color(0xFFABBDAF))}}}}}
   }
  }
 }
 if(info)AlertDialog(onDismissRequest={info=false},title={Text(thread.title)},text={Column(verticalArrangement=Arrangement.spacedBy(10.dp)){Text("Você");thread.memberIds.forEach{id->Text(repository.contacts().find{it.id==id}?.name?:"Referência de contato")};Text("Participação e convites entre aparelhos ainda serão implementados.",style=MaterialTheme.typography.bodySmall)}},confirmButton={TextButton(onClick={info=false}){Text("Fechar")}})
}
