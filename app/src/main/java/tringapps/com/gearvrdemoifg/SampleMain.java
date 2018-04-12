package tringapps.com.gearvrdemoifg;

import android.graphics.Color;
import android.media.AudioManager;
import android.media.SoundPool;
import android.view.Gravity;
import android.view.MotionEvent;

import org.gearvrf.GVRActivity;
import org.gearvrf.GVRAndroidResource;
import org.gearvrf.GVRAssetLoader;
import org.gearvrf.GVRCollider;
import org.gearvrf.GVRContext;
import org.gearvrf.GVRDirectLight;
import org.gearvrf.GVRMain;
import org.gearvrf.GVRMaterial;
import org.gearvrf.GVRMaterial.GVRShaderType;
import org.gearvrf.GVRMesh;
import org.gearvrf.GVRMeshCollider;
import org.gearvrf.GVRPicker;
import org.gearvrf.GVRRenderData;
import org.gearvrf.GVRScene;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.GVRShader;
import org.gearvrf.GVRSphereCollider;
import org.gearvrf.GVRTexture;
import org.gearvrf.GVRTransform;
import org.gearvrf.IPickEvents;
import org.gearvrf.ITouchEvents;
import org.gearvrf.io.GVRCursorController;
import org.gearvrf.io.GVRInputManager;
import org.gearvrf.scene_objects.GVRCubeSceneObject;
import org.gearvrf.scene_objects.GVRSphereSceneObject;
import org.gearvrf.scene_objects.GVRTextViewSceneObject;
import org.gearvrf.utility.Log;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class SampleMain extends GVRMain {
    private static final String TAG = "SampleMain";

    private static final float UNPICKED_COLOR_R = 0.7f;
    private static final float UNPICKED_COLOR_G = 0.7f;
    private static final float UNPICKED_COLOR_B = 0.7f;
    private static final float UNPICKED_COLOR_A = 1.0f;

    private static final float PICKED_COLOR_R = 1.0f;
    private static final float PICKED_COLOR_G = 0.0f;
    private static final float PICKED_COLOR_B = 0.0f;
    private static final float PICKED_COLOR_A = 1.0f;

    private static final float CLICKED_COLOR_R = 0.5f;
    private static final float CLICKED_COLOR_G = 0.5f;
    private static final float CLICKED_COLOR_B = 1.0f;
    private static final float CLICKED_COLOR_A = 1.0f;

    private static final float SCALE = 200.0f;
    private static final float DEPTH = -7.0f;
    private static final float BOARD_OFFSET = 2.0f;
    private GVRScene mainScene;
    private GVRContext mGVRContext = null;
    private GVRActivity mActivity;
    private GVRSceneObject cursor;
    private GVRCursorController controller;
    private EventEndListener listener;

    SampleMain(GVRActivity activity, EventEndListener listener) {
        mActivity = activity;
        this.listener = listener;
    }

    @Override
    public void onInit(GVRContext gvrContext) {
        mGVRContext = gvrContext;
        mainScene = mGVRContext.getMainScene();
        mainScene.getEventReceiver().addListener(mPickHandler);
        GVRInputManager inputManager = mGVRContext.getInputManager();
        cursor = new GVRSceneObject(mGVRContext, mGVRContext.createQuad(1f, 1f),
                mGVRContext.getAssetLoader().loadTexture(
                        new GVRAndroidResource(mGVRContext, R.raw.cursor)));
        cursor.getRenderData().setDepthTest(false);
        cursor.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.OVERLAY);
        final EnumSet<GVRPicker.EventOptions> eventOptions = EnumSet.of(
                GVRPicker.EventOptions.SEND_TOUCH_EVENTS,
                GVRPicker.EventOptions.SEND_TO_LISTENERS);
        inputManager.selectController(new GVRInputManager.ICursorControllerSelectListener() {
            public void onCursorControllerSelected(GVRCursorController newController, GVRCursorController oldController) {
                if (oldController != null) {
                    oldController.removePickEventListener(mPickHandler);
                }
                controller = newController;
                newController.addPickEventListener(mPickHandler);
                newController.setCursor(cursor);
                newController.setCursorDepth(DEPTH);
                newController.setCursorControl(GVRCursorController.CursorControl.PROJECT_CURSOR_ON_SURFACE);
                newController.getPicker().setEventOptions(eventOptions);
            }
        });

        /*
         * Adding Boards
         */
        GVRSceneObject object = getColorBoard();
        object.getTransform().setPosition(0.0f, BOARD_OFFSET, DEPTH);
        attachMeshCollider(object);
        object.setName("MeshBoard1");
        mainScene.addSceneObject(object);

        object = getColorBoard();
        object.getTransform().setPosition(0.0f, -BOARD_OFFSET, DEPTH);
        attachMeshCollider(object);
        object.setName("MeshBoard2");
        mainScene.addSceneObject(object);

        object = getColorBoard();
        object.getTransform().setPosition(-BOARD_OFFSET, 0.0f, DEPTH);
        attachMeshCollider(object);
        object.setName("MeshBoard3");
        mainScene.addSceneObject(object);

        object = getColorBoard();
        object.getTransform().setPosition(BOARD_OFFSET, 0.0f, DEPTH);
        attachMeshCollider(object);
        object.setName("MeshBoard4");
        mainScene.addSceneObject(object);

        object = getColorBoard();
        object.getTransform().setPosition(BOARD_OFFSET, BOARD_OFFSET, DEPTH);
        object.setName("MeshBoard5");
        attachMeshCollider(object);
        mainScene.addSceneObject(object);

        object = getColorBoard();
        object.getTransform().setPosition(BOARD_OFFSET, -BOARD_OFFSET, DEPTH);
        attachMeshCollider(object);
        object.setName("MeshBoard6");
        mainScene.addSceneObject(object);

        object = getColorBoard();
        object.getTransform().setPosition(-BOARD_OFFSET, BOARD_OFFSET, DEPTH);
        attachSphereCollider(object);
        object.setName("SphereBoard1");
        mainScene.addSceneObject(object);

        object = getColorBoard();
        object.getTransform().setPosition(-BOARD_OFFSET, -BOARD_OFFSET, DEPTH);
        object.setName("SphereBoard2");
        attachSphereCollider(object);
        mainScene.addSceneObject(object);

        GVRMesh mesh = null;
        try {
            mesh = mGVRContext.getAssetLoader().loadMesh(
                    new GVRAndroidResource(mGVRContext, "bunny.obj"));
        } catch (IOException e) {
            e.printStackTrace();
            mesh = null;
        }
        if (mesh == null) {
            mActivity.finish();
            Log.e(TAG, "Mesh was not loaded. Stopping application!");
        }
        // activity was stored in order to stop the application if the mesh is
        // not loaded. Since we don't need anymore, we set it to null to reduce
        // chance of memory leak.
        mActivity = null;

        object = getColorMesh(0.75f, mesh);
        object.getTransform().setPosition(0.0f, 0.0f, DEPTH);
        object.setName("BoundsBunny1");
        attachBoundsCollider(object);
        mainScene.addSceneObject(object);

        object = getColorMesh(0.75f, mesh);
        object.getTransform().setPosition(4.0f, 0.0f, DEPTH);
        attachBoundsCollider(object);
        object.setName("BoundsBunny2");
        mainScene.addSceneObject(object);

        object = getColorMesh(0.75f, mesh);
        object.getTransform().setPosition(-4.0f, 0.0f, DEPTH);
        attachMeshCollider(object);
        object.setName("MeshBunny3");
        mainScene.addSceneObject(object);

        object = getColorMesh(0.75f, mesh);
        object.getTransform().setPosition(0.0f, -4.0f, DEPTH);
        attachMeshCollider(object);
        object.setName("MeshBunny4");
        mainScene.addSceneObject(object);

        GVRAssetLoader assetLoader = gvrContext.getAssetLoader();
        GVRTexture texture = assetLoader.loadTexture(
                new GVRAndroidResource(gvrContext, R.drawable.skybox_gridroom));
        GVRMaterial material = new GVRMaterial(gvrContext);
        GVRSphereSceneObject skyBox = new GVRSphereSceneObject(gvrContext, false, material);
        skyBox.getTransform().setScale(SCALE, SCALE, SCALE);
        skyBox.getRenderData().getMaterial().setMainTexture(texture);
        mainScene.addSceneObject(skyBox);
    }

    private ITouchEvents mPickHandler = new ITouchEvents() {
        private GVRSceneObject movingObject;

        public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) {
            sceneObj.getRenderData().getMaterial().setVec4("u_color", PICKED_COLOR_R,
                    PICKED_COLOR_G, PICKED_COLOR_B,
                    PICKED_COLOR_A);
        }

        public void onTouchStart(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) {
            if (movingObject == null) {
                sceneObj.getRenderData().getMaterial().setVec4("u_color", CLICKED_COLOR_R,
                        CLICKED_COLOR_G, CLICKED_COLOR_B,
                        CLICKED_COLOR_A);
                if (controller.startDrag(sceneObj)) {
                    movingObject = sceneObj;
                }
            }
        }

        public void onTouchEnd(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) {
            sceneObj.getRenderData().getMaterial().setVec4("u_color", PICKED_COLOR_R,
                    PICKED_COLOR_G, PICKED_COLOR_B,
                    PICKED_COLOR_A);
            if (sceneObj == movingObject) {
                controller.stopDrag();
                movingObject = null;
            }
        }

        public void onExit(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) {
            sceneObj.getRenderData().getMaterial().setVec4("u_color", UNPICKED_COLOR_R,
                    UNPICKED_COLOR_G, UNPICKED_COLOR_B,
                    UNPICKED_COLOR_A);
            if (sceneObj == movingObject) {
                controller.stopDrag();
                movingObject = null;
            }
            //mainScene.clear();
            listener.changeMainScene(mainScene);
            Log.i("sdnfcvdjcnkd ", "jhcgbdeked ffdv");
        }

        public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) {
        }

        public void onMotionOutside(GVRPicker p, MotionEvent e) {
        }
    };

    private GVRSceneObject getColorBoard() {
        GVRMaterial material = new GVRMaterial(mGVRContext, GVRShaderType.Color.ID);
        material.setVec4("u_color", UNPICKED_COLOR_R,
                UNPICKED_COLOR_G, UNPICKED_COLOR_B, UNPICKED_COLOR_A);
        GVRCubeSceneObject board = new GVRCubeSceneObject(mGVRContext);
        board.getRenderData().setMaterial(material);
        board.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.GEOMETRY);
        return board;
    }

    private GVRSceneObject getColorMesh(float scale, GVRMesh mesh) {
        GVRMaterial material = new GVRMaterial(mGVRContext, GVRShaderType.Color.ID);
        material.setVec4("u_color", UNPICKED_COLOR_R,
                UNPICKED_COLOR_G, UNPICKED_COLOR_B, UNPICKED_COLOR_A);

        GVRSceneObject meshObject = new GVRSceneObject(mGVRContext, mesh);
        meshObject.getTransform().setScale(scale, scale, scale);
        meshObject.getRenderData().setMaterial(material);
        meshObject.getRenderData().setRenderingOrder(GVRRenderData.GVRRenderingOrder.GEOMETRY);
        return meshObject;
    }

    private void attachMeshCollider(GVRSceneObject sceneObject) {
        sceneObject.attachComponent(new GVRMeshCollider(mGVRContext, false));
    }

    private void attachSphereCollider(GVRSceneObject sceneObject) {
        sceneObject.attachComponent(new GVRSphereCollider(mGVRContext));
    }

    private void attachBoundsCollider(GVRSceneObject sceneObject) {
        sceneObject.attachComponent(new GVRMeshCollider(mGVRContext, true));
    }


















    public class PickHandler implements IPickEvents {
        public GVRSceneObject PickedObject = null;

        public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) {
        }

        public void onExit(GVRSceneObject sceneObj) {
        }

        public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo) {
        }

        public void onNoPick(GVRPicker picker) {
            PickedObject = null;
        }

        public void onPick(GVRPicker picker) {
            GVRPicker.GVRPickedObject picked = picker.getPicked()[0];
            PickedObject = picked.hitObject;
        }
    }

    public GVRScene mScene = null;
    private PickHandler pickHandler;
    private ParticleEmitter mParticleSystem;
    private ArrayList<GVRMaterial> mMaterials;
    private GVRMesh mSphereMesh;
    private Random mRandom = new Random();
    private SoundPool mAudioEngine;
    private SoundEffect mPopSound;
    private GVRTextViewSceneObject mScoreBoard;
    private Integer mScore = 0;
    private GVRPicker mPicker;
    public GVRSceneObject environment;

    public void loadBallon(GVRContext context) {
        /*
         * Load the balloon popping sound
         */
        android.util.Log.i("mncvbj", "onInit: ");
        mAudioEngine = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        try {
            mPopSound = new SoundEffect(context, mAudioEngine, "pop.wav", false);
            mPopSound.setVolume(0.6f);
        } catch (IOException ex) {
            Log.e("Audio", "Cannot load pop.wav");
        }        /*
         * Set the background color
         */
        mScene = context.getMainScene();
        mScene.getMainCameraRig().getLeftCamera().setBackgroundColor(1.0f, 1.0f, 1.0f, 1.0f);
        mScene.getMainCameraRig().getRightCamera().setBackgroundColor(1.0f, 1.0f, 1.0f, 1.0f);

        /*
         * Set up a head-tracking pointer
         */
        GVRSceneObject headTracker = new GVRSceneObject(context,
                context.createQuad(0.1f, 0.1f),
                context.getAssetLoader().loadTexture(new GVRAndroidResource(context, R.drawable.headtrackingpointer)));
        headTracker.getTransform().setPosition(0.0f, 0.0f, -1.0f);
        headTracker.getRenderData().setDepthTest(false);
        headTracker.getRenderData().setRenderingOrder(100000);
        mScene.getMainCameraRig().addChildObject(headTracker);
        /*
         * Add the scoreboard
         */
        mScoreBoard = makeScoreboard(context, headTracker);
        /*
         * Add the environment
         */
        environment = makeEnvironment(context);
        mScene.addSceneObject(environment);
        /*
         * Make balloon prototype sphere mesh
         */
        mMaterials = makeMaterials(context);
        mSphereMesh = new GVRSphereSceneObject(context, true).getRenderData().getMesh();

        /*
         * Start the particle emitter making balloons
         */
        GVRSceneObject particleRoot = new GVRSceneObject(context);
        particleRoot.setName("ParticleSystem");
        ParticleEmitter.MakeParticle particleCreator = new ParticleEmitter.MakeParticle() {
            public GVRSceneObject create(GVRContext context) {
                return makeBalloon(context);
            }
        };
        mParticleSystem = new ParticleEmitter(context, mScene, particleCreator);
        mParticleSystem.MaxDistance = 10.0f;
        mParticleSystem.TotalParticles = 10;
        mParticleSystem.EmissionRate = 3;
        mParticleSystem.Velocity = new ParticleEmitter.Range<Float>(2.0f, 6.0f);
        mParticleSystem.EmitterArea = new ParticleEmitter.Range<Vector2f>(new Vector2f(-5.0f, -2.0f), new Vector2f(5.0f, 2.0f));
        particleRoot.getTransform().setRotationByAxis(-90.0f, 1, 0, 0);
        particleRoot.getTransform().setPosition(0, -3.0f, -3.0f);
        particleRoot.attachComponent(mParticleSystem);
        mScene.addSceneObject(particleRoot);
        /*
         * Respond to picking events
         */
        mPicker = new GVRPicker(context, mScene);
        pickHandler = new PickHandler();
        mScene.getEventReceiver().addListener(mPickHandler);
        /*
         * start the game timer
         */
        gameStart();
    }

    public void gameOver() {
        mParticleSystem.setEnable(false);
        mScoreBoard.getTransform().setPosition(0, 0, -1.0f);
        mScoreBoard.getCollider().setEnable(true);
        mScoreBoard.setTextSize(10.0f);
        mScoreBoard.setText(mScoreBoard.getTextString() + "\nTap to play again");
    }

    public void gameStart() {
        mScoreBoard.getTransform().setPosition(-1.2f, 1.2f, -2.2f);
        mScore = 0;
        float s = mScoreBoard.getTextSize();
        mScoreBoard.setTextSize(15.0f);
        mScoreBoard.setText("000");
        mScoreBoard.getCollider().setEnable(false);
        mParticleSystem.setEnable(true);
        Timer timer = new Timer();
        TimerTask gameOver = new TimerTask() {
            public void run() {
                gameOver();
            }
        };
        long oneMinute = 60 * 1000;
        timer.schedule(gameOver, oneMinute);
    }

    GVRSceneObject makeBalloon(GVRContext context) {
        GVRSceneObject balloon = new GVRSceneObject(context, mSphereMesh);
        GVRRenderData rdata = balloon.getRenderData();
        GVRSphereCollider collider = new GVRSphereCollider(context);
        Random rand = new Random();
        int mtlIndex = rand.nextInt(mMaterials.size() - 1);

        balloon.setName("balloon");
        rdata.setAlphaBlend(true);
        rdata.setMaterial(mMaterials.get(mtlIndex));
        rdata.setRenderingOrder(GVRRenderData.GVRRenderingOrder.TRANSPARENT);
        collider.setRadius(0.8f);
        balloon.attachComponent(collider);
        return balloon;
    }

    GVRSceneObject makeEnvironment(GVRContext context) {
        GVRTexture tex = context.getAssetLoader().loadCubemapTexture(new GVRAndroidResource(context, R.raw.lycksele3));
        GVRMaterial material = new GVRMaterial(context, GVRMaterial.GVRShaderType.Cubemap.ID);
        material.setMainTexture(tex);
        GVRSphereSceneObject environment = new GVRSphereSceneObject(context, 18, 36, false, material, 4, 4);
        environment.getTransform().setScale(20.0f, 20.0f, 20.0f);

        if (!GVRShader.isVulkanInstance()) {
            GVRDirectLight sunLight = new GVRDirectLight(context);
            sunLight.setAmbientIntensity(0.4f, 0.4f, 0.4f, 1.0f);
            sunLight.setDiffuseIntensity(0.6f, 0.6f, 0.6f, 1.0f);
            environment.attachComponent(sunLight);
        }
        return environment;
    }

    /*
     * Make an array of materials for the particles
     * so they will not all be the same.
     */
    ArrayList<GVRMaterial> makeMaterials(GVRContext ctx) {
        float[][] colors = new float[][]{
                {1.0f, 0.0f, 0.0f, 0.8f},
                {0.0f, 1.0f, 0.0f, 0.8f},
                {0.0f, 0.0f, 1.0f, 0.8f},
                {1.0f, 0.0f, 1.0f, 0.8f},
                {1.0f, 1.0f, 0.0f, 0.8f},
                {0.0f, 1.0f, 1.0f, 0.8f}
        };
        ArrayList<GVRMaterial> materials = new ArrayList<GVRMaterial>();
        for (int i = 0; i < 6; ++i) {
            GVRMaterial mtl = new GVRMaterial(ctx, GVRMaterial.GVRShaderType.Phong.ID);
            mtl.setDiffuseColor(colors[i][0], colors[i][1], colors[i][2], colors[i][3]);
            materials.add(mtl);
        }
        return materials;
    }

    /*
     * Make the scoreboard
     */
    GVRTextViewSceneObject makeScoreboard(GVRContext ctx, GVRSceneObject parent) {
        GVRTextViewSceneObject scoreBoard = new GVRTextViewSceneObject(ctx, 2.0f, 1.5f, "000");
        GVRRenderData rdata = scoreBoard.getRenderData();
        GVRCollider collider = new GVRMeshCollider(ctx, true);

        collider.setEnable(false);
        scoreBoard.attachComponent(collider);
        scoreBoard.setTextColor(Color.YELLOW);
        scoreBoard.setBackgroundColor(Color.argb(0, 0, 0, 0));
        scoreBoard.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        rdata.setDepthTest(false);
        rdata.setAlphaBlend(true);
        rdata.setRenderingOrder(GVRRenderData.GVRRenderingOrder.OVERLAY);
        GVRSceneObject boardFrame = null;
        try {
            boardFrame = ctx.getAssetLoader().loadModel("mirror.3ds");
            GVRSceneObject.BoundingVolume bv = boardFrame.getBoundingVolume();
            GVRTransform trans = boardFrame.getTransform();
            Matrix4f mtx = new Matrix4f();
            float sf = 1.5f / bv.radius;

            trans.setScale(sf, sf, sf);
            trans.rotateByAxis(-90.0f, 0, 1, 0);
            trans.rotateByAxis(90.0f, 0, 0, 1);
            bv = boardFrame.getBoundingVolume();
            trans.setPosition(-bv.center.x, -bv.center.y, -bv.center.z + 0.1f);
            scoreBoard.addChildObject(boardFrame);
        } catch (IOException ex) {
            Log.e("Balloons", "Cannot load scoreboard frame " + ex.getMessage());
        }
        parent.addChildObject(scoreBoard);
        return scoreBoard;
    }

    @Override
    public void onStep() {
        FPSCounter.tick();
    }

    public void onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (pickHandler.PickedObject != null) {
                    onHit(pickHandler.PickedObject);
                }
                break;

            default:
                break;
        }
    }

    private void onHit(GVRSceneObject sceneObj) {
        Particle particle = (Particle) sceneObj.getComponent(Particle.getComponentType());
        if (particle != null) {
            mPopSound.play();
            mParticleSystem.stop(particle);
            mScore += Math.round(particle.Velocity);
            mScoreBoard.setText(mScore.toString());
        } else if (sceneObj == mScoreBoard) {
            gameStart();
        }
    }








}
