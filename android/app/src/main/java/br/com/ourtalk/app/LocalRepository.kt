package br.com.ourtalk.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import br.com.ourtalk.core.Guide

data class SavedContact(val id: String, val name: String)
data class ChatThread(val id:String,val title:String,val group:Boolean,val memberIds:List<String>,val updatedAt:Long)
data class LocalMessage(val id:String,val body:String,val createdAt:Long)
data class PreparationTask(val id: String, val title: String, val done: Boolean)

class LocalRepository(context: Context) {
 private val preferences = context.applicationContext.getSharedPreferences("ourtalk-local-v1", Context.MODE_PRIVATE)
 val guides: List<Guide> = context.assets.open("guides.json").bufferedReader().use { reader ->
  val json=JSONArray(reader.readText())
  List(json.length()) { index -> val g=json.getJSONObject(index); Guide(g.getString("id"),g.getString("title"),g.getString("category"),g.getString("body"),g.getString("source"),g.getString("sourceUrl"),g.getString("reviewedAt")) }
 }
 private fun array(key: String): JSONArray = try { JSONArray(preferences.getString(key,"[]")) } catch (_: Exception) { JSONArray() }
 fun contacts(): List<SavedContact> {val a=array("contacts");return List(a.length()){i->val c=a.getJSONObject(i);SavedContact(c.getString("id"),c.getString("name"))}}
 fun addContact(name: String): SavedContact { val value=name.trim();require(value.isNotEmpty()&&value.length<=80);val all=contacts();require(all.size<100);val contact=SavedContact(UUID.randomUUID().toString(),value);saveContacts(all+contact);return contact }
 private fun saveContacts(contacts: List<SavedContact>) {val a=JSONArray();contacts.forEach {a.put(JSONObject().put("id",it.id).put("name",it.name))};preferences.edit().putString("contacts",a.toString()).apply()}
 fun draft(id: String): String = preferences.getString("draft-$id","") ?: ""
 fun saveDraft(id: String,text: String) {require(threads().any{it.id==id});require(text.length<=2000);preferences.edit().putString("draft-$id",text).apply()}
 fun tasks(): List<PreparationTask> {
  if(!preferences.contains("tasks")) return listOf("contacts" to "Separar contatos importantes","meeting" to "Combinar um ponto de encontro","power" to "Conferir bateria e carregador","water" to "Organizar água e itens essenciais").map{PreparationTask(it.first,it.second,false)}
  val a=array("tasks");return List(a.length()){i->val t=a.getJSONObject(i);PreparationTask(t.getString("id"),t.getString("title"),t.getBoolean("done"))}
 }
 fun saveTasks(tasks: List<PreparationTask>) {require(tasks.size<=100);val a=JSONArray();tasks.forEach{require(it.title.isNotBlank()&&it.title.length<=160);a.put(JSONObject().put("id",it.id).put("title",it.title).put("done",it.done))};preferences.edit().putString("tasks",a.toString()).apply()}
 fun favorites(): Set<String> = preferences.getStringSet("favorites",emptySet())?.toSet() ?: emptySet()
 fun toggleFavorite(id: String) {require(guides.any{it.id==id});val set=favorites().toMutableSet();if(!set.add(id))set.remove(id);preferences.edit().putStringSet("favorites",set).apply()}

 fun threads(): List<ChatThread> {val a=array("threads");return List(a.length()){i->val t=a.getJSONObject(i);val members=t.getJSONArray("members");ChatThread(t.getString("id"),t.getString("title"),t.getBoolean("group"),List(members.length()){members.getString(it)},t.getLong("updatedAt"))}.sortedByDescending{it.updatedAt}}
 private fun saveThreads(threads:List<ChatThread>) {val a=JSONArray();threads.forEach{a.put(JSONObject().put("id",it.id).put("title",it.title).put("group",it.group).put("members",JSONArray(it.memberIds)).put("updatedAt",it.updatedAt))};preferences.edit().putString("threads",a.toString()).apply()}
 fun directThread(contact:SavedContact):ChatThread {val all=threads();all.find{!it.group&&it.memberIds==listOf(contact.id)}?.let{return it};require(all.size<100);val thread=ChatThread(UUID.randomUUID().toString(),contact.name,false,listOf(contact.id),System.currentTimeMillis());saveThreads(all+thread);return thread}
 fun createGroup(title:String,members:List<String>):ChatThread {val name=title.trim();require(name.isNotEmpty()&&name.length<=60);val ids=members.distinct();require(ids.isNotEmpty()&&ids.size<=63&&ids.all{id->contacts().any{it.id==id}});val all=threads();require(all.size<100);val group=ChatThread(UUID.randomUUID().toString(),name,true,ids,System.currentTimeMillis());saveThreads(all+group);return group}
 fun messages(threadId:String):List<LocalMessage> {val a=array("messages-$threadId");return List(a.length()){i->val m=a.getJSONObject(i);LocalMessage(m.getString("id"),m.getString("body"),m.getLong("createdAt"))}}
 fun saveLocalMessage(threadId:String,body:String):LocalMessage {val all=threads();require(all.any{it.id==threadId});val text=body.trim();require(text.isNotEmpty()&&text.length<=2000);val old=messages(threadId);require(old.size<300);val message=LocalMessage(UUID.randomUUID().toString(),text,System.currentTimeMillis());val json=JSONArray();(old+message).forEach{json.put(JSONObject().put("id",it.id).put("body",it.body).put("createdAt",it.createdAt).put("state","LOCAL_ONLY"))};preferences.edit().putString("messages-$threadId",json.toString()).apply();saveThreads(all.map{if(it.id==threadId)it.copy(updatedAt=message.createdAt)else it});return message}
}
