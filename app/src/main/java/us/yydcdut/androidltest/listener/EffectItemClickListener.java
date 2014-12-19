package us.yydcdut.androidltest.listener;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.PopupWindow;

import us.yydcdut.androidltest.callback.PreviewSessionCallback;

/**
 * Created by yuyidong on 14-12-18.
 */
public class EffectItemClickListener implements AdapterView.OnItemClickListener {
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mCameraCaptureSession;
    private Handler mHandler;
    private PopupWindow mWindow;
    private PreviewSessionCallback mPreviewSessionCallback;

    public EffectItemClickListener(CaptureRequest.Builder mPreviewBuilder, CameraCaptureSession mCameraCaptureSession, Handler mHandler, PopupWindow mWindow, PreviewSessionCallback mPreviewSessionCallback) {
        this.mPreviewBuilder = mPreviewBuilder;
        this.mCameraCaptureSession = mCameraCaptureSession;
        this.mHandler = mHandler;
        this.mWindow = mWindow;
        this.mPreviewSessionCallback = mPreviewSessionCallback;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        switch (position) {
            case 0:
                mPreviewBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_AQUA);
                break;
            case 1:
                mPreviewBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_BLACKBOARD);
                break;
            case 2:
                mPreviewBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_MONO);
                break;
            case 3:
                mPreviewBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_NEGATIVE);
                break;
            case 4:
                mPreviewBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_POSTERIZE);
                break;
            case 5:
                mPreviewBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_SEPIA);
                break;
            case 6:
                mPreviewBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_SOLARIZE);
                break;
            case 7:
                mPreviewBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_WHITEBOARD);
                break;
            case 8:
                mPreviewBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_OFF);
                break;
        }
        updatePreview();
        mWindow.dismiss();
    }

    /**
     * 更新预览
     */
    private void updatePreview() {
        try {
            mCameraCaptureSession.setRepeatingRequest(mPreviewBuilder.build(), mPreviewSessionCallback, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("updatePreview", "ExceptionExceptionException");
        }
    }
}
