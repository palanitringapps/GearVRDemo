package tringapps.com.gearvrdemoifg

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle

import org.gearvrf.GVRActivity
import org.gearvrf.GVRScene
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONException
import org.json.JSONObject

import tringapps.com.gearvrdemoifg.Constants.GAME
import tringapps.com.gearvrdemoifg.Constants.MESSAGE


class VrActivity : GVRActivity(), EventEndListener {

    companion object {
        private const val CAPTURE_PERMISSION_REQUEST_CODE = 1
    }

    private var main: SampleMain? = null
    private var screenCaptureIntent: Intent? = null
    private var screenCaptureResultCode: Int? = null

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(message: SocketMessage) {
        if (message.state == GAME) {
            val game = message.message as JSONObject?
            try {
                if (game != null)
                    when (game.getString(MESSAGE)) {
                        Constants.START -> loadGame()
                        Constants.STOP -> loadSplash()
                        Constants.INIT -> startMirroring()
                        Constants.TERMINATE -> stopMirroring()
                    }
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        main = SampleMain(this, this)
        setMain(main)


        if (!MirroringService.isScreenCaptureStarted) {
            MirroringService.isScreenCaptureStarted = true
            startScreenCapture()
        }
    }

    override fun changeMainScene(mainScene: GVRScene) {
        gvrContext.mainScene.clear()
        main!!.loadBallon(gvrContext)
    }


    @TargetApi(21)
    private fun startScreenCapture() {
        val mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), CAPTURE_PERMISSION_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == CAPTURE_PERMISSION_REQUEST_CODE) {
            screenCaptureIntent = data
            screenCaptureResultCode = resultCode
        }

    }

    private fun startMirroring() {
        if (screenCaptureIntent != null && screenCaptureResultCode == Activity.RESULT_OK) {
            val cbIntent = Intent(this, MirroringService::class.java)
            cbIntent.putExtra(Constants.SCREEN_DATA, screenCaptureIntent)
            startService(cbIntent)
        }
    }

    private fun stopMirroring() {
        if (screenCaptureIntent != null && screenCaptureResultCode == Activity.RESULT_OK) {
            val cbIntent = Intent(this, MirroringService::class.java)
            stopService(cbIntent)
        }
    }

    private fun loadSplash() {
        gvrContext.mainScene.clear()
        main!!.loadSplash(gvrContext)
    }

    private fun loadGame() {
        gvrContext.mainScene.clear()
        main!!.loadBallon(gvrContext)
    }
}
