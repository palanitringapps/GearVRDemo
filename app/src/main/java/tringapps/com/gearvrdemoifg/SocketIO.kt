package tringapps.com.gearvrdemoifg

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.greenrobot.eventbus.EventBus
import org.json.JSONException
import org.json.JSONObject
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription
import tringapps.com.gearvrdemoifg.Constants.ANSWER
import tringapps.com.gearvrdemoifg.Constants.CANDIDATE
import tringapps.com.gearvrdemoifg.Constants.CHAT
import tringapps.com.gearvrdemoifg.Constants.CONNECTED
import tringapps.com.gearvrdemoifg.Constants.DISCONNECTED
import tringapps.com.gearvrdemoifg.Constants.ERROR
import tringapps.com.gearvrdemoifg.Constants.GAME
import tringapps.com.gearvrdemoifg.Constants.OFFER

class SocketIO private constructor() {
    private var socket: Socket? = null
    private var isConnected = false

    private val onConnect = Emitter.Listener {
        isConnected = true
        onMessageReceived(CONNECTED, CONNECTED)
    }

    private val onDisconnect = Emitter.Listener {
        isConnected = false
        onMessageReceived(DISCONNECTED, DISCONNECTED)
    }

    private val onConnectError = Emitter.Listener { onMessageReceived(ERROR, ERROR) }

    private val onMessage = Emitter.Listener { args -> onMessageReceived(CHAT, args[0]) }


    private object SingletonHelper {
        val INSTANCE = SocketIO()
    }

    fun connect() {
        try {
            socket = IO.socket(Constants.CHAT_SERVER_URL)
            socket!!.on(Socket.EVENT_CONNECT, onConnect)
                    .on(CHAT, onMessage)
                    .on(Socket.EVENT_CONNECT_ERROR, onConnectError)
                    .on(Socket.EVENT_DISCONNECT, onDisconnect)
            socket!!.connect()

        } catch (e: Exception) {

        }

    }

    fun disConnect() {
        if (socket != null) {
            socket!!.disconnect()
        }
    }

    private fun onMessageReceived(state: String, message: Any) {
        if (message is String) {
            EventBus.getDefault().post(SocketMessage(state, message))
        } else if (message is JSONObject) {
            try {

                val type = message.getString("type")
                when {
                    type.equals(OFFER, ignoreCase = true)
                            || type.equals(ANSWER, ignoreCase = true)
                            || type.equals(CANDIDATE, ignoreCase = true)
                            || type.equals(GAME, ignoreCase = true)
                    -> EventBus.getDefault().post(SocketMessage(type, message))

                }

            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }
    }


    fun emitMessage(message: String) {
        if (isConnected) {
            socket!!.emit(CHAT, message)
        }
    }

    fun emitMessage(message: SessionDescription) {
        if (isConnected) {
            try {
                Log.d("SignallingClient", "emitMessage() called with: message = [$message]")
                val obj = JSONObject()
                obj.put("type", message.type.canonicalForm())
                obj.put("sdp", message.description)
                Log.d("emitMessage", obj.toString())
                socket!!.emit(CHAT, obj)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }


    fun emitIceCandidate(iceCandidate: IceCandidate) {
        if (isConnected) {
            try {
                val `object` = JSONObject()
                `object`.put("type", "candidate")
                `object`.put("label", iceCandidate.sdpMLineIndex)
                `object`.put("id", iceCandidate.sdpMid)
                `object`.put("candidate", iceCandidate.sdp)
                socket!!.emit(CHAT, `object`)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {

        private val TAG = SocketIO::class.java.name

        val instance: SocketIO
            get() = SingletonHelper.INSTANCE
    }

}

