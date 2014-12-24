package us.yydcdut.androidltest;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.media.ImageReader;
import android.media.MediaActionSound;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.Surface;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import us.yydcdut.androidltest.callback.DngSessionCallback;
import us.yydcdut.androidltest.callback.JpegSessionCallback;
import us.yydcdut.androidltest.callback.PreviewSessionCallback;
import us.yydcdut.androidltest.listener.TextureViewTouchEvent;
import us.yydcdut.androidltest.ui.AnimationImageView;
import us.yydcdut.androidltest.ui.MyTextureView;

/**
 * Created by yuyidong on 14-12-23.
 */
public class CameraUtils {
    private static final int STATE_PREVIEW = 1;
    private static final int STATE_CAPTURE = 2;
    private int mState = 0;
    private MyTextureView mTextureView;
    private CameraManager mCameraManager;
    private Handler mMainHandler;
    private Handler mCameraHandler;
    private HandlerThread mCameraThread;
    private CameraCharacteristics mCameraCharacteristics;
    private Context mContext;
    private Size mPreviewSize;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCameraCaptureSession;
    private PreviewSessionCallback mPreviewSessionCallback;
    private SharedPreferences mSp;
    private String mCameraId;
    private int mFormat;
    private ImageReader mImageReader;
    /**
     * 显示的Builder
     */
    private CaptureRequest.Builder mPreviewBuilder;
    /**
     * 拍照的Builder
     */
    private CaptureRequest.Builder mCaptureBuilder;
    private MediaActionSound mMediaActionSound;

    public CameraUtils(Context context, CameraManager mCameraManager, MyTextureView mTextureView, Handler mMainHandler, String mCameraId, MediaActionSound mMediaActionSound) {
        this.mContext = context;
        this.mCameraManager = mCameraManager;
        this.mMainHandler = mMainHandler;
        this.mTextureView = mTextureView;
        this.mCameraId = mCameraId;
        this.mMediaActionSound = mMediaActionSound;
        initHandlerAndThread();
        mSp = mContext.getSharedPreferences("currentcamera" + mCameraId, Context.MODE_PRIVATE);
    }

