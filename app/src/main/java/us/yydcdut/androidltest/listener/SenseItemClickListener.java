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
 * Created by yuyidong on 14-12-23.
 */
public class SenseItemClickListener implements AdapterView.OnItemClickListener {
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mCameraCaptureSession;
    private Handler mHandler;
    private PopupWindow mWindow;
    private PreviewSessionCallback mPreviewSessionCallback;
    private AnimationTextView mAnimationTextView;


    public SenseItemClickListener(CaptureRequest.Builder mPreviewBuilder, CameraCaptureSession mCameraCaptureSession, Handler mHandler, PopupWindow mWindow, PreviewSessionCallback mPreviewSessionCallback, AnimationTextView mAnimationTextView) {
        this.mPreviewBuilder = mPreviewBuilder;
        this.mCameraCaptureSession = mCameraCaptureSession;
        this.mHandler = mHandler;
        this.mWindow = mWindow;
        this.mPreviewSessionCallback = mPreviewSessionCallback;
        this.mAnimationTextView = mAnimationTextView;

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                mPreviewBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_DISABLED);
                mAnimationTextView.start("DISABLED", DisplayFragment.WINDOW_TEXT_DISAPPEAR);
                break;
            case 1:
                mPreviewBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_FACE_PRIORITY);
                mAnimationTextView.start("FACE_PRIORITY", DisplayFragment.WINDOW_TEXT_DISAPPEAR);
                break;
            case 2:
                mPreviewBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_ACTION);
                mAnimationTextView.start("ACTION", DisplayFragment.WINDOW_TEXT_DISAPPEAR);
                break;
            case 3:
                mPreviewBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_PORTRAIT);
                mAnimationTextView.start("PORTRAIT", DisplayFragment.WINDOW_TEXT_DISAPPEAR);
                break;
            case 4:
                mPreviewBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_LANDSCAPE);
                mAnimationTextView.start("LANDSCAPE", DisplayFragment.WINDOW_TEXT_DISAPPEAR);
                break;
            case 5:
                mPreviewBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_NIGHT);
                mAnimationTextView.start("NIGHT", DisplayFragment.WINDOW_TEXT_DISAPPEAR);
                break;
            case 6:
                mPreviewBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_NIGHT_PORTRAIT);
                mAnimationTextView.start("PORTRAIT", DisplayFragment.WINDOW_TEXT_DISAPPEAR);
                break;
            case 7:
                mPreviewBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_THEATRE);
                mAnimationTextView.start("THEATRE", DisplayFragment.WINDOW_TEXT_DISAPPEAR);
                break;
            case 8:
                mPreviewBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_BEACH);
                mAnimationTextView.start("BEACH", DisplayFragment.WINDOW_TEXT_DISAPPEAR);
                break;
            case 9:
                mPreviewBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_SNOW);
                mAnimationTextView.start("SNOW", DisplayFragment.WINDOW_TEXT_DISAPPEAR);
                break;
            case 10:
                mPreviewBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_SUNSET);
                mAnimationTextView.start("SUNSET", DisplayFragment.WINDOW_TEXT_DISAPPEAR);
                break;
            case 11:
                mPreviewBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_STEADYPHOTO);
                mAnimationTextView.start("STEADYPHOTO", DisplayFragment.WINDOW_TEXT_DISAPPEAR);
                break;
            case 12:
                mPreviewBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_FIREWORKS);
                mAnimationTextView.start("FIREWORKS", DisplayFragment.WINDOW_TEXT_DISAPPEAR);
                break;
            case 13:
                mPreviewBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_SPORTS);
                mAnimationTextView.start("SPORTS", DisplayFragment.WINDOW_TEXT_DISAPPEAR);
                break;
            case 14:
                mPreviewBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_PARTY);
                mAnimationTextView.start("PARTY", DisplayFragment.WINDOW_TEXT_DISAPPEAR);
                break;
            case 15:
                mPreviewBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_CANDLELIGHT);
                mAnimationTextView.start("CANDLELIGHT", DisplayFragment.WINDOW_TEXT_DISAPPEAR);
                break;
            case 16:
                mPreviewBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, CameraMetadata.CONTROL_SCENE_MODE_BARCODE);
                mAnimationTextView.start("BARCODE", DisplayFragment.WINDOW_TEXT_DISAPPEAR);
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
