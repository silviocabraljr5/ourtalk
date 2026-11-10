package br.com.ourtalk.app

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LocalFeaturesTest {
 @get:Rule val ui=createAndroidComposeRule<MainActivity>()
 @Before fun resetOwnedTestData(){val context=ApplicationProvider.getApplicationContext<Context>();context.getSharedPreferences("ourtalk-encrypted-v1",Context.MODE_PRIVATE).edit().clear().commit();context.getSharedPreferences("ourtalk-local-v1",Context.MODE_PRIVATE).edit().clear().commit();context.getSharedPreferences("ourtalk-position-v1",Context.MODE_PRIVATE).edit().clear().commit()}
 private fun tap(text:String,scroll:Boolean=false){val node=ui.onNodeWithText(text,useUnmergedTree=false);if(scroll)node.performScrollTo();node.performTouchInput{click()}}
 private fun nav(tag:String){ui.onNodeWithTag("nav-$tag").performClick()}
 private fun field(label:String,value:String){ui.onNode(hasSetTextAction() and hasText(label)).performTextInput(value)}
 @Test fun municipalHealthMapFiltersAndFindsFacilitiesWithoutLocation(){
  nav("MAP")
  ui.onNodeWithText("Hospitais e pronto atendimento").performScrollTo().assertIsDisplayed()
  ui.onNodeWithContentDescription("Mapa municipal de saúde de Ribeirão Preto. As unidades também estão na lista abaixo.").assertExists()
  tap("Só pronto atendimento municipal",true)
  ui.onNodeWithText("5 unidades encontradas").assertExists()
  val search=ui.onNode(hasSetTextAction() and hasText("Buscar unidade ou endereço"))
  search.performScrollTo().performTextInput("ribeirao verde")
  Espresso.closeSoftKeyboard()
  tap("UPA Ribeirão Verde · Pronto atendimento municipal",true)
  ui.onNodeWithText("UPA Ribeirão Verde").assertExists()
  search.performTextReplacement("local-inexistente")
  ui.onNodeWithText("0 unidades encontradas").assertExists()
 }
 @Test fun unavailableNearbyDoesNotBlockOfflineFeatures(){
  val context=ApplicationProvider.getApplicationContext<Context>()
  org.junit.Assume.assumeFalse(NearbySession(context).available())
  nav("CONTACTS")
  ui.onNodeWithTag("chat-tab-nearby").performClick()
  ui.onNodeWithTag("nearby-start").performScrollTo().performClick()
  ui.onNodeWithText("Regras das conversas por perto").assertIsDisplayed()
  ui.onNodeWithText("Aceitar e continuar").performClick()
  ui.onNodeWithText("Google Play Services não está disponível para a conexão próxima neste aparelho.").assertExists()
  nav("TALK")
  field("Buscar conteúdo salvo","bateria")
  ui.onNodeWithText("Fazer a bateria durar mais").assertExists()
 }
 @Test fun offlineLibrarySearchAndChecklistSurviveRecreation(){
  tap("Biblioteca",true)
  tap("Fazer a bateria durar mais",true)
  tap(" Salvar nos favoritos",true)
  Espresso.pressBack()
  tap("Favoritos")
  ui.onNodeWithText("Fazer a bateria durar mais").assertExists()
  nav("TALK")
  field("Buscar conteúdo salvo","LOCALIZACAO")
  ui.onNodeWithText("Localização e mapas sem internet").assertExists()
  ui.onNodeWithText("Fazer a bateria durar mais").assertDoesNotExist()
  ui.onNode(hasSetTextAction() and hasText("Buscar conteúdo salvo")).performTextReplacement("assunto_inexistente")
  ui.onNodeWithText("Nenhum conteúdo encontrado").assertExists()
  Espresso.closeSoftKeyboard()
  nav("HOME")
  tap("Minha preparação",true)
  tap("Separar contatos importantes")
  field("Adicionar ao meu preparo","Item de teste local")
  Espresso.closeSoftKeyboard()
  tap("Adicionar item",true)
  ui.activityRule.scenario.recreate()
  ui.waitForIdle()
  ui.onNodeWithText("Item de teste local").assertExists()
  ui.onNodeWithText("1 de 5 itens concluídos").assertExists()
 }
 @Test fun createContactGroupAndPersistLocalConversation(){
  nav("CONTACTS")
  ui.onNodeWithTag("chat-tab-contacts").performClick()
  tap(" Adicionar pessoa")
  field("Nome","Pessoa teste local")
  tap("Salvar")
  ui.onNodeWithText("Pessoa teste local").assertExists()
  ui.onNodeWithTag("chat-tab-conversations").performClick()
  tap(" Novo grupo")
  field("Nome do grupo","Grupo de teste")
  Espresso.closeSoftKeyboard()
  ui.onNodeWithContentDescription("Selecionar Pessoa teste local").performClick()
  tap("Criar grupo")
  ui.onNodeWithText("Grupo de teste").assertIsDisplayed()
  ui.onNodeWithTag("message-composer").performTextInput("Mensagem salva no aparelho")
  ui.onNodeWithTag("store-message").performClick()
  Espresso.closeSoftKeyboard()
  ui.onNodeWithText("Mensagem salva no aparelho").assertIsDisplayed()
  ui.onNodeWithText("Neste aparelho").assertExists()
  ui.onNodeWithContentDescription("Participantes").performClick()
  ui.onNodeWithText("Pessoa teste local").assertIsDisplayed()
  tap("Fechar")
  ui.activityRule.scenario.recreate()
  ui.waitForIdle()
  ui.onNodeWithText("Mensagem salva no aparelho").assertExists()
  ui.onNodeWithContentDescription("Voltar às conversas").performClick()
  ui.onNodeWithText("Grupo de teste").assertExists()
  ui.onNodeWithTag("chat-tab-nearby").performClick()
  ui.onNodeWithText("Descoberta desligada").assertExists()
 }
}
