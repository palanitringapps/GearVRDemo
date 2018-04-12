package tringapps.com.gearvrdemoifg;

import android.graphics.Color;

import org.gearvrf.GVRPicker;
import org.gearvrf.GVRSceneObject;
import org.gearvrf.IPickEvents;

public class PickerHandler implements IPickEvents {
    @Override
    public void onPick(GVRPicker picker) {
        GVRPicker.GVRPickedObject picked = picker.getPicked()[0];
        picketObject = picked.hitObject;
        picketObject.getRenderData().getMaterial().setDiffuseColor(1.0f, 1.0f, 0.0f, 0.5f);
    }

    @Override
    public void onNoPick(GVRPicker picker) {
        if (picketObject != null) {
            picketObject.getRenderData().getMaterial().setDiffuseColor(1.0f, 0.0f, 0.0f, 0.5f);
        }
        picketObject = null;
    }

    /*public void onTouchStart(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject pickInfo)
    {
        if (picketObject == null)
        {
            sceneObj.getRenderData().getMaterial().setColor(Color.BLUE);
            if (mController.startDrag(sceneObj))
            {
                picketObject = sceneObj;
            }
        }
    }*/

    public GVRSceneObject getPicketObject() {
        return picketObject;
    }

    @Override
    public void onEnter(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) {
        sceneObj.getRenderData().getMaterial().setColor(Color.RED);
    }

    @Override
    public void onExit(GVRSceneObject sceneObj) {

    }

    @Override
    public void onInside(GVRSceneObject sceneObj, GVRPicker.GVRPickedObject collision) {

    }

    public GVRSceneObject picketObject = null;
}