    private void initHandlerAndThread() {
        mCameraThread = new HandlerThread("CameraUtils");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper());
    }

    public void openCamera(String cameraId, AnimationImageView focusImage) {
        try {
            doSomeThingBeforeOpen(cameraId);
            newPreviewSessionCallback(focusImage);
            mCameraManager.openCamera(cameraId, mCameraDeviceStateCallback, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    //openCamera
    private void doSomeThingBeforeOpen(String cameraId) throws CameraAccessException {
        mCameraCharacteristics = mCameraManager.getCameraCharacteristics(cameraId);
        //适合SurfaceTexture的显示的size
        mPreviewSize = new Size(1280, 720);
    }

    //openCamera
    private void newPreviewSessionCallback(AnimationImageView focusImage) {
        mPreviewSessionCallback = new PreviewSessionCallback(focusImage, mMainHandler, mTextureView);
    }

    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onClosed(CameraDevice camera) {
        }

        @Override
        public void onOpened(CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            startPreview();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            Toast.makeText(mContext, "onDisconnected", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(CameraDevice cameraDevice, int i) {
            Toast.makeText(mContext, "onError", Toast.LENGTH_SHORT).show();
        }
    };

    //mCameraDeviceStateCallback
    private void startPreview() {
        try {
            //得到SurfaceTexture
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            //得到SurfaceTexture
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            //设置默认的图像缓冲区的大小
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            //显示的Surface
            Surface surface = new Surface(texture);
            //TEMPLATE_PREVIEW--->创建一个请求适合相机预览窗口。
            mPreviewBuilder.addTarget(surface);
            //初始化参数
            //3A--->auto
            mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            //3A
            mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_AUTO);
            mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AF_MODE_AUTO);
            mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_AUTO);
            //创建拍照会话，一旦CameraCaptureSession创建,可以提交请求（capture、captureBurst,或setRepeatingBurst）。
            mState = STATE_PREVIEW;
            mCameraDevice.createCaptureSession(Arrays.asList(surface), mSessionStateCallback, mCameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void takePicture() throws CameraAccessException {
        //图片格式
        mFormat = mSp.getInt("format", 256);
        Log.i("takePicture", "format--->" + mFormat);
        //图片尺寸
        int sizeWidth = mSp.getInt("format_" + mFormat + "_pictureSize_width", 1280);
        int sizeHeight = mSp.getInt("format_" + mFormat + "_pictureSize_height", 960);
        //得到ImageReader对象,5为maxImage，放入队列里面的最大连拍张数(应该是这个意思)
        mImageReader = ImageReader.newInstance(sizeWidth, sizeHeight, mFormat, 2);
        //得到surface
        List<Surface> outputFurfaces = new ArrayList<Surface>(2);
        outputFurfaces.add(mImageReader.getSurface());
        //创建构建者，配置参数
        mCaptureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
        if (mFormat == ImageFormat.RAW_SENSOR) {
            mCaptureBuilder.set(CaptureRequest.STATISTICS_LENS_SHADING_MAP_MODE, CaptureRequest.STATISTICS_LENS_SHADING_MAP_MODE_ON); // Required for RAW capture
        }
        mCaptureBuilder.addTarget(mImageReader.getSurface());
        //设置连续帧
        Range<Integer> fps[] = mCameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
        mCaptureBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, fps[fps.length - 1]);//设置每秒30帧
        //得到方向
//        int rotation = mContext.getWindowManager().getDefaultDisplay().getRotation();
//        mCaptureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
//        previewBuilder2CaptureBuilder();
        mState = STATE_CAPTURE;
        mCameraDevice.createCaptureSession(outputFurfaces, mSessionStateCallback, mCameraHandler);
    }

    private CameraCaptureSession.StateCallback mSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
            Log.i("CameraCaptureSession.StateCallback", "mSessionStateCallback--->onConfigured");
            if (mState == STATE_PREVIEW) {
                try {
                    mCameraCaptureSession = cameraCaptureSession;
                    setTextureViewTouchEvent();
                    cameraCaptureSession.setRepeatingRequest(mPreviewBuilder.build(), mPreviewSessionCallback, mCameraHandler);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    Log.e("onConfigured", "STATE_PREVIEW,,,Session has been closed; further changes are illegal.");
                    e.printStackTrace();
                    Toast.makeText(mContext, "STATE_PREVIEW Exception e,now reopen camera.", Toast.LENGTH_SHORT).show();
                }
            } else if (mState == STATE_CAPTURE) {
                if (mFormat == ImageFormat.RAW_SENSOR) {
                    try {
                        cameraCaptureSession.setRepeatingRequest(mCaptureBuilder.build(), new DngSessionCallback(mContext, mImageReader, mCameraHandler, mMediaActionSound), mCameraHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        cameraCaptureSession.setRepeatingRequest(mCaptureBuilder.build(), new JpegSessionCallback(mCameraHandler, mMediaActionSound, mImageReader), mCameraHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        Log.e("onConfigured", "STATE_CAPTURE,,,Session has been closed; further changes are illegal.");
                        e.printStackTrace();
                        Toast.makeText(mContext, "STATE_CAPTURE Exception e,now reopen camera.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {
            Toast.makeText(mContext, "Failed!Reopen~", Toast.LENGTH_SHORT).show();
        }
    };

    //mSessionStateCallback 在preview之后才起作用
    private void setTextureViewTouchEvent() {
        mTextureView.setmMyTextureViewTouchEvent(new TextureViewTouchEvent(mCameraCharacteristics, mTextureView, mPreviewBuilder, mCameraCaptureSession, mCameraHandler, mPreviewSessionCallback));
    }


}
