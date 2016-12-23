package name.huliqing.editor;

import com.jme3.input.CameraInput;
import com.jme3.input.ChaseCamera;
import com.jme3.input.InputManager;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Spatial;
import java.util.logging.Logger;

/**
 *
 * @author huliqing
 */
public class EditorCamera extends ChaseCamera {

    private static final Logger LOG = Logger.getLogger(EditorCamera.class.getName());
    
    private boolean hRotationEnabled = true;
    private boolean vRotationEnabled = true;

    public EditorCamera(Camera cam, InputManager inputManager) {
        super(cam, inputManager);
        // 使用鼠标中键来实现旋转，和blender一样        
        setToggleRotationTrigger(new MouseButtonTrigger(MouseInput.BUTTON_MIDDLE));

        setSmoothMotion(false);
        setTrailingEnabled(false);
        setInvertVerticalAxis(true);
        setZoomSensitivity(3f);
        setRotationSpeed(5f);
        setRotationSensitivity(5);
        setMinDistance(0.0001f);
        setMaxDistance(Float.MAX_VALUE);
        setDefaultDistance(20);
        setChasingSensitivity(5);
        setDownRotateOnCloseViewOnly(true); 
        setUpVector(Vector3f.UNIT_Y);
        // 不要隐藏光标,否则在MAC系统下鼠标点击后会上下错位
        setHideCursorOnRotate(false);
        setEnableRotation(true);
    }
    
    /**
     * 设置要跟随的目标对象。
     * @param spatial 
     */
    public void setChase(Spatial spatial) {
        if (target != null) {
            target.removeControl(this);
        }
        spatial.addControl(this);
    }
    
    /**
     * 是否打开水平旋转
     * @param hRotation 
     */
    public void setEnableRotation(boolean hRotation) {
        hRotationEnabled = hRotation;
    }
    
    /**
     * 设置是否打开垂直旋转
     * @param vRotation 
     */
    public void setEnabledRotationV(boolean vRotation) {
        this.vRotationEnabled = vRotation;
    }

    //move the camera toward or away the target
    @Override
    protected void zoomCamera(float value) {
        if (!enabled) {
            return;
        }
        zooming = true;
        targetDistance += value * zoomSensitivity;
        if (targetDistance > maxDistance) {
            targetDistance = maxDistance;
        }
        if (targetDistance < minDistance) {
            targetDistance = minDistance;
        }
        
        // 让垂直旋转可以自由执行
//        if (veryCloseRotation) {
//            if ((targetVRotation < minVerticalRotation) && (targetDistance > (minDistance + 1.0f))) {
//                targetVRotation = minVerticalRotation;
//            }
//        }
    }
    
    @Override
    protected void rotateCamera(float value) {
        if (!hRotationEnabled) {
            return;
        }
        super.rotateCamera(value);
    }
    
    //rotate the camera around the target on the vertical plane
    @Override
    protected void vRotateCamera(float value) {
        if (!vRotationEnabled) {
            return;
        }
        if (!canRotate || !enabled) {
            return;
        }
        vRotating = true;
        float lastGoodRot = targetVRotation;
        targetVRotation += value * rotationSpeed;
        if (targetVRotation > maxVerticalRotation) {
            targetVRotation = lastGoodRot;
        }
        
        // 让垂直旋转可以自由执行
//        if (veryCloseRotation) {
//            if ((targetVRotation < minVerticalRotation) && (targetDistance > (minDistance + 1.0f))) {
//                targetVRotation = minVerticalRotation;
//            } else if (targetVRotation < -FastMath.DEG_TO_RAD * 90) {
//                targetVRotation = lastGoodRot;
//            }
//        } else {
//            if ((targetVRotation < minVerticalRotation)) {
//                targetVRotation = lastGoodRot;
//            }
//        }
        
    }
}