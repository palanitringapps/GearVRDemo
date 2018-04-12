package tringapps.com.gearvrdemoifg;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRScene;

public class VrActivity extends GVRActivity implements EventEndListener {

    private SampleMain main;
    private BalloonMain balloonMain = new BalloonMain();

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        main = new SampleMain(this, this);
        setMain(main, "gvr.xml");
    }

    @Override
    public void changeMainScene(GVRScene mainScene) {

        getGVRContext().getMainScene().clear();
        main.loadBallon(getGVRContext());
        getGVRContext().setMainScene(main.mScene);

    }
}
