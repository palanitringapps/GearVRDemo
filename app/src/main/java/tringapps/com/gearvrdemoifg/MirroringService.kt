package tringapps.com.gearvrdemoifg

import android.annotation.TargetApi
import android.app.Service
import android.content.Intent
import android.media.projection.MediaProjection
import android.os.IBinder
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONException
import org.json.JSONObject
import org.webrtc.*
import tringapps.com.gearvrdemoifg.Constants.ANSWER
import tringapps.com.gearvrdemoifg.Constants.CANDIDATE
import tringapps.com.gearvrdemoifg.Constants.SCREEN_DATA

class MirroringService : Service() {

    companion object {
        @JvmStatic
        var isScreenCaptureStarted = false
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private val peerConnectionFactory: PeerConnectionFactory by lazy {
        //Initialize PeerConnectionFactory globals.
        val initializationOptions = PeerConnectionFactory.InitializationOptions.builder(this)
                .setEnableVideoHwAcceleration(true)
                .createInitializationOptions()
        PeerConnectionFactory.initialize(initializationOptions)
        PeerConnectionFactory()
    }

    internal var localPeer: PeerConnection? = null
    private var localVideoTrack: VideoTrack? = null
    var isStarted: Boolean = false
    var isInitiator: Boolean = true
    private var peerIceServers: MutableList<PeerConnection.IceServer> = ArrayList()
    private val sdpConstraints = MediaConstraints()
    private lateinit var videoCapturerAndroid: VideoCapturer

    override fun onCreate() {
        super.onCreate()
        EventBus.getDefault().register(this)
    }


    override fun onDestroy() {
        super.onDestroy()
        isScreenCaptureStarted = false
        videoCapturerAndroid.stopCapture()
        localPeer!!.close()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(message: SocketMessage) {
        when {
            message.state.equals(ANSWER, ignoreCase = true) -> onAnswerReceived(message.message as JSONObject)
            message.state.equals(CANDIDATE, ignoreCase = true) -> onIceCandidateReceived(message.message as JSONObject)
            else -> {
            }
        }
    }

    private fun onTryToStart() {
        if (!isStarted && localVideoTrack != null) {
            createPeerConnection()
            isStarted = true
            if (isInitiator) {
                doCall()
            }
        }
    }

    private fun createPeerConnection() {
        val rtcConfig = PeerConnection.RTCConfiguration(peerIceServers)
        // TCP candidates are only useful when connecting to a server that supports
        // ICE-TCP.
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        // Use ECDSA encryption.
        rtcConfig.keyType = PeerConnection.KeyType.ECDSA
        localPeer = peerConnectionFactory.createPeerConnection(rtcConfig, object : CustomPeerConnectionObserver("localPeerCreation") {
            override fun onIceCandidate(iceCandidate: IceCandidate) {
                super.onIceCandidate(iceCandidate)
                onIceCandidateReceived(iceCandidate)
            }

        })

        addStreamToLocalPeer()
    }


    private fun addStreamToLocalPeer() {
        //creating local mediastream
        val stream = peerConnectionFactory.createLocalMediaStream("102")
        stream.addTrack(localVideoTrack)
        localPeer!!.addStream(stream)
    }

    /**
     * This method is called when the app is initiator - We generate the offer and send it over through socket
     * to remote peer
     */
    private fun doCall() {
        // Create SDP constraints.

        sdpConstraints.mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        localPeer!!.createOffer(object : CustomSdpObserver("localCreateOffer") {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                super.onCreateSuccess(sessionDescription)
                localPeer!!.setLocalDescription(CustomSdpObserver("localSetLocalDesc"), sessionDescription)
                SocketIO.instance.emitMessage(sessionDescription)
            }
        }, sdpConstraints)
    }


    /**
     * Received local ice candidate. Send it to remote peer through signalling for negotiation
     */
    fun onIceCandidateReceived(iceCandidate: IceCandidate) {
        //we have received ice candidate. We can set it to the other peer.
        SocketIO.instance.emitIceCandidate(iceCandidate)
    }

    /**
     * Received remote ice candidate.
     */
    private fun onIceCandidateReceived(data: JSONObject) {
        try {
            localPeer!!.addIceCandidate(IceCandidate(data.getString("id"), data.getInt("label"), data.getString("candidate")))
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    private fun onAnswerReceived(data: JSONObject) {
        try {
            localPeer!!.setRemoteDescription(CustomSdpObserver("localSetRemote"), SessionDescription(SessionDescription.Type.fromCanonicalForm(data.getString("type").toLowerCase()), data.getString("sdp")))
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    @TargetApi(21)
    private fun createScreenCapturer(data: Intent): VideoCapturer {

        return ScreenCapturerAndroid(
                data, object : MediaProjection.Callback() {
            override fun onStop() {
            }
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            videoCapturerAndroid = createScreenCapturer(intent.getParcelableExtra(SCREEN_DATA))
            localVideoTrack = peerConnectionFactory.createVideoTrack("100", peerConnectionFactory.createVideoSource(videoCapturerAndroid))
            videoCapturerAndroid.startCapture(480, 360, 30)
            localVideoTrack!!.setEnabled(true)
            onTryToStart()
        } else {
            stopSelf()
        }
        return Service.START_NOT_STICKY

    }


}
