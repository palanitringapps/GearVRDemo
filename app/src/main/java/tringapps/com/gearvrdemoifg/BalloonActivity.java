package tringapps.com.gearvrdemoifg;

import org.gearvrf.GVRActivity;
import android.os.Bundle;
import android.view.MotionEvent;

public class BalloonActivity extends GVRActivity {

    BalloonMain main = new BalloonMain();

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setMain(main, "gvr.xml");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        main.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
}
