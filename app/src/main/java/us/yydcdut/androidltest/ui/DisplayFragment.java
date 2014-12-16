package us.yydcdut.androidltest.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
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
import android.hardware.camera2.params.MeteringRectangle;
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
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

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
public class DisplayFragment extends Fragment implements View.OnClickListener, MyTextureView.MyTextureViewTouchEvent {
    private static final int STATE_PREVIEW = 1;
    private static final int STATE_CAPTURE = 2;
    private int mState = 0;

    private static final int SHOW_AF = 1;
    private static final int SHOW_AE = 2;
    private static final int SHOW_AWB = 3;

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
     * 参数,再openCamera--->initCamera中初始化的
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
    /**
     * 相机的会话
     */
    private CameraCaptureSession mCameraCaptureSession;
    /**
     * 是否显示focus的按钮
     */
    private boolean showAfFlag = false;
    /**
     * focus的layout
     */
    private LinearLayout mLayoutAf;
    /**
     * 是否显示Ae的按钮
     */
    private boolean showAeFlag = false;
    /**
     * ae的layout
     */
    private LinearLayout mLayoutAe;
    /**
     * 是否显示Awb的按钮
     */
    private boolean showAwbFlag = false;
    /**
     * awb的layout
     */
    private LinearLayout mLayoutAwb;
    /**
     * 底部的layout
     */
    private RelativeLayout mLayoutBottom;
    /**
     * 拍照的layout
     */
    private LinearLayout mLayoutCapture;
    /**
     * 存layout，方便再visible和invisible的时候取出来，注意位置
     */
    private List<View> mLayoutList;
    /**
     * visible与invisible之间切换的动画
     */
    private TranslateAnimation mShowAction;

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
        initUIAndListener(v);
        initAnimation();
        return v;
    }

    /**
     * 初始化动画效果
     */
    private void initAnimation() {
        mShowAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                -1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        mShowAction.setDuration(500);
    }

    /**
     * 初始化
     *
     * @param v
     */
    private void initUIAndListener(View v) {
        mTextureView = (MyTextureView) v.findViewById(R.id.textureview);
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        mTextureView.setmMyTextureViewTouchEvent(this);

        mLayoutBottom = (RelativeLayout) v.findViewById(R.id.layout_bottom);

        mLayoutAf = (LinearLayout) v.findViewById(R.id.layout_focus);
        Switch switchAf = (Switch) v.findViewById(R.id.switch_focus);
        SeekBar sbAf = (SeekBar) v.findViewById(R.id.sb_focus);

        mLayoutAe = (LinearLayout) v.findViewById(R.id.layout_ae);
        Switch switchAe = (Switch) v.findViewById(R.id.switch_ae);
        SeekBar sbAe = (SeekBar) v.findViewById(R.id.sb_ae);

        mLayoutAwb = (LinearLayout) v.findViewById(R.id.layout_awb);
        AwbSeekBar sbAwb = (AwbSeekBar) v.findViewById(R.id.sb_awb);

        mLayoutCapture = (LinearLayout) v.findViewById(R.id.layout_capture);

        ImageView btnSetting = (ImageView) v.findViewById(R.id.btn_setting);
        ImageView btnCapture = (ImageView) v.findViewById(R.id.btn_capture);
        ImageView btnChangeCamera = (ImageView) v.findViewById(R.id.btn_change_camera);
        ImageView btnFocus = (ImageView) v.findViewById(R.id.btn_focus);
        ImageView btnAe = (ImageView) v.findViewById(R.id.btn_ae);
        ImageView btnAwb = (ImageView) v.findViewById(R.id.btn_awb);

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
                        Log.i("setOnTouchListener", "MotionEvent.ACTION_DOWN");
                        break;
                    case MotionEvent.ACTION_UP:
                        continuePreview();
                        Log.i("setOnTouchListener", "MotionEvent.ACTION_UP");
                        break;
                }
                return true;
            }
        });
        //button
        btnSetting.setOnClickListener(this);
        btnChangeCamera.setOnClickListener(this);
        btnFocus.setOnClickListener(this);
        btnAe.setOnClickListener(this);
        btnAwb.setOnClickListener(this);
        //switch
        MyOnCheckedChangeListener myOnClickChangeListener = new MyOnCheckedChangeListener();
        switchAf.setOnCheckedChangeListener(myOnClickChangeListener);
        switchAe.setOnCheckedChangeListener(myOnClickChangeListener);

        //seekbar
        MySeekBarListener listener = new MySeekBarListener();
        sbAf.setOnSeekBarChangeListener(listener);
        sbAe.setOnSeekBarChangeListener(listener);
        sbAwb.setmOnAwbSeekBarChangeListener(new MyAwbSeekBarChangedListener());
        //sbAwb.setOnSeekBarChangeListener(listener);
        sbAf.setMax(100);
        sbAe.setMax(100);
        //list ,add
        mLayoutList = new ArrayList<View>();
        mLayoutList.add(mLayoutBottom);//位置0
        mLayoutList.add(mLayoutAf);//位置1
        mLayoutList.add(mLayoutAe);//位置2
        mLayoutList.add(mLayoutAwb);//位置3
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
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
            Log.i("SurfaceTextureListener", "onSurfaceTextureSizeChanged");
            configureTransform(i, i2);
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
        if (null != mCameraDevice) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        if (mHandler != null) {
            try {
                //关闭线程
                mHandlerThread.quitSafely();
                mHandlerThread.join();
                mHandlerThread = null;
                mHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 退出之前把修改的内容保存下来
     */
    private void saveCurrentPreference() {
        PreferenceHelper.writeCurrentCameraid(getActivity(), mCameraId);
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
        setUpCameraOutputs(viewWidth, viewHeight);
        configureTransform(viewWidth, viewHeight);
        //打开相机
        mCameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mHandler);
    }

    /**
     * 得到CameraCharacteristics等信息，设置显示大小
     *
     * @param viewWidth
     * @param viewHeight
     * @throws CameraAccessException
     */
    private void setUpCameraOutputs(int viewWidth, int viewHeight) throws CameraAccessException {
        //描述CameraDevice属性
        mCameraCharacteristics = mCameraManager.getCameraCharacteristics(mCameraId);
        //流配置
        StreamConfigurationMap map = mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        //适合SurfaceTexture的显示的size
        Size largest = Collections.max(
                Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                new CompareSizesByArea());
        //if (mCameraId.equals("0")) {
        mPreviewSize = map.getOutputSizes(SurfaceTexture.class)[0];
//        } else {
//            mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), viewWidth, viewHeight, largest);
//            // 选择适合TextureView的长宽比的大小预览
//            int orientation = getResources().getConfiguration().orientation;
//            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                mTextureView.fitWindow(mPreviewSize.getWidth(), mPreviewSize.getHeight());
//            } else {
//                mTextureView.fitWindow(mPreviewSize.getHeight(), mPreviewSize.getWidth());
//            }
//        }
    }

    /**
     * 官方sample中的
     *
     * @param viewWidth
     * @param viewHeight
     */
    private void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = getActivity();
        if (null == mTextureView || null == mPreviewSize || null == activity) {
            Log.e("configureTransform", "null == mTextureView || null == mPreviewSize || null == activity");
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
        mTextureView.setTransform(matrix);
    }

    /**
     * 得到最合适屏幕显示的尺寸
     *
     * @param sizes
     * @param viewWidth
     * @param viewHeight
     * @return
     */
    private Size chooseOptimalSize(Size[] sizes, int viewWidth, int viewHeight, Size largeSize) {
        List<Size> bigEnough = new ArrayList<Size>();
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
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            Log.e("chooseOptimalSize", "Couldn't find any suitable preview size");
            return sizes[0];
        }
    }

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
            startPreview();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            Log.i("CameraDevice.StateCallback", "onDisconnected");
        }

        @Override
        public void onError(CameraDevice cameraDevice, int i) {
            Log.i("CameraDevice.StateCallback", "onError--->" + i + ",,,null == cameraDevice--->" + (null == cameraDevice));
//            Toast.makeText(getActivity(), "相机出问题了，其他应用未关闭相机!", Toast.LENGTH_SHORT).show();
        }
    };
    Surface surface;

    /**
     * 开始Preview
     */
    private void startPreview() {
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
        try {
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            //设置默认的图像缓冲区的大小
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            //显示的Surface
            surface = new Surface(texture);
            //TEMPLATE_PREVIEW--->创建一个请求适合相机预览窗口。
            mPreviewBuilder.addTarget(surface);
            //3A--->auto
            mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            //创建拍照会话，一旦CameraCaptureSession创建,可以提交请求（capture、captureBurst,或setRepeatingBurst）。
            mState = STATE_PREVIEW;
            mCameraDevice.createCaptureSession(Arrays.asList(surface), mSessionStateCallback, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 继续预览
     *
     * @throws CameraAccessException
     */
    private void continuePreview() {
//        mState = STATE_CONTINUE_PREVIEW;
        mState = STATE_PREVIEW;
        try {
            mCameraDevice.createCaptureSession(Arrays.asList(surface), mSessionStateCallback, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
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
                    mCameraCaptureSession = cameraCaptureSession;
                    cameraCaptureSession.setRepeatingRequest(mPreviewBuilder.build(), null, mHandler);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    Log.e("onConfigured", "STATE_PREVIEW,,,Session has been closed; further changes are illegal.");
                    e.printStackTrace();
                    mCameraDevice.close();
                    try {
                        mCameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mHandler);
                    } catch (CameraAccessException ee) {
                        ee.printStackTrace();
                    }
                }
            } else if (mState == STATE_CAPTURE) {
                try {
                    cameraCaptureSession.setRepeatingRequest(mCaptureBuilder.build(), mSessionCaptureCallback, mHandler);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    Log.e("onConfigured", "STATE_CAPTURE,,,Session has been closed; further changes are illegal.");
                    e.printStackTrace();
                    mCameraDevice.close();
                    try {
                        mCameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mHandler);
                    } catch (CameraAccessException ee) {
                        ee.printStackTrace();
                    }
                }
            }
        }


        @Override
        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
            Log.i("CameraCaptureSession.StateCallback", "mSessionStateCallback--->onConfigureFailed");
            Activity activity = getActivity();
            if (null != activity) {
                Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show();
            }
            //因为尺寸问题可能导致开始时候配置失败，如果那样的话就不断的去开启直到成功
            //getActivity().getFragmentManager().beginTransaction().replace(R.id.frame_main, DisplayFragment.newInstance()).commit();
        }
    };

    /**
     * 拍照会话的回调
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
                if (null == mDngCreator) {
                    mDngCreator = new DngCreator(mCameraCharacteristics, result);
                }
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
                //关闭相机再开启另外个摄像头
                mCameraDevice.close();
                try {
                    mCameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mHandler);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_focus:
                showAfFlag = !showAfFlag;
                showAfLayout(SHOW_AF, showAfFlag);
                break;
            case R.id.btn_ae:
                showAeFlag = !showAeFlag;
                showAfLayout(SHOW_AE, showAeFlag);
                break;
            case R.id.btn_awb:
                showAwbFlag = !showAwbFlag;
                showAfLayout(SHOW_AWB, showAwbFlag);
                break;
        }
    }

    /**
     * 触摸对焦
     *
     * @param event
     * @return
     */
    @Override
    public boolean onAreaTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_UP:
                //这里存折聚焦的left,right,top,bottm
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
//                Log.i("onAreaTouchEvent", "left--->" + left + ",,,right--->" + right + ",,,top--->" + top + ",,,bottom--->" + bottom);
//                Log.i("onAreaTouchEvent", "mTextureView.getWidth()--->" + a + ",,,mTextureView.getHeight()--->" + b);
//                Log.i("onAreaTouchEvent", "event.getX()--->" + centerX + ",,,event.getY()--->" + centerY);
//                Log.i("onAreaTouchEvent", "ll--->" + ll + ",,,rr--->" + rr);
                Log.i("focus_position", "focusLeft--->" + focusLeft + ",,,focusTop--->" + focusBottom + ",,,focusRight--->" + (focusLeft + areaSize) + ",,,focusBottom--->" + (focusBottom + areaSize));
                newRect = new Rect(focusLeft, focusBottom, focusLeft + areaSize, focusBottom + areaSize);
                MeteringRectangle meteringRectangle = new MeteringRectangle(newRect, 500);
                MeteringRectangle[] meteringRectangleArr = new MeteringRectangle[1];
                meteringRectangleArr[0] = meteringRectangle;
                mPreviewBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, meteringRectangleArr);
                mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
                updatePreview();
                break;
        }
        return true;
    }

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
     * 基于区域来比较两个Size
     */
    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    /**
     * seekbar的监听器
     */
    class MySeekBarListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            switch (seekBar.getId()) {
                case R.id.sb_focus:
                    //AF--->ok   ???[0,10]
                    //Log.i("onProgressChanged", "mCameraCharacteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE)--->" + mCameraCharacteristics.get(CameraCharacteristics.LENS_INFO_MINIMUM_FOCUS_DISTANCE));
                    //float[] f = mCameraCharacteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
                    mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_OFF);
                    mPreviewBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, (float) (i / 10));
                    //Log.i("onProgressChanged", "LENS_FOCUS_DISTANCE--->" + (i / 10));
                    break;
                case R.id.sb_ae:
                    //AE--->OK
                    //这里AE太多会非常非常卡
