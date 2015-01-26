package us.yydcdut.camera2.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.renderscript.RenderScript;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import us.yydcdut.camera2.CompareSizesByArea;
import us.yydcdut.camera2.Progress;
import us.yydcdut.camera2.R;
import us.yydcdut.camera2.view.AutoFitTextureView;

/**
 * Created by yuyidong on 15-1-21.
 */
public class YUVCameraFragment extends Fragment implements Progress.GetFrameBitmap {
    private AutoFitTextureView mPreviewView;
    private Size mPreviewSize;
    private String mCameraId;
    private CaptureRequest.Builder mBuilder;
    private RenderScript mRS;
    private Progress mProcessor;

    private Bitmap mFrameBitmap;
    private ImageView mframeImage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.preview_fragment, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPreviewView = (AutoFitTextureView) view.findViewById(R.id.texture);
        view.findViewById(R.id.btn_capture).setVisibility(View.GONE);
        view.findViewById(R.id.btn_setting).setVisibility(View.GONE);
        mframeImage = (ImageView) view.findViewById(R.id.view);
        mPreviewView.setSurfaceTextureListener(mSurfaceTextureListener);
    }

    private Surface mInputSurface;
    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            setUpCameraOutputs(width, height);
            configureTransform(width, height);
            mRS = RenderScript.create(getActivity());
            mProcessor = new Progress(mRS, mPreviewSize, YUVCameraFragment.this);

            SurfaceTexture texture = mPreviewView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mPreviewSurface = new Surface(texture);

            mProcessor.setOutputSurface(mPreviewSurface);
            mInputSurface = mProcessor.getInputNormalSurface();

            CameraManager cameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
            try {
                cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };


    private void setUpCameraOutputs(int width, int height) {
        Activity activity = getActivity();
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {

            String cameraId = "0";
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new CompareSizesByArea());
            mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), width, height, largest);
            int orientation = getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                mPreviewView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            } else {
                mPreviewView.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
            }
            mCameraId = cameraId;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
        }
    }

    private Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
        List<Size> bigEnough = new ArrayList<Size>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            return choices[0];
        }
    }

    private void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = getActivity();
        if (null == mPreviewView || null == mPreviewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        mPreviewView.setTransform(matrix);
    }


    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice camera) {
            try {
                createSession(camera);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(CameraDevice camera) {

        }

        @Override
        public void onError(CameraDevice camera, int error) {

        }
    };
    private Surface mPreviewSurface;

    private void createSession(CameraDevice camera) throws CameraAccessException {
        mBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        mBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        mBuilder.addTarget(mInputSurface);
        camera.createCaptureSession(Arrays.asList(mInputSurface), mCameraCaptureSessionStateCallback, null);
    }

    private CameraCaptureSession.StateCallback mCameraCaptureSessionStateCallback = new CameraCaptureSession.StateCallback() {

        @Override
        public void onConfigured(CameraCaptureSession session) {
            try {
                session.setRepeatingRequest(mBuilder.build(), mCameraCaptureSessionCaptureCallback, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {
        }
    };

    private CameraCaptureSession.CaptureCallback mCameraCaptureSessionCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            if (mframeImage == null) {
                return;
            }
            mframeImage.setImageBitmap(mFrameBitmap);
        }
    };


    @Override
    public void getBitmap(Bitmap bitmap) {
        mFrameBitmap = bitmap;
    }
}

