package us.yydcdut.androidltest.callback;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import us.yydcdut.androidltest.ui.AnimationImageView;
import us.yydcdut.androidltest.ui.MyTextureView;

/**
 * Created by yuyidong on 14-12-19.
 */
public class PreviewSessionCallback extends CameraCaptureSession.CaptureCallback implements MyTextureView.FocusPositionTouchEvent {
    private int mAfState = CameraMetadata.CONTROL_AF_STATE_INACTIVE;
    private AnimationImageView mFocusImage;
    private Handler mMainHandler;
    private int mRawX;
    private int mRawY;
    private boolean mFlagShowFocusImage = false;

    public PreviewSessionCallback(AnimationImageView mFocusImage, Handler mMainHandler, MyTextureView mMyTextureView) {
        this.mFocusImage = mFocusImage;
        this.mMainHandler = mMainHandler;
        mMyTextureView.setmFocusPositionTouchEvent(this);
    }

    @Override
    public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, final TotalCaptureResult result) {
        super.onCaptureCompleted(session, request, result);
        //这样就可以操作focus的imageview了
        Integer nowAfState = result.get(CaptureResult.CONTROL_AF_STATE);
        //获取失败
        if (nowAfState == null) {
            return;
        }
        //这次的值与之前的一样，忽略掉
        if (nowAfState.intValue() == mAfState) {
            return;
        }
        mAfState = nowAfState.intValue();
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                judgeFocus();
            }
        });
    }

    private void judgeFocus() {
        switch (mAfState) {
            case CameraMetadata.CONTROL_AF_STATE_ACTIVE_SCAN:
                //得到宽高
                int width = mFocusImage.getWidth();
                int height = mFocusImage.getHeight();
                //居中
                ViewGroup.MarginLayoutParams margin = new ViewGroup.MarginLayoutParams(mFocusImage.getLayoutParams());
                margin.setMargins(mRawX - width / 2, mRawY - height / 2, margin.rightMargin, margin.bottomMargin);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(margin);
                mFocusImage.setLayoutParams(layoutParams);
                //显示
                if (mFlagShowFocusImage == false) {
                    mFocusImage.startFocusing();
                    mFlagShowFocusImage = true;
                }
                break;
            case CameraMetadata.CONTROL_AF_STATE_FOCUSED_LOCKED:
                if (mFlagShowFocusImage == true) {
                    mFocusImage.focusSuccess();
                    mFlagShowFocusImage = false;
                }
                break;
            case CameraMetadata.CONTROL_AF_STATE_INACTIVE:
                mFocusImage.stopFocus();
                mFlagShowFocusImage = false;
                break;
            case CameraMetadata.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED:
                if (mFlagShowFocusImage == true) {
                    mFocusImage.focusFailed();
                    mFlagShowFocusImage = false;
                }
                break;
        }
    }

    @Override
    public void getPosition(MotionEvent event) {
        mRawX = (int) event.getRawX();
        mRawY = (int) event.getRawY();
    }

}
