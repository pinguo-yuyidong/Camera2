package us.yydcdut.androidltest.listener;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.SeekBar;
import android.widget.TextView;

import us.yydcdut.androidltest.R;
import us.yydcdut.androidltest.callback.PreviewSessionCallback;
import us.yydcdut.androidltest.ui.AwbSeekBar;

/**
 * Created by yuyidong on 14-12-18.
 */
public class AwbSeekBarChangeListener implements AwbSeekBar.OnAwbSeekBarChangeListener {
    private TextView mTextView;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mCameraCaptureSession;
    private Handler mHandler;
    private PreviewSessionCallback mPreviewSessionCallback;

    private Animation mAlphaInAnimation;
    private Animation mAlphaOutAnimation;

    public AwbSeekBarChangeListener(Context mContext, TextView mTextView, CaptureRequest.Builder mPreviewBuilder, CameraCaptureSession mCameraCaptureSession, Handler mHandler, PreviewSessionCallback mPreviewSessionCallback) {
        this.mTextView = mTextView;
        this.mPreviewBuilder = mPreviewBuilder;
        this.mCameraCaptureSession = mCameraCaptureSession;
        this.mHandler = mHandler;
        this.mPreviewSessionCallback = mPreviewSessionCallback;
        mAlphaInAnimation = AnimationUtils.loadAnimation(mContext, R.anim.alpha_in);
        mAlphaOutAnimation = AnimationUtils.loadAnimation(mContext, R.anim.alpha_out);
    }

    @Override
    public void doInProgress1() {
        mTextView.setText("自动");
        mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_AUTO);
        updatePreview();
    }

    @Override
    public void doInProgress2() {
        mTextView.setText("多云");
        mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT);
        updatePreview();
    }

    @Override
    public void doInProgress3() {
        mTextView.setText("白天");
        mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_DAYLIGHT);
        updatePreview();
    }

    @Override
    public void doInProgress4() {
        mTextView.setText("日光灯");
        mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_FLUORESCENT);
        updatePreview();
    }

    @Override
    public void doInProgress5() {
        mTextView.setText("白炽灯");
        mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_INCANDESCENT);
        updatePreview();
    }

    @Override
    public void doInProgress6() {
        mTextView.setText("阴影");
        mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_SHADE);
        updatePreview();
    }

    @Override
    public void doInProgress7() {
        mTextView.setText("黄昏");
        mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_TWILIGHT);
        updatePreview();
    }

    @Override
    public void doInProgress8() {
        mTextView.setText("暖光");
        mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_WARM_FLUORESCENT);
        updatePreview();
    }

    @Override
    public void onStopTrackingTouch(int num) {
        switch (num) {
            case 0:
                mTextView.setText("自动");
                mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_AUTO);
                break;
            case 10:
                mTextView.setText("多云");
                mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT);
                break;
            case 20:
                mTextView.setText("白天");
                mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_DAYLIGHT);
                break;
            case 30:
                mTextView.setText("日光灯");
                mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_FLUORESCENT);
                break;
            case 40:
                mTextView.setText("白炽灯");
                mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_INCANDESCENT);
                break;
            case 50:
                mTextView.setText("阴影");
                mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_SHADE);
                break;
            case 60:
                mTextView.setText("黄昏");
                mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_TWILIGHT);
                break;
            case 70:
                mTextView.setText("暖光");
                mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_WARM_FLUORESCENT);
                break;
        }
        updatePreview();
        mTextView.startAnimation(mAlphaOutAnimation);
        mTextView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mTextView.setVisibility(View.VISIBLE);
        mTextView.startAnimation(mAlphaInAnimation);
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
