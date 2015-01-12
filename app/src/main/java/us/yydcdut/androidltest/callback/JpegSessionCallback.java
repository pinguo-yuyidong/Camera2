package us.yydcdut.androidltest.callback;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.MediaActionSound;
import android.os.Handler;
import android.util.Log;

import us.yydcdut.androidltest.ui.DisplayFragment;

/**
 * Created by yuyidong on 14-12-17.
 */
public class JpegSessionCallback extends CameraCaptureSession.CaptureCallback {
    private Handler mHandler;
    private MediaActionSound mMediaActionSound;
    private Handler mMainHanler;

    public JpegSessionCallback(Handler mHandler, MediaActionSound mMediaActionSound, Handler mMainHanler) {
        this.mHandler = mHandler;
        this.mMediaActionSound = mMediaActionSound;
        this.mMainHanler = mMainHanler;
    }

    @Override
    public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
        Log.i("JpegSessionCallback", "onCaptureCompleted");
        super.onCaptureCompleted(session, request, result);
        Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
        if (afState == null || aeState == null) {
            return;
        }
        Log.i("JpegSessionCallback", "要开始进入了,,afState.intValue()--->" + afState.intValue());
        //聚焦完成才能拍照
        if (afState.intValue() == CameraMetadata.CONTROL_AF_STATE_FOCUSED_LOCKED || afState.intValue() == CameraMetadata.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
            Log.i("JpegSessionCallback", "进去了一层,,aeState.intValue()--->" + aeState.intValue());
            if (aeState.intValue() == CameraMetadata.CONTROL_AE_STATE_LOCKED || aeState == CameraMetadata.CONTROL_AE_STATE_PRECAPTURE
                    || aeState.intValue() == CameraMetadata.CONTROL_AE_STATE_FLASH_REQUIRED) {
                Log.i("JpegSessionCallback", "进去了两层");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mMediaActionSound.play(MediaActionSound.SHUTTER_CLICK);
                    }
                });
            } else {
                mMainHanler.sendEmptyMessage(DisplayFragment.FOCUS_AGAIN);
            }
        } else {
            mMainHanler.sendEmptyMessage(DisplayFragment.FOCUS_AGAIN);
        }
    }
}
