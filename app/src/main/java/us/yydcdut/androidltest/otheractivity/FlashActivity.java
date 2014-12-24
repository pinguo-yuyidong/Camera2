package us.yydcdut.androidltest.otheractivity;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.media.ImageReader;
import android.os.Bundle;
import android.view.Surface;
import android.widget.CompoundButton;
import android.widget.Switch;

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


    @SuppressWarnings("ResourceType")
    private void init() throws CameraAccessException {
        CameraManager cameraManager = (CameraManager) FlashActivity.this.getSystemService(Context.CAMERA_SERVICE);
       /*//here to judge if flash is available
        CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics("0");
        boolean flashAvailable = cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
        if (flashAvailable) {
            cameraManager.openCamera("0", new MyCameraDeviceStateCallback(), null);
        } else {
            Toast.makeText(FlashActivity.this, "Flash not available", Toast.LENGTH_SHORT).show();
        }*/
        cameraManager.openCamera("0", new MyCameraDeviceStateCallback(), null);
    }

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
                ImageReader imageReader = ImageReader.newInstance(800, 600, ImageFormat.JPEG, 5);
                list.add(imageReader.getSurface());
                mBuilder.addTarget(imageReader.getSurface());
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
                    /*// it doesn't work
                    mBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
                    mSession.setRepeatingRequest(mBuilder.build(), null, null);*/
                    init();
                } else {
                    /*// it doesn't work
                    mBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_SINGLE);
                    mSession.setRepeatingRequest(mBuilder.build(), null, null);*/
                    close();
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
        new Thread() {
            @Override
            public void run() {
                super.run();
                mSession.close();
                mCameraDevice.close();
                mCameraDevice = null;
                mSession = null;
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        close();
    }
}
