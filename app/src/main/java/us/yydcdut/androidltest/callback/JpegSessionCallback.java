package us.yydcdut.androidltest.callback;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureRequest;
import android.media.MediaActionSound;
import android.os.Handler;

/**
 * Created by yuyidong on 14-12-17.
 */
public class JpegSessionCallback extends CameraCaptureSession.CaptureCallback {
    private Handler mHandler;
    private MediaActionSound mMediaActionSound;

    public JpegSessionCallback(Handler mHandler, MediaActionSound mMediaActionSound) {
        this.mHandler = mHandler;
        this.mMediaActionSound = mMediaActionSound;
    }

    @Override
    public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
        super.onCaptureStarted(session, request, timestamp, frameNumber);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mMediaActionSound.play(MediaActionSound.SHUTTER_CLICK);
            }
        });
    }

}
