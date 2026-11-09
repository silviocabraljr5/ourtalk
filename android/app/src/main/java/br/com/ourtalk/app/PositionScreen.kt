package br.com.ourtalk.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

internal data class PositionFix(val latitude:Double,val longitude:Double,val accuracy:Float,val time:Long,val mock:Boolean)
private class GpsReader(context:Context) {
 private val manager=context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
 private val handler=Handler(Looper.getMainLooper())
 private var listener:LocationListener?=null
 private var timeout:Runnable?=null
 fun cancel(){ listener?.let{manager.removeUpdates(it)};listener=null;timeout?.let{handler.removeCallbacks(it)};timeout=null }
 @SuppressLint("MissingPermission")
 @Suppress("DEPRECATION")
 fun request(result:(PositionFix?,String?)->Unit){
  cancel()
  if(!manager.allProviders.contains(LocationManager.GPS_PROVIDER)){result(null,"Este aparelho não oferece leitura GPS.");return}
  if(!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){result(null,"Ative a localização nas configurações do aparelho e tente novamente.");return}
  val callback=object:LocationListener {
   override fun onLocationChanged(location:Location){
    if(!location.latitude.isFinite()||!location.longitude.isFinite()||!location.hasAccuracy())return
    val mock=if(Build.VERSION.SDK_INT>=31)location.isMock else location.isFromMockProvider
    val fix=PositionFix(location.latitude,location.longitude,location.accuracy,location.time,mock)
    cancel();result(fix,null)
   }
   override fun onProviderDisabled(provider:String){cancel();result(null,"A localização foi desligada no aparelho.")}
   override fun onProviderEnabled(provider:String){}
   override fun onStatusChanged(provider:String?,status:Int,extras:Bundle?){}
  }
  listener=callback
  try {
   manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000L,0f,callback,Looper.getMainLooper())
   val stop=Runnable{cancel();result(null,"Não foi possível obter uma leitura agora. Tente em uma área com melhor recepção dos satélites.")};timeout=stop;handler.postDelayed(stop,30000)
  }catch(_:SecurityException){cancel();result(null,"A permissão de localização não está disponível.")}
  catch(_:IllegalArgumentException){cancel();result(null,"O GPS não está disponível neste aparelho.")}
 }
}

@Composable fun PositionScreen(){
 val context=LocalContext.current;val lifecycleOwner=LocalLifecycleOwner.current
 val reader=remember{GpsReader(context)}
 val stored=remember{runCatching{SecureStore(context,"ourtalk-position-v1")}}
 val preferences=stored.getOrNull()
 if(preferences==null){InfoCard("Posição salva indisponível","Não foi possível abrir os dados protegidos. Feche e abra o app para tentar novamente. A posição salva não foi apagada.");OfflineCityMap(null,false);return}
 fun readSaved():PositionFix?=try{if(!preferences.contains("latitude"))null else PositionFix(preferences.getString("latitude",null)!!.toDouble(),preferences.getString("longitude",null)!!.toDouble(),preferences.getFloat("accuracy",0f),preferences.getLong("time",0L),preferences.getBoolean("mock",false))}catch(_:Exception){null}
 var fix by remember{mutableStateOf(readSaved())};var fromSaved by remember{mutableStateOf(fix!=null)}
 var now by remember(fix){mutableLongStateOf(System.currentTimeMillis())}
 LaunchedEffect(fix,fromSaved){
  val age=fix?.let{now-it.time}
  if(!fromSaved && age!=null && age in -60000L..120000L){
   delay(120001L-age)
   now=System.currentTimeMillis()
  }
 }
 var busy by remember{mutableStateOf(false)};var message by remember{mutableStateOf("")};var save by remember{mutableStateOf(false)}
 val start:()->Unit={busy=true;message="Buscando uma leitura por satélite…";reader.request{position,error->busy=false;if(position!=null){fix=position;fromSaved=false;message="";if(save){preferences.edit().putString("latitude",position.latitude.toString()).putString("longitude",position.longitude.toString()).putFloat("accuracy",position.accuracy).putLong("time",position.time).putBoolean("mock",position.mock).apply()}}else message=error?:"Leitura indisponível."}}
 val permissions=rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){grants->if(grants[Manifest.permission.ACCESS_FINE_LOCATION]==true)start() else message="A leitura GPS desta versão precisa da localização precisa. Você pode continuar usando os outros recursos sem concedê-la."}
 DisposableEffect(lifecycleOwner){val observer=LifecycleEventObserver{_,event->if(event==Lifecycle.Event.ON_STOP){reader.cancel();if(busy){busy=false;message="Busca interrompida. Toque novamente quando quiser consultar a posição."}}};lifecycleOwner.lifecycle.addObserver(observer);onDispose{reader.cancel();lifecycleOwner.lifecycle.removeObserver(observer)}}
 ScreenTitle("Minha localização")
 Text("Consulte sua posição no aparelho. Ela não é enviada para contatos.")
 fix?.let{position->
  val old=now-position.time !in -60000L..120000L
  InfoCard(if(fromSaved||old)"Última posição registrada" else "Leitura do GPS",String.format(Locale.US,"Latitude: %.6f\nLongitude: %.6f",position.latitude,position.longitude)+"\nPrecisão informada: ${position.accuracy.toInt()} m\nHorário: ${DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.MEDIUM).format(Date(position.time))}")
  if(position.mock)Text("Posição de teste informada pelo aparelho.",color=TalkLime)
  if(fromSaved||old)Text("Essa leitura não confirma onde você está agora.",style=MaterialTheme.typography.bodySmall)
 }
 Row {Checkbox(checked=save,onCheckedChange={save=it},enabled=!busy);Text("Salvar a próxima leitura neste aparelho",modifier=Modifier.padding(top=12.dp))}
 Button(enabled=!busy,onClick={if(context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)start() else permissions.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION))},modifier=Modifier.fillMaxWidth().height(56.dp)){Text(if(busy)"Buscando satélites…" else "Consultar minha posição")}
 if(message.isNotEmpty())Text(message,style=MaterialTheme.typography.bodyMedium)
 if(preferences.contains("latitude"))TextButton(onClick={preferences.edit().clear().apply();if(fromSaved){fix=null;fromSaved=false};message="Posição salva removida deste aparelho."}){Text("Apagar posição salva")}
 val age=fix?.let{now-it.time}
 OfflineCityMap(fix, !fromSaved && age!=null && age in -60000L..120000L)
}
