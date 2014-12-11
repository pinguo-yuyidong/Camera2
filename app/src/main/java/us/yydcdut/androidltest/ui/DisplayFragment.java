package us.yydcdut.androidltest.ui;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.DngCreator;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import us.yydcdut.androidltest.ImageSaver;
import us.yydcdut.androidltest.PreferenceHelper;
import us.yydcdut.androidltest.R;

/**
 * Created by yuyidong on 14-12-4.
 */
public class DisplayFragment extends Fragment implements View.OnClickListener {
    private static final int STATE_PREVIEW = 1;
    private static final int STATE_CAPTURE = 2;
    private static final int STATE_FOCUSING = 3;
    private int mState = 0;

    private SharedPreferences mSp;
    private SharedPreferences.Editor mEditor;

    /**
     * 转换屏幕预览和JPEG的朝向一致
     */
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    /**
     * 显示的界面
     */
    private MyTextureView mTextureView;
    /**
     * 拍照按钮
     */
    private ImageView btnCapture;
    /**
     * 设置按钮
     */
    private ImageView btnSetting;
    /**
     * 切换摄像头按钮
     */
    private ImageView btnChangeCamera;
    /**
     * size
     */
    private Size mPreviewSize;
    /**
     * CameraDevice，相机设备
     */
    private CameraDevice mCameraDevice;
    /**
     * 显示的Builder
     */
    private CaptureRequest.Builder mPreviewBuilder;
    /**
     * 拍照的Builder
     */
    private CaptureRequest.Builder mCaptureBuilder;
    /**
     * 相机管理
     */
    private CameraManager mCameraManager;
    /**
     * 为建立looper
     */
    private HandlerThread mHandlerThread;
    /**
     * 获取自己looper的handler
     */
    private Handler mHandler;
    /**
     * 参数
     */
    private CameraCharacteristics mCameraCharacteristics;
    /**
     * 目前使用的相机id
     */
    private String mCameraId;
    /**
     * dng图片的保存
     */
    private DngCreator mDngCreator;
    /**
     * 图片保存格式
     */
    private int mFormat;
    /**
     * 用来保存图片的ImageReader
     */
    private ImageReader mImageReader;

    public DisplayFragment() {
    }

