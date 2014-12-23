package us.yydcdut.androidltest.callback;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.DngCreator;
import android.hardware.camera2.TotalCaptureResult;
import android.media.ImageReader;
import android.media.MediaActionSound;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * Created by yuyidong on 14-12-17.
 */
@SuppressWarnings("ALL")
public class DngSessionCallback extends CameraCaptureSession.CaptureCallback {

    private Context mContext;
    private String mCameraId;
    private ImageReader mImageReader;
    private Handler mHandler;
    private MediaActionSound mMediaActionSound;

    public DngSessionCallback(Context mContext, String mCameraId, ImageReader mImageReader, Handler mHandler, MediaActionSound mMediaActionSound) {
        this.mContext = mContext;
        this.mCameraId = mCameraId;
        this.mImageReader = mImageReader;
        this.mHandler = mHandler;
        this.mMediaActionSound = mMediaActionSound;
    }

    @Override
    public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
        super.onCaptureCompleted(session, request, result);
        Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
        if (afState == null || aeState == null) {
            return;
        }
        Log.i("DngSessionCallback", "要开始进入了,,afState.intValue()--->" + afState.intValue());
        //聚焦完成才能拍照
        if (afState.intValue() == CameraMetadata.CONTROL_AF_STATE_FOCUSED_LOCKED || afState.intValue() == CameraMetadata.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
            Log.i("DngSessionCallback", "进去了一层,,aeState.intValue()--->" + aeState.intValue());
            if (aeState.intValue() == CameraMetadata.CONTROL_AE_STATE_LOCKED || aeState == CameraMetadata.CONTROL_AE_STATE_CONVERGED || aeState == CameraMetadata.CONTROL_AE_STATE_PRECAPTURE || aeState == CameraMetadata.CONTROL_AE_STATE_FLASH_REQUIRED) {
                Log.i("DngSessionCallback", "进去了两层");
//                mHandler.post(new DngRunnable(result));
                new Thread(new DngRunnable(result)).start();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mMediaActionSound.play(MediaActionSound.SHUTTER_CLICK);
                    }
                });
            }
        }

    }

    class DngRunnable implements Runnable {
        private TotalCaptureResult mResult;

        DngRunnable(TotalCaptureResult mResult) {
            this.mResult = mResult;
        }

        @Override
        public void run() {
            DngCreator dngCreator = null;
            CameraManager manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
            try {
                CameraCharacteristics cameraCharacteristics = manager.getCameraCharacteristics(mCameraId);
                dngCreator = new DngCreator(cameraCharacteristics, mResult);
                dngCreator.writeImage(new FileOutputStream(createFile()), mImageReader.acquireNextImage());
            } catch (CameraAccessException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("DngRunnable", "Exception eException eException e");
            } finally {
                try {
                    dngCreator.close();
                } catch (Exception e) {
                    Log.e("DngRunnable", "dngCreator.close();dngCreator.close();");
                }
            }
        }

        private File createFile() {
            File dir = new File(Environment.getExternalStorageDirectory() + "/Android_L_Test/");
            if (!dir.exists()) {
                dir.mkdir();
            }
            long time = System.currentTimeMillis();
            int random = new Random().nextInt(1000);
            File dir1 = new File(Environment.getExternalStorageDirectory() + "/Android_L_Test/dng/");
            if (!dir1.exists()) {
                dir1.mkdir();
            }
            File meidaFile = new File(dir1, time + "_" + random + ".dng");
            return meidaFile;
        }

    }
}
