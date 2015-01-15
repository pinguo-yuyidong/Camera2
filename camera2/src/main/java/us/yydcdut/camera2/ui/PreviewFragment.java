package us.yydcdut.camera2.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
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
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.media.MediaActionSound;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import us.yydcdut.camera2.CompareSizesByArea;
import us.yydcdut.camera2.DngImageSaver;
import us.yydcdut.camera2.ImageSaver;
import us.yydcdut.camera2.PreferenceHelper;
import us.yydcdut.camera2.R;
import us.yydcdut.camera2.view.AutoFitTextureView;

/**
 * Created by yuyidong on 15-1-8.
 */
public class PreviewFragment extends Fragment implements View.OnClickListener {
    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAITING_CAPTURE = 1;
    private static final int STATE_TRY_CAPTURE_AGAIN = 2;
    private static final int STATE_TRY_DO_CAPTURE = 3;
    private int mState = STATE_PREVIEW;
    private Handler mPreviewHandler;
    private HandlerThread mHandlerThread;
    private AutoFitTextureView mPreviewView;
    private ImageReader mImageReader;
    private Size mPreviewSize;
    private String mCameraId;
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mSession;
    private ImageView mBtnCapture;
    private MediaActionSound mMediaActionSound;
    private Surface mSurface;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    public static PreviewFragment newInstance() {
        PreviewFragment fragment = new PreviewFragment();
        fragment.setRetainInstance(true);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initShutter();
        return inflater.inflate(R.layout.preview_fragment, null);
    }

    private void initShutter() {
        mMediaActionSound = new MediaActionSound();
        mMediaActionSound.load(MediaActionSound.SHUTTER_CLICK);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPreviewView = (AutoFitTextureView) view.findViewById(R.id.texture);
        mBtnCapture = (ImageView) view.findViewById(R.id.btn_capture);
        mBtnCapture.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        try {
                            takePicture();
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        try {
                            updateCameraPreviewSession();
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                        break;
                }
                return true;
            }
        });
        view.findViewById(R.id.btn_setting).setOnClickListener(this);
    }

    private void takePicture() throws CameraAccessException {
        mState = STATE_WAITING_CAPTURE;
        Log.i("format", PreferenceHelper.getCameraFormat(getActivity()));
        if (PreferenceHelper.getCameraFormat(getActivity()).equals("JPEG")) {
            mSession.setRepeatingRequest(mPreviewBuilder.build(), mSessionCaptureCallback, mPreviewHandler);
        } else if (PreferenceHelper.getCameraFormat(getActivity()).equals("DNG")) {
            mSession.setRepeatingRequest(initDngBuilder().build(), mSessionDngCaptureCallback, mPreviewHandler);
        } else {
            mSession.setRepeatingRequest(mPreviewBuilder.build(), mSessionCaptureCallback, mPreviewHandler);
        }
    }

    private CaptureRequest.Builder initDngBuilder() {
        CaptureRequest.Builder captureBuilder = null;
        try {
            captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);

            captureBuilder.addTarget(mImageReader.getSurface());
            // Required for RAW capture
            captureBuilder.set(CaptureRequest.STATISTICS_LENS_SHADING_MAP_MODE, CaptureRequest.STATISTICS_LENS_SHADING_MAP_MODE_ON);
            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            captureBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, (long) ((214735991 - 13231) / 2));
            captureBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, 0);
            captureBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, (10000 - 100) / 2);//设置 ISO，感光度
            //设置每秒30帧
            CameraManager cameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(mCameraId);
            Range<Integer> fps[] = cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
            captureBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, fps[fps.length - 1]);
        } catch (CameraAccessException e) {
            Log.i("initDngBuilder", "initDngBuilder");
            e.printStackTrace();
        }
        return captureBuilder;
    }

    private void initLooper() {
        mHandlerThread = new HandlerThread("Camera_2");
        mHandlerThread.start();
        mPreviewHandler = new Handler(mHandlerThread.getLooper());
    }

    @Override
    public void onResume() {
        super.onResume();
        initLooper();
        if (mPreviewView.isAvailable()) {
            openCamera(mPreviewView.getWidth(), mPreviewView.getHeight());
        } else {
            mPreviewView.setSurfaceTextureListener(mSurfaceTextureListener);
        }

    }

    private void openCamera(int width, int height) {
        setUpCameraOutputs(width, height);
        configureTransform(width, height);
        Activity activity = getActivity();
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            manager.openCamera(mCameraId, DeviceStateCallback, mPreviewHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera opening.", e);
        }
    }

    private void setUpCameraOutputs(int width, int height) {
        Activity activity = getActivity();
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {

            String cameraId = PreferenceHelper.getCameraId(getActivity());
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size largest;
            if (PreferenceHelper.getCameraFormat(getActivity()).equals("JPEG")) {
                largest = Collections.max(Arrays.asList(map.getOutputSizes(android.graphics.ImageFormat.JPEG)), new CompareSizesByArea());
                initImageReader(largest, ImageFormat.JPEG);
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), width, height, largest);
            } else {
                largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.RAW_SENSOR)), new CompareSizesByArea());
                initImageReader(largest, ImageFormat.RAW_SENSOR);
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), width, height, largest);
            }

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

    private void initImageReader(Size size, int format) {
        mImageReader = ImageReader.newInstance(size.getWidth(), size.getHeight(), format, /*maxImages*/7);
        if (format == ImageFormat.JPEG) {
            mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mPreviewHandler);
        } else {
            mImageReader.setOnImageAvailableListener(mRawOnImageAvailableListener, mPreviewHandler);
        }
    }

    private ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            new Thread(new ImageSaver(reader)).start();
            mMediaActionSound.play(MediaActionSound.SHUTTER_CLICK);
        }
    };
    //    private Image mRawImage;
    private ImageReader.OnImageAvailableListener mRawOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
