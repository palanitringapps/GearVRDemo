package tringapps.com.gearvrdemoifg;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRScene;


public class VrActivity extends GVRActivity implements EventEndListener {

    private SampleMain main;
    private static final int CAPTURE_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        main = new SampleMain(this, this);
        setMain(main, "gvr.xml");

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

    @Override
    public void changeMainScene(GVRScene mainScene) {

        getGVRContext().getMainScene().clear();
        main.loadBallon(getGVRContext());
        getGVRContext().setMainScene(main.mScene);

    }
}
