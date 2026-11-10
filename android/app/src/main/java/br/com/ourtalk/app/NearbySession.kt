package br.com.ourtalk.app

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import br.com.ourtalk.core.ChatFrame
import br.com.ourtalk.core.SessionGate
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import java.util.UUID

data class NearbyPeer(val id: String, val alias: String)
data class NearbyChallenge(val endpoint: String, val alias: String, val digits: String)
enum class LiveDelivery { SENDING, RECEIVED, UNCONFIRMED, INCOMING }
data class LiveMessage(val id: UUID, val text: String, val time: Long, val delivery: LiveDelivery)

/** Foreground-only, one-to-one Nearby session with a local block list. */
@SuppressLint("MissingPermission") // Entry point is guarded by runtime permission checks in NearbyScreen.
class NearbySession(context: Context) {
    private val app = context.applicationContext
    private val client by lazy { Nearby.getConnectionsClient(app) }
    private val handler = Handler(Looper.getMainLooper())
    private val gate = SessionGate()
    private val identity by lazy { NearbyIdentity(app) }
    private var sessionToken: ByteArray? = null
    private var incomingConnection = false
    private var peerIdentity: String? = null
    private var generation = 0
    private var deadline = 0L
    private var pendingTimeout: Runnable? = null
    private var stopTimeout: Runnable? = null
    private val received = mutableMapOf<UUID,String>()
    private val pending = mutableMapOf<UUID, Runnable>()
    private var incomingWindow = 0L
    private var incomingCount = 0
    var peers by mutableStateOf<List<NearbyPeer>>(emptyList()); private set
    var challenge by mutableStateOf<NearbyChallenge?>(null); private set
    var messages by mutableStateOf<List<LiveMessage>>(emptyList()); private set
    var active by mutableStateOf(false); private set
    var connected by mutableStateOf(false); private set
    var busy by mutableStateOf(false); private set
    var alias by mutableStateOf(""); private set
    var peerAlias by mutableStateOf(""); private set
    var status by mutableStateOf("Descoberta desligada"); private set
    var error by mutableStateOf(""); private set
    var draft by mutableStateOf(""); private set
    private val serviceId = "${app.packageName}.nearby.v2"

    fun acceptedTerms() = identity.acceptedTerms
    fun acceptTerms() = identity.acceptTerms()
    fun blockedCount() = identity.blockedCount()
    fun unblockAll() = identity.unblockAll()
    fun blockPeer() {
        val peer = peerIdentity ?: return
        identity.block(peer)
        stop("Aparelho bloqueado. A conversa foi encerrada.")
    }

    fun available() = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(app) == ConnectionResult.SUCCESS

