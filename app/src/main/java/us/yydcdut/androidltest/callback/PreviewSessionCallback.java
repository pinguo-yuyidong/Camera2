package us.yydcdut.androidltest.callback;

import android.content.Context;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import us.yydcdut.androidltest.R;
import us.yydcdut.androidltest.SleepThread;
import us.yydcdut.androidltest.ui.DisplayFragment;
import us.yydcdut.androidltest.ui.MyTextureView;

/**
 * Created by yuyidong on 14-12-19.
 */
public class PreviewSessionCallback extends CameraCaptureSession.CaptureCallback implements MyTextureView.FocusPositionTouchEvent {
    private int mAfState = CameraMetadata.CONTROL_AF_STATE_INACTIVE;
    private ImageView mFocusImage;
    private Context mContext;
    private Handler mHandler;
    private int mX;
    private int mY;
    private boolean mFlagShowFocusImage = false;

    public PreviewSessionCallback(ImageView mFocusImage, Context mContext, Handler mHandler, MyTextureView mMyTextureView) {
        this.mFocusImage = mFocusImage;
        this.mContext = mContext;
        this.mHandler = mHandler;
        mMyTextureView.setmFocusPositionTouchEvent(this);
    }

    @Override
    public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, final TotalCaptureResult result) {
        super.onCaptureCompleted(session, request, result);
        //这样就可以操作focus的imageview了
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                judgeFocus(result);
            }
        });
    }

    private void judgeFocus(CaptureResult result) {
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
        switch (mAfState) {
            case CameraMetadata.CONTROL_AF_STATE_ACTIVE_SCAN:
                //得到宽高
                int width = mFocusImage.getWidth();
                int height = mFocusImage.getHeight();
                //居中
                ViewGroup.MarginLayoutParams margin = new ViewGroup.MarginLayoutParams(mFocusImage.getLayoutParams());
                margin.setMargins(mX - width / 2, mY - height / 2, margin.rightMargin, margin.bottomMargin);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(margin);
                mFocusImage.setLayoutParams(layoutParams);
                //显示
                if (mFlagShowFocusImage == false) {
                    mFocusImage.setVisibility(View.VISIBLE);
                    mFocusImage.setBackground(mContext.getDrawable(R.drawable.focus));
                    mFlagShowFocusImage = true;
                }
                break;
            case CameraMetadata.CONTROL_AF_STATE_FOCUSED_LOCKED:
                if (mFlagShowFocusImage == true) {
                    mFocusImage.setVisibility(View.VISIBLE);
                    mFocusImage.setBackground(mContext.getDrawable(R.drawable.focus_succeed));
                    new Thread(new SleepThread(mHandler, DisplayFragment.FOCUS_DISAPPEAR, 800)).start();
                    mFlagShowFocusImage = false;
                }
                break;
            case CameraMetadata.CONTROL_AF_STATE_INACTIVE:
                mFocusImage.setVisibility(View.VISIBLE);
                mFlagShowFocusImage = false;
                break;
            case CameraMetadata.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED:
                if (mFlagShowFocusImage == true) {
                    mFocusImage.setBackground(mContext.getDrawable(R.drawable.focus_failed));
                    new Thread(new SleepThread(mHandler, DisplayFragment.FOCUS_DISAPPEAR, 800)).start();
                    mFlagShowFocusImage = false;
                }
                break;
        }
    }

    @Override
    public void getPosition(MotionEvent event) {
        mX = (int) event.getX();
        mY = (int) event.getY();
    }
}
