package us.yydcdut.androidltest.listener;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.PopupWindow;

import us.yydcdut.androidltest.R;
import us.yydcdut.androidltest.callback.PreviewSessionCallback;
import us.yydcdut.androidltest.ui.DisplayFragment;
import us.yydcdut.androidltest.view.AnimationTextView;

/**
 * Created by yuyidong on 14-12-18.
 */
public class FlashItemClickListener implements AdapterView.OnItemClickListener {
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mCameraCaptureSession;
    private Handler mHandler;
    private PopupWindow mWindow;
    private ImageView mBtnFlash;
    private PreviewSessionCallback mPreviewSessionCallback;
    private AnimationTextView mAnimationTextView;

    public FlashItemClickListener(CaptureRequest.Builder mPreviewBuilder, CameraCaptureSession mCameraCaptureSession, Handler mHandler, PopupWindow mWindow, ImageView mBtnFlash, PreviewSessionCallback mPreviewSessionCallback, AnimationTextView mAnimationTextView) {
        this.mPreviewBuilder = mPreviewBuilder;
        this.mCameraCaptureSession = mCameraCaptureSession;
        this.mHandler = mHandler;
        this.mWindow = mWindow;
        this.mBtnFlash = mBtnFlash;
        this.mPreviewSessionCallback = mPreviewSessionCallback;
        this.mAnimationTextView = mAnimationTextView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                mBtnFlash.setImageResource(R.drawable.btn_flash_off);
                mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
                mPreviewBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                mAnimationTextView.start("OFF", DisplayFragment.WINDOW_TEXT_DISAPPEAR);
                break;
            case 1:
                mBtnFlash.setImageResource(R.drawable.btn_flash_on);
                mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
                mPreviewBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_SINGLE);
                mAnimationTextView.start("SINGLE", DisplayFragment.WINDOW_TEXT_DISAPPEAR);
                break;
            case 2:
                mBtnFlash.setImageResource(R.drawable.btn_flash_all_on);
                mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
                mPreviewBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
                mAnimationTextView.start("TORCH", DisplayFragment.WINDOW_TEXT_DISAPPEAR);
                break;
            case 3:
                mBtnFlash.setImageResource(R.drawable.btn_flash_auto);
                mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH);
                mAnimationTextView.start("AUTO_FLASH", DisplayFragment.WINDOW_TEXT_DISAPPEAR);
                break;
            case 4:
                mBtnFlash.setImageResource(R.drawable.btn_flash_auto);
                mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON_ALWAYS_FLASH);
                mAnimationTextView.start("ALWAYS_FLASH", DisplayFragment.WINDOW_TEXT_DISAPPEAR);
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