//            mRawImage = reader.acquireNextImage();
        }
    };

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


    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

    private CameraDevice.StateCallback DeviceStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice camera) {
            mCameraOpenCloseLock.release();
            mCameraDevice = camera;
            try {
                createCameraPreviewSession();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            mCameraOpenCloseLock.release();
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            mCameraOpenCloseLock.release();
            camera.close();
            mCameraDevice = null;
            Toast.makeText(getActivity(), "onError,error--->" + error, Toast.LENGTH_SHORT).show();
        }
    };

    private void initSurface() {
        SurfaceTexture texture = mPreviewView.getSurfaceTexture();
        assert texture != null;
        texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        mSurface = new Surface(texture);
    }

    private void createCameraPreviewSession() throws CameraAccessException {
        initSurface();
        mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        mPreviewBuilder.addTarget(mSurface);
        mState = STATE_PREVIEW;
        mCameraDevice.createCaptureSession(Arrays.asList(mSurface, mImageReader.getSurface()), mSessionPreviewStateCallback, mPreviewHandler);
    }

    private CameraCaptureSession.StateCallback mSessionPreviewStateCallback = new CameraCaptureSession.StateCallback() {

        @Override
        public void onConfigured(CameraCaptureSession session) {
            if (null == mCameraDevice) {
                return;
            }
            mSession = session;
            try {
                mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                session.setRepeatingRequest(mPreviewBuilder.build(), mSessionCaptureCallback, mPreviewHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {
            Activity activity = getActivity();
            if (null != activity) {
                Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void updateCameraPreviewSession() throws CameraAccessException {
        mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
        mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
        mState = STATE_PREVIEW;
        mSession.setRepeatingRequest(mPreviewBuilder.build(), mSessionCaptureCallback, mPreviewHandler);
    }

    private CameraCaptureSession.CaptureCallback mSessionDngCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            Log.i("mSessionDngCaptureCallback", "mSessionDngCaptureCallback");
            takeDngPicture(result);
            mMediaActionSound.play(MediaActionSound.SHUTTER_CLICK);
        }
    };

    private CameraCaptureSession.CaptureCallback mSessionCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            mSession = session;
            if (!PreferenceHelper.getCameraFormat(getActivity()).equals("DNG")) {
                checkState(result);
            }
        }

        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
            mSession = session;
            if (!PreferenceHelper.getCameraFormat(getActivity()).equals("DNG")) {
                checkState(partialResult);
            }
        }

        private void checkState(CaptureResult result) {
            switch (mState) {
                case STATE_PREVIEW:
                    // NOTHING
                    break;
                case STATE_WAITING_CAPTURE:
                    int afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    Log.i("checkState", "afState--->" + afState);
                    if (CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED == afState || CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState
                            || CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED == afState || CaptureResult.CONTROL_AF_STATE_PASSIVE_UNFOCUSED == afState) {
                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                        Log.i("checkState", "进来了一层,aeState--->" + aeState);
                        if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                            Log.i("checkState", "进来了第二层");
                            mState = STATE_TRY_DO_CAPTURE;
                            doStillCapture();
                        } else {
                            mState = STATE_TRY_CAPTURE_AGAIN;
                            tryCaptureAgain();
                        }
                    }
                    break;
                case STATE_TRY_CAPTURE_AGAIN:
                    Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null ||
                            aeState == CaptureResult.CONTROL_AE_STATE_PRECAPTURE ||
                            aeState == CaptureRequest.CONTROL_AE_STATE_FLASH_REQUIRED) {
                        mState = STATE_TRY_DO_CAPTURE;
                    }
                    break;
                case STATE_TRY_DO_CAPTURE:
                    aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                    if (aeState == null || aeState != CaptureResult.CONTROL_AE_STATE_PRECAPTURE) {
                        mState = STATE_TRY_DO_CAPTURE;
                        doStillCapture();
                    }
                    break;
            }
        }

    };

    private void doStillCapture() {
        try {
            CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());
            if (PreferenceHelper.getCameraFormat(getActivity()).equals("DNG")) {
                // Required for RAW capture
                captureBuilder.set(CaptureRequest.STATISTICS_LENS_SHADING_MAP_MODE, CaptureRequest.STATISTICS_LENS_SHADING_MAP_MODE_ON);
                captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
                captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                captureBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, (long) ((214735991 - 13231) / 2));
                captureBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, 0);
                captureBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, (10000 - 100) / 2);//设置 ISO，感光度
            } else {
                captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                captureBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

            }
            //设置每秒30帧
            CameraManager cameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(mCameraId);
            Range<Integer> fps[] = cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
            captureBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, fps[fps.length - 1]);

            int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
            mSession.stopRepeating();
            mSession.setRepeatingRequest(captureBuilder.build(), new CameraCaptureSession.CaptureCallback() {
            }, mPreviewHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void tryCaptureAgain() {
        mPreviewBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
        try {
            mSession.capture(mPreviewBuilder.build(), mSessionCaptureCallback, mPreviewHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void takeDngPicture(CaptureResult result) {
        if (PreferenceHelper.getCameraFormat(getActivity()).equals("DNG")) {
//            new Thread(new DngImageSaver(getActivity(), mImageReader, result)).start();
            mPreviewHandler.post(new DngImageSaver(getActivity(), mImageReader, result));
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_setting:
                Intent intent = new Intent(getActivity(), ChangeCameraActivity.class);
                getActivity().startActivity(intent);
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mCameraOpenCloseLock.release();
        mCameraDevice.close();
        mCameraDevice = null;
    }
}