//                    Range<Long> range = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
//                    long max = range.getUpper();
//                    long min = range.getLower();
                    long max = 214735991;
                    long min = 13231;
                    //这个范围差不多
                    long ae = ((i * (max - min)) / 100 + min);
                    mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_OFF);
                    mPreviewBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, ae);
//                    Log.i("onProgressChanged", "ae--->" + ae);
                    break;
            }
//            float f = mCameraCharacteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
//            Log.i("onProgressChanged", "ffff->>>" + f);
//            Rect rect = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
//            Log.i("111111", rect.left + "   " + rect.top + "   " + rect.right + "   " + rect.bottom);
//            int centerX = rect.centerX();
//            int centerY = rect.centerY();
//            Rect newRect = new Rect(4, 4,  ((100 - i) * 1292 / 100),  ((100 - i) * 972 / 100));
//            mPreviewBuilder.set(CaptureRequest.SCALER_CROP_REGION, newRect);
            updatePreview();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    /**
     * 更新预览
     */
    private void updatePreview() {
        try {
            mCameraCaptureSession.setRepeatingRequest(mPreviewBuilder.build(), null, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Awb的seekbar
     */
    class MyAwbSeekBarChangedListener implements AwbSeekBar.OnAwbSeekBarChangeListener {

        @Override
        public void doInProgress1() {
            mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_AUTO);
            updatePreview();
        }

        @Override
        public void doInProgress2() {
            mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT);
            updatePreview();
        }

        @Override
        public void doInProgress3() {
            mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_DAYLIGHT);
            updatePreview();
        }

        @Override
        public void doInProgress4() {
            mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_FLUORESCENT);
            updatePreview();
        }

        @Override
        public void doInProgress5() {
            mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_INCANDESCENT);
            updatePreview();
        }

        @Override
        public void doInProgress6() {
            mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_SHADE);
            updatePreview();
        }

        @Override
        public void doInProgress7() {
            mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_TWILIGHT);
            updatePreview();
        }

        @Override
        public void doInProgress8() {
            mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_WARM_FLUORESCENT);
            updatePreview();
        }

        @Override
        public void onStopTrackingTouch(int num) {
            switch (num) {
                case 0:
                    mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_AUTO);
                    break;
                case 10:
                    mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT);
                    break;
                case 20:
                    mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_DAYLIGHT);
                    break;
                case 30:
                    mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_FLUORESCENT);
                    break;
                case 40:
                    mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_INCANDESCENT);
                    break;
                case 50:
                    mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_SHADE);
                    break;
                case 60:
                    mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_TWILIGHT);
                    break;
                case 70:
                    mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_WARM_FLUORESCENT);
                    break;
            }
            updatePreview();
        }
    }

    /**
     * switch的监听器
     */
    class MyOnCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.switch_focus:
                    if (isChecked) {
                        mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_AUTO);
                        mLayoutAf.getChildAt(1).setEnabled(false);
                    } else {
                        mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_OFF);
                        mLayoutAf.getChildAt(1).setEnabled(true);
                    }
                    break;
                case R.id.switch_ae:
                    if (isChecked) {
                        mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
                        mLayoutAe.getChildAt(1).setEnabled(false);
                    } else {
                        mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_OFF);
                        mLayoutAe.getChildAt(1).setEnabled(true);
                    }
                    break;
            }
        }
    }

    /**
     * 显示和隐藏控件
     *
     * @param showWhat
     * @param showOrNot
     */
    private void showAfLayout(int showWhat, boolean showOrNot) {
        View v = mLayoutList.get(showWhat);
        if (showOrNot) {
            //全部隐藏但是AF/AE的显示出来
            for (int i = 0; i < mLayoutBottom.getChildCount(); i++) {
                mLayoutBottom.getChildAt(i).setVisibility(View.INVISIBLE);
            }
            v.startAnimation(mShowAction);
            v.setVisibility(View.VISIBLE);
        } else {
            //全部隐藏但是capture的显示出来
            for (int i = 0; i < mLayoutBottom.getChildCount(); i++) {
                mLayoutBottom.getChildAt(i).setVisibility(View.INVISIBLE);
            }
            mLayoutCapture.startAnimation(mShowAction);
            mLayoutCapture.setVisibility(View.VISIBLE);
        }
    }


}
