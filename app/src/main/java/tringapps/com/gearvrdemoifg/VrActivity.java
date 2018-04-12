package tringapps.com.gearvrdemoifg;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRTexture;
import org.gearvrf.io.GVRControllerType;
import org.gearvrf.io.GVRCursorController;
import org.gearvrf.io.GVRInputManager;

import java.util.ArrayList;

public class VrActivity extends GVRActivity {
    Main main = new Main();
    PickerHandler pickerHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * Set Main Scene
         * It will be displayed when app starts
         */

        main = new Main();

        //main.getSplashMesh(getGVRContext());
        //main.getSplashShader(getGVRContext());

        //setMain(main,"gvr.xml");

        setMain(new Main());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                Log.i("sdjfgcsdjhvnkfdm", "dsnfcbjdsncds");
                if (pickerHandler.picketObject != null) {
                    Toast.makeText(getBaseContext(), "clicked ", Toast.LENGTH_SHORT).show();

                    Log.i("sdjfgcsdjhvnkfdm", "dsnfcbjdsncds   sjgcjskd hj jhdgcjhdb");
                    pickerHandler.picketObject.getRenderData().getMaterial().setDiffuseColor(0, 0.0f, 1.0f, 1.0f);
                }
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }


    /*GVRSceneObject makeBall(GVRContext context) {
        GVRSceneObject sphere = new GVRSceneObject(context);
        GVRRenderData renderData = sphere.getRenderData();
        GVRMaterial gvrMaterial = new GVRMaterial(context);
        gvrMaterial.setDiffuseColor(1.0f, 0.0f, 1.0f, 0.5f);
        sphere.setName("hello");
        renderData.setAlphaBlend(true);
        renderData.setMaterial(gvrMaterial);
        renderData.setRenderingOrder(GVRRenderData.GVRRenderingOrder.TRANSPARENT);
        sphere.getTransform().setPositionZ(-0.5f);
        return sphere;
    }*/

    private final class Main extends GVRMain {

        GVRScene scene = null;


        @Override
        public void onInit(GVRContext gvrContext) throws Throwable {

            //Load texture
            /*pickerHandler = new PickerHandler();
            GVRTexture texture = gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext,
                    R.drawable.__default_splash_screen__));

            //Create a rectangle with the texture we just loaded
            GVRSceneObject quad = new GVRSceneObject(gvrContext, 4, 2, texture);
            quad.getTransform().setPosition(0, 0, -3);
            quad.getEventReceiver().addListener(pickerHandler);
            gvrContext.getMainScene().addSceneObject(quad);*/

            mMainScene = gvrContext.getMainScene();

            //Load texture
            GVRTexture texture = gvrContext.getAssetLoader().loadTexture(new GVRAndroidResource(gvrContext, R.drawable.__default_splash_screen__));

            //Create a rectangle with the texture we just loaded
            GVRSceneObject quad = new GVRSceneObject(gvrContext, 4, 2, texture);
            quad.getTransform().setPosition(0, 0, -3);

            //Add rectangle to the scene
            gvrContext.getMainScene().addSceneObject(quad);

            //Listen controller events
            GVRInputManager input = gvrContext.getInputManager();
            input.selectController(new GVRInputManager.ICursorControllerSelectListener() {
                @Override
                public void onCursorControllerSelected(GVRCursorController newController, GVRCursorController oldController) {

                }
            });
            //input.addCursorControllerListener(listener);

            Log.i("GUI", "Add Controller Listener");
            /*for (GVRCursorController cursor : input.getCursorControllers()) {
                listener.onCursorControllerAdded(cursor);
            }*/





        }

        @Override
        public SplashMode getSplashMode() {
            return SplashMode.AUTOMATIC;
        }

        @Override
        public void onStep() {
            //Add update logic here
        }
    }

    private GVRScene mMainScene;
    private static final float DEPTH = -1.5f;


}
