package us.yydcdut.androidltest.callback;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.util.Log;

/**
 * Created by yuyidong on 14-12-17.
 */
public class JpegSessionCallback extends CameraCaptureSession.CaptureCallback {
    @Override
    public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
        super.onCaptureCompleted(session, request, result);
        Log.i("JpegSessionCallback", "onCaptureCompleted,,,,onCaptureCompleted,,,,onCaptureCompleted");
    }


}