    fun start() {
        if(active || !acceptedTerms())return
        if(!available()){status="Google Play Services não está disponível para a conexão próxima neste aparelho.";return}
        stop("")
        val token=++generation
        gate.start(); active=true
        alias="OurTalk-${UUID.randomUUID().toString().take(8)}"
        status="Procurando pessoas próximas. Visível por até 10 minutos."
        deadline=SystemClock.elapsedRealtime()+600_000
        stopTimeout=Runnable { if(generation==token)stop("A sessão de 10 minutos terminou. Ative novamente quando quiser.") }.also{handler.postDelayed(it,600_000)}
        try {
            client.startAdvertising(alias,serviceId,lifecycle(token),AdvertisingOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build())
                .addOnSuccessListener { if(generation!=token)client.stopAdvertising() }
                .addOnFailureListener { if(generation==token)stop("Não foi possível ativar a conexão. Confira permissões, Bluetooth e Wi-Fi.") }
            client.startDiscovery(serviceId,discovery(token),DiscoveryOptions.Builder().setStrategy(Strategy.P2P_POINT_TO_POINT).build())
                .addOnSuccessListener { if(generation!=token)client.stopDiscovery() }
                .addOnFailureListener { if(generation==token)stop("Não foi possível procurar aparelhos. Confira permissões, Bluetooth e Wi-Fi.") }
        } catch(_:SecurityException){stop("As permissões de conexão não estão disponíveis.")}
    }
    private fun discovery(token:Int) = object:EndpointDiscoveryCallback() {
        override fun onEndpointFound(id:String,info:DiscoveredEndpointInfo){
            if(generation!=token||!active||gate.state()!=SessionGate.State.DISCOVERING||info.serviceId!=serviceId)return
            if(peers.size>=30 || !info.endpointName.matches(Regex("OurTalk-[0-9a-f]{8}")))return
            peers=(peers.filterNot{it.id==id}+NearbyPeer(id,info.endpointName)).take(30)
        }
        override fun onEndpointLost(id:String){if(generation==token)peers=peers.filterNot{it.id==id}}
    }
    fun request(id:String){
        val peer=peers.find{it.id==id}?:return
        if(!active||busy||connected)return
        runCatching { gate.request(id) }.getOrElse{return}
        busy=true;peerAlias=peer.alias;status="Aguardando o pedido de conexão…"
        val token=generation
        try{client.requestConnection(alias,id,lifecycle(token)).addOnFailureListener{if(generation==token)stop("O pedido não foi concluído. Ative novamente para tentar.")}}
        catch(_:SecurityException){stop("A permissão de conexão foi removida.")}
        connectionTimeout(token)
    }
    private fun connectionTimeout(token:Int){
        pendingTimeout?.let{handler.removeCallbacks(it)}
        pendingTimeout=Runnable{if(generation==token&&!connected)stop("O pedido expirou sem confirmação dos dois aparelhos.")}.also{handler.postDelayed(it,60_000)}
    }
    private fun lifecycle(token:Int) = object:ConnectionLifecycleCallback(){
        override fun onConnectionInitiated(id:String,info:ConnectionInfo){
            if(generation!=token||!active)return rejectStale(id)
            val code=info.authenticationDigits
            val raw=info.rawAuthenticationToken
            if(!info.endpointName.matches(Regex("OurTalk-[0-9a-f]{8}")) || code.isNullOrBlank() || raw.isNullOrEmpty() || !runCatching{gate.challenge(id)}.isSuccess){rejectStale(id);return}
            sessionToken=raw.copyOf()
            incomingConnection=info.isIncomingConnection
            busy=true;peerAlias=info.endpointName
            challenge=NearbyChallenge(id,peerAlias,code)
            status="Compare o código nos dois aparelhos."
            client.stopAdvertising();client.stopDiscovery();peers=emptyList()
            connectionTimeout(token)
        }
        override fun onConnectionResult(id:String,result:ConnectionResolution){
            if(generation!=token||!active)return
            if(result.status.isSuccess && runCatching{gate.connected(id)}.isSuccess){
                challenge=null;status="Verificando o aparelho…"
                client.stopAdvertising();client.stopDiscovery();peers=emptyList()
                runCatching {
                    val proof=identity.proof(checkNotNull(sessionToken), incomingConnection)
                    client.sendPayload(id,Payload.fromBytes(proof)).addOnFailureListener {
                        if(generation==token) stop("Não foi possível verificar o aparelho.")
                    }
                }.onFailure { stop("Não foi possível verificar o aparelho.") }
            }else stop("A conexão não foi aceita e verificada nos dois aparelhos.")
        }
        override fun onDisconnected(id:String){if(generation==token && id==gate.endpoint())stop("Conexão encerrada. Ative novamente para conversar.")}
    }
    private fun rejectStale(id:String){runCatching{client.rejectConnection(id)}}
    fun confirm(){
        val current=challenge?:return
        if(!active)return
        if(!runCatching{gate.confirm(current.endpoint)}.isSuccess)return
        val token=generation
        challenge=null;status="Aguardando a confirmação no outro aparelho…"
        try{client.acceptConnection(current.endpoint,payloads(token)).addOnFailureListener{if(generation==token)stop("A conexão não pôde ser aceita.")}}
        catch(_:SecurityException){stop("A permissão de conexão foi removida.")}
    }
    fun reject(){challenge?.let{rejectStale(it.endpoint)};stop("Pedido recusado. Descoberta desligada.")}
    private fun payloads(token:Int)=object:PayloadCallback(){
        override fun onPayloadReceived(endpoint:String,payload:Payload){
            if(generation!=token||!active||!gate.permitsData(endpoint))return
            if(payload.type!=Payload.Type.BYTES){client.cancelPayload(payload.id);stop("A sessão foi encerrada por receber um formato não permitido.");return}
            if (!connected) {
                val peer=runCatching {
                    identity.verify(checkNotNull(payload.asBytes()),checkNotNull(sessionToken),incomingConnection)
                }.getOrElse { stop("O outro aparelho não pôde ser verificado.");return }
                if (identity.isBlocked(peer)) { stop("Aparelho bloqueado.");return }
                peerIdentity=peer
                pendingTimeout?.let{handler.removeCallbacks(it)};pendingTimeout=null
                connected=true;busy=false;status="Conexão próxima verificada nesta sessão"
                return
            }
            val now=SystemClock.elapsedRealtime()
            if(now-incomingWindow>10_000){incomingWindow=now;incomingCount=0}
            if(++incomingCount>60){stop("A sessão foi encerrada pelo limite de mensagens.");return}
            val frame=runCatching{ChatFrame.decode(payload.asBytes())}.getOrElse{stop("A sessão foi encerrada por receber uma mensagem inválida.");return}
            if(frame.kind==ChatFrame.Kind.ACK){
                val timeout=pending.remove(frame.id)?:return
                handler.removeCallbacks(timeout)
                messages=messages.map{if(it.id==frame.id&&it.delivery!=LiveDelivery.INCOMING)it.copy(delivery=LiveDelivery.RECEIVED)else it}
            }else{
                if(received.containsKey(frame.id)&&received[frame.id]!=frame.text){stop("A sessão foi encerrada por receber um identificador repetido com conteúdo diferente.");return}
                if(frame.id !in received){
                    if(messages.size>=300){stop("Limite desta sessão atingido. Encerre e inicie uma nova conversa.");return}
                    received[frame.id]=frame.text
                    messages=messages+LiveMessage(frame.id,frame.text,System.currentTimeMillis(),LiveDelivery.INCOMING)
                }
                sendFrame(endpoint,ChatFrame.ack(frame.id),token,null)
            }
        }
        override fun onPayloadTransferUpdate(endpoint:String,update:PayloadTransferUpdate) { /* An SDK transfer is not an application receipt. */ }
    }
    fun editDraft(value:String){if(connected&&value.length<=2000)draft=value}
    fun send(text:String):Boolean{
        val endpoint=gate.endpoint()?:return false
        if(!active||!connected||!gate.permitsData(endpoint)||SystemClock.elapsedRealtime()>=deadline||messages.size>=300)return false
        val frame=runCatching{ChatFrame.text(UUID.randomUUID(),text.trim())}.getOrElse{error="O texto contém caracteres não permitidos ou excede o limite.";return false}
        error=""
        draft=""
        messages=messages+LiveMessage(frame.id,frame.text,System.currentTimeMillis(),LiveDelivery.SENDING)
        val token=generation
        val timeout=Runnable{if(generation==token){pending.remove(frame.id);markUnconfirmed(frame.id)}}
        pending[frame.id]=timeout;handler.postDelayed(timeout,30_000)
        sendFrame(endpoint,frame,token,frame.id)
        return true
    }
    private fun sendFrame(endpoint:String,frame:ChatFrame,token:Int,messageId:UUID?){
        try{client.sendPayload(endpoint,Payload.fromBytes(frame.encode())).addOnFailureListener{if(generation==token&&messageId!=null){pending.remove(messageId)?.let{handler.removeCallbacks(it)};markUnconfirmed(messageId)}}}
        catch(_:SecurityException){stop("A permissão de conexão foi removida.")}
    }
    private fun markUnconfirmed(id:UUID){messages=messages.map{if(it.id==id&&it.delivery==LiveDelivery.SENDING)it.copy(delivery=LiveDelivery.UNCONFIRMED)else it}}
    fun stop(reason:String="Descoberta desligada"){
        generation++
        pendingTimeout?.let{handler.removeCallbacks(it)};pendingTimeout=null
        stopTimeout?.let{handler.removeCallbacks(it)};stopTimeout=null
        pending.values.forEach{handler.removeCallbacks(it)};pending.clear()
        if(active){runCatching{client.stopAdvertising()};runCatching{client.stopDiscovery()};runCatching{client.stopAllEndpoints()}}
        gate.stop();active=false;connected=false;busy=false;challenge=null;peers=emptyList()
        received.clear();incomingCount=0;incomingWindow=0
        sessionToken?.fill(0);sessionToken=null;peerIdentity=null;incomingConnection=false
        // Ephemeral chats and codes are intentionally removed when the session ends.
        messages=emptyList();draft="";alias="";peerAlias="";status=reason;error=""
    }
}
