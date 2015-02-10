package us.yydcdut.androidltest.otheractivity;

import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.util.Size;
import android.view.Surface;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import us.yydcdut.androidltest.R;

/**
 * Created by yuyidong on 14-12-18.
 */
public class FlashActivity extends Activity {
    private CameraCaptureSession mSession;
    private CaptureRequest.Builder mBuilder;
    private CameraDevice mCameraDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flash_activity);
        try {
            init();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Switch flashSwitch = (Switch) findViewById(R.id.switch_flash);
        //default of flash mode  is on
        flashSwitch.setChecked(true);
        flashSwitch.setOnCheckedChangeListener(new MyCheckedChangeListener());
    }

    private CameraManager mCameraManager;

    @SuppressWarnings("ResourceType")
    private void init() throws CameraAccessException {
        mCameraManager = (CameraManager) FlashActivity.this.getSystemService(Context.CAMERA_SERVICE);
        //here to judge if flash is available
        CameraCharacteristics cameraCharacteristics = mCameraManager.getCameraCharacteristics("0");
        boolean flashAvailable = cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
        if (flashAvailable) {
            mCameraManager.openCamera("0", new MyCameraDeviceStateCallback(), null);
        } else {
            Toast.makeText(FlashActivity.this, "Flash not available", Toast.LENGTH_SHORT).show();
        }
        mCameraManager.openCamera("0", new MyCameraDeviceStateCallback(), null);
    }

    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;

    /**
     * camera device callback
     */
    class MyCameraDeviceStateCallback extends CameraDevice.StateCallback {

        @Override
        public void onOpened(CameraDevice camera) {
            mCameraDevice = camera;
            //get builder
            try {
                mBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_MANUAL);
                //flash on, default is on
                mBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
                List<Surface> list = new ArrayList<Surface>();
                mSurfaceTexture = new SurfaceTexture(1);
                Size size = getSmallestSize(mCameraDevice.getId());
                mSurfaceTexture.setDefaultBufferSize(size.getWidth(), size.getHeight());
                mSurface = new Surface(mSurfaceTexture);
                list.add(mSurface);
                mBuilder.addTarget(mSurface);
                camera.createCaptureSession(list, new MyCameraCaptureSessionStateCallback(), null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(CameraDevice camera) {

        }

        @Override
        public void onError(CameraDevice camera, int error) {

        }
    }

    private Size getSmallestSize(String cameraId) throws CameraAccessException {
        Size[] outputSizes = mCameraManager.getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                .getOutputSizes(SurfaceTexture.class);
        if (outputSizes == null || outputSizes.length == 0) {
            throw new IllegalStateException(
                    "Camera " + cameraId + "doesn't support any outputSize.");
        }
        Size chosen = outputSizes[0];
        for (Size s : outputSizes) {
            if (chosen.getWidth() >= s.getWidth() && chosen.getHeight() >= s.getHeight()) {
                chosen = s;
            }
        }
        return chosen;
    }

    /**
     * session callback
     */
    class MyCameraCaptureSessionStateCallback extends CameraCaptureSession.StateCallback {

        @Override
        public void onConfigured(CameraCaptureSession session) {
            mSession = session;
            try {
                mSession.setRepeatingRequest(mBuilder.build(), null, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {

        }
    }

    /**
     * switch listener
     */
    class MyCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            try {
                if (isChecked) {
                    mBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AF_MODE_AUTO);
                    mBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
                    mSession.setRepeatingRequest(mBuilder.build(), null, null);
                } else {
                    mBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AF_MODE_AUTO);
                    mBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                    mSession.setRepeatingRequest(mBuilder.build(), null, null);
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void close() {
        if (mCameraDevice == null || mSession == null) {
            return;
        }
                mSession.close();
                mCameraDevice.close();
                mCameraDevice = null;
                mSession = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        close();
    }
}
