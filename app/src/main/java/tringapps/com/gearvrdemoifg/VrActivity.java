package tringapps.com.gearvrdemoifg;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;


public class VrActivity extends GVRActivity implements EventEndListener  {

    private static final int CAPTURE_PERMISSION_REQUEST_CODE = 1;
    private SampleMain main;
    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(SocketMessage message) {
        if (message.getState().equals(SocketIO.GAME)){
            JSONObject game= (JSONObject) message.getMessage();
            try {
                if(game.getString(SocketIO.MESSAGE).equals(Constants.START)){
                    loadGame();
                }else if(game.getString(SocketIO.MESSAGE).equals(Constants.STOP)){
                    loadSplash();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        main = new SampleMain(this, this);
        setMain(new Main());


        if(!Singleton.getInstance().isScreenCaptureStarted()) {
            Singleton.getInstance().setScreenCaptureStarted(true);
            startScreenCapture();
        }
    }

    @TargetApi(21)
    private void startScreenCapture() {

        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), CAPTURE_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,Intent data) {
        if (requestCode != CAPTURE_PERMISSION_REQUEST_CODE || resultCode != Activity.RESULT_OK)
            return;

        Intent cbIntent =  new Intent(this, MirroringService.class);
        cbIntent.putExtra(Constants.SCREEN_DATA, data);
        startService(cbIntent);
    }


    public void loadSplash(){
        getGVRContext().getMainScene().clear();
        main.loadSplash(getGVRContext());
    }

    public void loadGame(){
        getGVRContext().getMainScene().clear();
        main.loadGame(getGVRContext());
    }


    @Override
    public void changeMainScene(GVRScene mainScene) {
        getGVRContext().getMainScene().clear();
        main.loadBallon(getGVRContext());
    }

    private final class Main extends GVRMain {

        @Override
        public void onInit(GVRContext gvrContext) throws Throwable {

            //Load texture
            GVRTexture texture = gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext, R.drawable.__default_splash_screen__));

            //Create a rectangle with the texture we just loaded
            GVRSceneObject quad = new GVRSceneObject(gvrContext, 4, 2, texture);
            quad.getTransform().setPosition(0, 0, -3);

            //Add rectangle to the scene
            gvrContext.getMainScene().addSceneObject(quad);


        }

        @Override
        public SplashMode getSplashMode() {
            return SplashMode.NONE;
        }

        @Override
        public void onStep() {
            //Add update logic here
        }
    }
}