    public static DisplayFragment newInstance() {
        DisplayFragment displayFregment = new DisplayFragment();
        return displayFregment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mCameraId = bundle.getString("cameraid");
        } else {
            mCameraId = PreferenceHelper.getCurrentCameraid(getActivity());
        }
        Log.i("mCameraId", "mCameraId--->" + mCameraId);
        //初始化sharedPreference
        mSp = getActivity().getSharedPreferences("currentcamera" + mCameraId, Context.MODE_PRIVATE);
        mEditor = mSp.edit();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.display_camera_frag, null);
        mTextureView = (MyTextureView) v.findViewById(R.id.textureview);
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        btnSetting = (ImageView) v.findViewById(R.id.btn_setting);
        btnCapture = (ImageView) v.findViewById(R.id.btn_capture);
        btnChangeCamera = (ImageView) v.findViewById(R.id.btn_change_camera);
        btnCapture.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
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
                            startPreview();
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                        break;
                }
                return true;
            }
        });
        btnSetting.setOnClickListener(this);
        btnChangeCamera.setOnClickListener(this);
        return v;
    }

    /**
     * 拍照
     */
    private void takePicture() throws CameraAccessException {
        if (null == mCameraDevice || null == mCameraManager) {
            Log.i("takePicture", "null==mCameraDevice || null == mCameraManager ");
            return;
        }
        //图片格式
        mFormat = mSp.getInt("format", 256);
        Log.i("takePicture", "format--->" + mFormat);
        //图片尺寸
        int sizeWidth = mSp.getInt("format_" + mFormat + "_pictureSize_width", 1280);
        int sizeHeight = mSp.getInt("format_" + mFormat + "_pictureSize_height", 960);
        Log.i("takePicture", "format_" + mFormat + "_pictureSize_width--->" + sizeWidth);
        Log.i("takePicture", "format_" + mFormat + "_pictureSize_height--->" + sizeHeight);
        //得到ImageReader对象,5为maxImage，放入队列里面的最大连拍张数(应该是这个意思)
        mImageReader = ImageReader.newInstance(sizeWidth, sizeHeight, mFormat, 5);
        //得到surface
        List<Surface> outputFurfaces = new ArrayList<Surface>(2);
        outputFurfaces.add(mImageReader.getSurface());
        outputFurfaces.add(new Surface(mTextureView.getSurfaceTexture()));
        //创建构建者，配置参数
        mCaptureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
        if (mFormat == ImageFormat.RAW_SENSOR) {
            mCaptureBuilder.set(CaptureRequest.STATISTICS_LENS_SHADING_MAP_MODE, CaptureRequest.STATISTICS_LENS_SHADING_MAP_MODE_ON); // Required for RAW capture
        }
        mCaptureBuilder.addTarget(mImageReader.getSurface());
        mCaptureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        //设置连续帧
        Range<Integer> fps[] = mCameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
        mCaptureBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, fps[fps.length - 1]);//设置每秒30帧
        //得到方向
        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        mCaptureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

        //开启保存JPEG图片的线程，，，保存DNG的图片线程再mSessionStateCallback--->mSessionCaptureCallback的回调中进行
        if (mFormat == ImageFormat.JPEG) {
            new Thread(new ImageSaver(mImageReader, mHandler, mFormat)).start();
        }

        mState = STATE_CAPTURE;
        mCameraDevice.createCaptureSession(outputFurfaces, mSessionStateCallback, mHandler);
    }

    /**
     * Texture的监听器
     */
    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
            Log.i("SurfaceTextureListener", "onSurfaceTextureAvailable");
            initHandler();
            try {
                openCamera(i, i2);
            } catch (CameraAccessException e) {
                e.printStackTrace();
                Log.i("onSurfaceTextureAvailable", "openCamera--->CameraAccessException e");
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
            Log.i("SurfaceTextureListener", "onSurfaceTextureSizeChanged");
            //configureTransform(i, i2);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            Log.i("SurfaceTextureListener", "onSurfaceTextureDestroyed");
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            //Log.i("SurfaceTextureListener", "onSurfaceTextureUpdated");
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        Log.i("onPause", "onPause");
        closeCamera();
        saveCurrentPreference();
    }

    /**
     * 关闭相机
     */
    private void closeCamera() {
        try {
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (mHandler != null) {
                //关闭线程
                mHandlerThread.quitSafely();
                mHandlerThread.join();
                mHandlerThread = null;
                mHandler = null;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化handler
     */
    private void initHandler() {
        mHandlerThread = new HandlerThread("Android_L_Camera");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    /**
     * 打开Camera
     */
    @SuppressWarnings("ResourceType")
    private void openCamera(int viewWidth, int viewHeight) throws CameraAccessException {
        //获得camera服务
        mCameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        //获得cameraid
        //String[] cameraIds = mCameraManager.getCameraIdList();
        initCameraParam(mCameraId, viewWidth, viewHeight);
        //configureTransform(viewWidth, viewHeight);
        //打开相机
        mCameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mHandler);
    }

    /**
     * 得到CameraCharacteristics等信息，设置显示大小
     *
     * @param cameraId
     * @param viewWidth
     * @param viewHeight
     * @throws CameraAccessException
     */
    private void initCameraParam(String cameraId, int viewWidth, int viewHeight) throws CameraAccessException {
        //描述CameraDevice属性
        mCameraCharacteristics = mCameraManager.getCameraCharacteristics(cameraId);
        //流配置
        StreamConfigurationMap map = mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        //适合SurfaceTexture的显示的size
        Size[] sizes = map.getOutputSizes(SurfaceTexture.class);
//        for (Size s : sizes) {
//            Log.i("map.getOutputSizes", "s.getHeight()--->" + s.getHeight() + ".....s.getWidth()--->" + s.getWidth());
//        }
//        //获得帧率
//        Range<Integer>[] fps = mCameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
//        for (int i = 0; i < fps.length; i++) {
//            Log.i("fps", "fps[" + i + "]" + fps[i]);
//        }
        mPreviewSize = getSuitablePreviewSize(sizes, viewWidth, viewHeight);
        int width = mSp.getInt("previewSize_width", 0);
        int height = mSp.getInt("previewSize_height", 0);
//        mPreviewSize = sizes[0];
        // 选择适合TextureView的长宽比的大小预览
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            mTextureView.fitWindow(width, height);
            mTextureView.fitWindow(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        } else {
            //方向旋转后的显示
//            mTextureView.fitWindow(height, width);
        }
    }

    /**
     * 得到最合适屏幕显示的尺寸
     *
     * @param sizes
     * @param viewWidth
     * @param viewHeight
     * @return
     */
    private Size getSuitablePreviewSize(Size[] sizes, int viewWidth, int viewHeight) {
        List<Size> bigEnough = new ArrayList<Size>();
        Size largeSize = Collections.max(Arrays.asList(sizes), new SizeComparator());
        int w = largeSize.getWidth();
        int h = largeSize.getHeight();
        for (Size option : sizes) {
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= viewWidth && option.getHeight() >= viewHeight) {
                bigEnough.add(option);
            }
        }

        // 选择最小的
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new SizeComparator());
        } else {
            Log.e("CameraFragment", "Couldn't find any suitable preview size");
            return sizes[0];
        }
    }

