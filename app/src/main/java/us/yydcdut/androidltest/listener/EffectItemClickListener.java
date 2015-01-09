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
import us.yydcdut.androidltest.ui.DisplayFragment;
import us.yydcdut.androidltest.view.AnimationTextView;

/**
 * Created by yuyidong on 14-12-18.
 */
public class EffectItemClickListener implements AdapterView.OnItemClickListener {
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mCameraCaptureSession;
    private Handler mHandler;
    private PopupWindow mWindow;
    private PreviewSessionCallback mPreviewSessionCallback;
    private AnimationTextView mAnimationTextView;

    public EffectItemClickListener(CaptureRequest.Builder mPreviewBuilder, CameraCaptureSession mCameraCaptureSession, Handler mHandler, PopupWindow mWindow, PreviewSessionCallback mPreviewSessionCallback, AnimationTextView mAnimationTextView) {
        this.mPreviewBuilder = mPreviewBuilder;
        this.mCameraCaptureSession = mCameraCaptureSession;
        this.mHandler = mHandler;
        this.mWindow = mWindow;
        this.mPreviewSessionCallback = mPreviewSessionCallback;
        this.mAnimationTextView = mAnimationTextView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        switch (position) {
            case 0:
                mPreviewBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_AQUA);
                mAnimationTextView.start("AQUA", DisplayFragment.WINDOW_TEXT_DISAPPEAR);
                break;
            case 1:
                mPreviewBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_BLACKBOARD);
                mAnimationTextView.start("BLACKBOARD", DisplayFragment.WINDOW_TEXT_DISAPPEAR);
                break;
            case 2:
                mPreviewBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_MONO);
                mAnimationTextView.start("MONO", DisplayFragment.WINDOW_TEXT_DISAPPEAR);
                break;
            case 3:
                mPreviewBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_NEGATIVE);
                mAnimationTextView.start("NEGATIVE", DisplayFragment.WINDOW_TEXT_DISAPPEAR);
                break;
            case 4:
                mPreviewBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_POSTERIZE);
                mAnimationTextView.start("POSTERIZE", DisplayFragment.WINDOW_TEXT_DISAPPEAR);
                break;
            case 5:
                mPreviewBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_SEPIA);
                mAnimationTextView.start("SEPIA", DisplayFragment.WINDOW_TEXT_DISAPPEAR);
                break;
            case 6:
                mPreviewBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_SOLARIZE);
                mAnimationTextView.start("SOLARIZE", DisplayFragment.WINDOW_TEXT_DISAPPEAR);
                break;
            case 7:
                mPreviewBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_WHITEBOARD);
                mAnimationTextView.start("WHITEBOARD", DisplayFragment.WINDOW_TEXT_DISAPPEAR);
                break;
            case 8:
                mPreviewBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_OFF);
                mAnimationTextView.start("OFF", DisplayFragment.WINDOW_TEXT_DISAPPEAR);
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
