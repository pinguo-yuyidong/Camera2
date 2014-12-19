package us.yydcdut.androidltest.listener;

import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.MeteringRectangle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;

import us.yydcdut.androidltest.callback.PreviewSessionCallback;
import us.yydcdut.androidltest.ui.MyTextureView;

/**
 * Created by yuyidong on 14-12-18.
 */
public class TextureViewTouchEvent implements MyTextureView.MyTextureViewTouchEvent {
    private CameraCharacteristics mCameraCharacteristics;
    private TextureView mTextureView;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mCameraCaptureSession;
    private Handler mHandler;
    private PreviewSessionCallback mPreviewSessionCallback;

    public TextureViewTouchEvent(CameraCharacteristics mCameraCharacteristics, TextureView mTextureView, CaptureRequest.Builder mPreviewBuilder, CameraCaptureSession mCameraCaptureSession, Handler mHandler, PreviewSessionCallback mPreviewSessionCallback) {
        this.mCameraCharacteristics = mCameraCharacteristics;
        this.mTextureView = mTextureView;
        this.mPreviewBuilder = mPreviewBuilder;
        this.mCameraCaptureSession = mCameraCaptureSession;
        this.mHandler = mHandler;
        this.mPreviewSessionCallback = mPreviewSessionCallback;
    }

    @Override
    public boolean onAreaTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Rect rect = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
                int areaSize = 200;
                int right = rect.right;
                int bottom = rect.bottom;
                int viewWidth = mTextureView.getWidth();
                int viewHeight = mTextureView.getHeight();
                int ll, rr;
                Rect newRect;
                int centerX = (int) event.getX();
                int centerY = (int) event.getY();
                ll = ((centerX * right) - areaSize) / viewWidth;
                rr = ((centerY * bottom) - areaSize) / viewHeight;
                int focusLeft = clamp(ll, 0, right);
                int focusBottom = clamp(rr, 0, bottom);
                Log.i("focus_position", "focusLeft--->" + focusLeft + ",,,focusTop--->" + focusBottom + ",,,focusRight--->" + (focusLeft + areaSize) + ",,,focusBottom--->" + (focusBottom + areaSize));
                newRect = new Rect(focusLeft, focusBottom, focusLeft + areaSize, focusBottom + areaSize);
                MeteringRectangle meteringRectangle = new MeteringRectangle(newRect, 500);
                MeteringRectangle[] meteringRectangleArr = {meteringRectangle};
                mPreviewBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, meteringRectangleArr);
                mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
                mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
                mPreviewBuilder.set(CaptureRequest.CONTROL_AE_REGIONS, meteringRectangleArr);
                mPreviewBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START);
                updatePreview();
                break;
            case MotionEvent.ACTION_UP:
                int aeRegions = mCameraCharacteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AE);
                Log.i("ACTION_UP", "CONTROL_MAX_REGIONS_AE--->" + aeRegions);
                int afRegions = mCameraCharacteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF);
                Log.i("ACTION_UP", "CONTROL_MAX_REGIONS_AF--->" + afRegions);
                break;
        }
        return true;
    }

    /**
     * 触摸对焦的计算
     *
     * @param x
     * @param min
     * @param max
     * @return
     */
    private int clamp(int x, int min, int max) {
        if (x < min) {
            return min;
        } else if (x > max) {
            return max;
        } else {
            return x;
        }
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