//    /**
//     * TextureView适合屏幕
//     *
//     * @param viewWidth
//     * @param viewHeight
//     */
//    private void configureTransform(int viewWidth, int viewHeight) {
//        Activity activity = getActivity();
//        if (null == mTextureView || null == mPreviewSize || null == activity) {
//            Log.i("configureTransform", "null == mTextureView || null == mPreviewSize || null == activity ");
//            return;
//        }
//        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
//        Matrix matrix = new Matrix();
//        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
//        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
//        float centerX = viewRect.centerX();
//        float centerY = viewRect.centerY();
//        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
//            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
//            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
//            float scale = Math.max(
//                    (float) viewHeight / mPreviewSize.getHeight(),
//                    (float) viewWidth / .getWidth());
//            matrix.postScale(scale, scale, centerX, centerY);
//            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
//        }
//        mTextureView.setTransform(matrix);
//    }

    /**
     * 相机状态的回调
     * 再onOpened中打开相机
     */
    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onClosed(CameraDevice camera) {
            Log.i("CameraDevice.StateCallback", "onClosed");
        }

        @Override
        public void onOpened(CameraDevice cameraDevice) {
            Log.i("CameraDevice.StateCallback", "onOpened");
            mCameraDevice = cameraDevice;
            try {
                startPreview();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            Log.i("CameraDevice.StateCallback", "onDisconnected");
        }

        @Override
        public void onError(CameraDevice cameraDevice, int i) {
            Log.i("CameraDevice.StateCallback", "onError--->" + i + ",,,null == cameraDevice--->" + (null == cameraDevice));
        }
    };

    /**
     * 开始Preview
     *
     * @throws CameraAccessException
     */
    private void startPreview() throws CameraAccessException {
        if (null == mCameraDevice || !mTextureView.isAvailable()) {
            Log.i("startPreview", "null == mCameraDevice || !mTextureView.isAvailable() ");
            Log.i("startPreview", "null == mCameraDevice--->" + (null == mCameraDevice));
            Log.i("startPreview", "!mTextureView.isAvailable()--->" + (!mTextureView.isAvailable()));
            return;
        }
        //得到SurfaceTexture
        SurfaceTexture texture = mTextureView.getSurfaceTexture();
        if (null == texture) {
            Log.i("startPreview", "null == texture");
            return;
        }
        //设置默认的图像缓冲区的大小
        int width = mSp.getInt("previewSize_width", 0);
        int height = mSp.getInt("previewSize_height", 0);
//        Log.i("startPreview", "width--->" + width + ",,,mPreviewSize.getWidth()--->" + mPreviewSize.getWidth() + ",,,height--->" + height + ",,,mPreviewSize.getHeight()" + mPreviewSize.getHeight());
//        texture.setDefaultBufferSize(width, height);
        texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Log.i("texture", "mPreviewSize.getWidth()--->" + mPreviewSize.getWidth() + ",,,mPreviewSize.getHeight()--->" + mPreviewSize.getHeight());
        //显示的Surface
        Surface surface = new Surface(texture);
        //TEMPLATE_PREVIEW--->创建一个请求适合相机预览窗口。
        mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        mPreviewBuilder.addTarget(surface);
        mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        //创建拍照会话，一旦CameraCaptureSession创建,可以提交请求（capture、captureBurst,或setRepeatingBurst）。
        mState = STATE_PREVIEW;
        mCameraDevice.createCaptureSession(Arrays.asList(surface), mSessionStateCallback, mHandler);
    }

    /**
     * 会话状态的回调
     * 继续preview
     */
    private CameraCaptureSession.StateCallback mSessionStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
            Log.i("CameraCaptureSession.StateCallback", "mSessionStateCallback--->onConfigured");
            Log.i("CameraCaptureSession.StateCallback", "mState--->" + mState);
            if (mState == STATE_PREVIEW) {
                try {
                    //这里也就是updatePreview，(官方文档--->请求无休止地重复捕获的图像捕获会话。)
                    //CONTROL_MODE--->3A整体模式(在自动曝光、自动白平衡、自动对焦)控制
                    //CONTROL_MODE_AUTO--->当设置为自动时,自己在android.control算法控制
                    cameraCaptureSession.setRepeatingRequest(mPreviewBuilder.build(), null, mHandler);
                    Log.i("CONTROL_AF_TRIGGER", "mPreviewBuilder.get(CaptureRequest.CONTROL_AF_TRIGGER)--->" + mPreviewBuilder.get(CaptureRequest.CONTROL_AF_TRIGGER));
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            } else if (mState == STATE_CAPTURE) {
                try {
                    cameraCaptureSession.setRepeatingRequest(mCaptureBuilder.build(), mSessionCaptureCallback, mHandler);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
            Log.i("CameraCaptureSession.StateCallback", "mSessionStateCallback--->onConfigureFailed");
//            Activity activity = getActivity();
//            if (null != activity) {
//                Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show();
//            }
            //因为尺寸问题可能导致开始时候配置失败，如果那样的话就不断的去开启直到成功
            getActivity().getFragmentManager().beginTransaction().replace(R.id.frame_main, DisplayFragment.newInstance()).commit();
        }
    };

    /**
     * 拍照的回调
     */
    private CameraCaptureSession.CaptureCallback mSessionCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
            Log.i("CameraCaptureSession.CaptureCallback", "onCaptureStarted");
            super.onCaptureStarted(session, request, timestamp, frameNumber);
        }

        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            Log.i("CameraCaptureSession.CaptureCallback", "onCaptureCompleted");
            super.onCaptureCompleted(session, request, result);
            //保存dng格式的文件
            if (mFormat == ImageFormat.RAW_SENSOR) {
                mDngCreator = new DngCreator(mCameraCharacteristics, result);
                new Thread(new ImageSaver(mImageReader, mHandler, mFormat, mDngCreator)).start();
            }

        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_setting:
                SettingFragment settingFragment = SettingFragment.newInstance();
                Bundle bundle = new Bundle();
                bundle.putString("camera", "camera" + mCameraId);
                settingFragment.setArguments(bundle);
                getActivity().getFragmentManager().beginTransaction().replace(R.id.frame_main, settingFragment).addToBackStack(null).commit();
                break;
            case R.id.btn_change_camera:
                if (mCameraId.equals("1")) {
                    mCameraId = "0";
                } else if (mCameraId.equals("0")) {
                    mCameraId = "1";
                } else {
                    mCameraId = "0";
                }
                //关闭相机在开启另外个摄像头
                mCameraDevice.close();
                try {
                    mCameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mHandler);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    /**
     * 对比，取出最大的
     */
    class SizeComparator implements Comparator<Size> {

        @Override
        public int compare(Size size, Size size2) {
            return Long.signum((long) size.getWidth() * size.getHeight() -
                    (long) size2.getWidth() * size2.getHeight());
        }
    }

    private void saveCurrentPreference() {
        PreferenceHelper.writeCurrentCameraid(getActivity(), mCameraId);
    }


}
