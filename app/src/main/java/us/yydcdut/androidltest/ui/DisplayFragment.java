package us.yydcdut.androidltest.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
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
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.media.MediaActionSound;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import us.yydcdut.androidltest.PreferenceHelper;
import us.yydcdut.androidltest.R;
import us.yydcdut.androidltest.adpater.EffectAdapter;
import us.yydcdut.androidltest.adpater.FlashAdapter;
import us.yydcdut.androidltest.adpater.SenseAdapter;
import us.yydcdut.androidltest.callback.DngSessionCallback;
import us.yydcdut.androidltest.callback.JpegSessionCallback;
import us.yydcdut.androidltest.callback.PreviewSessionCallback;
import us.yydcdut.androidltest.listener.AwbSeekBarChangeListener;
import us.yydcdut.androidltest.listener.EffectItemClickListener;
import us.yydcdut.androidltest.listener.FlashItemClickListener;
import us.yydcdut.androidltest.listener.JpegReaderListener;
import us.yydcdut.androidltest.listener.SenseItemClickListener;
import us.yydcdut.androidltest.listener.TextureViewTouchEvent;
import us.yydcdut.androidltest.otheractivity.FlashActivity;

/**
 * Created by yuyidong on 14-12-4.
 */
public class DisplayFragment extends Fragment implements View.OnClickListener {
    private static final int STATE_PREVIEW = 1;
    private static final int STATE_CAPTURE = 2;
    private int mState = 0;
    public static final int FOCUS_DISAPPEAR = 100;
    public static final int WINDOW_TEXT_DISAPPEAR = 101;

    private static final int SHOW_AF = 1;
    private static final int SHOW_AE = 2;
    private static final int SHOW_AWB = 3;
    private static final int SHOW_ISO = 4;
    private static final int SHOW_ZOOM = 5;

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
     * 用来focus的显示框框之类的
     */
    private PreviewSessionCallback mPreviewSessionCallback;
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
     * 是否显示Awb的按钮
     */
    private boolean showAwbFlag = false;
    /**
     * 是否显示iso的按钮
     */
    private boolean showIsoFlag = false;
    /**
     * iso的layout
     */
    private LinearLayout mLayoutIso;
    /**
     * 是否显示zoom的按钮
     */
    private boolean showZoomFlag = false;
    /**
     * 底部的layout
     */
    private RelativeLayout mLayoutBottom;
    /**
     * 拍照的layout
     */
    private RelativeLayout mLayoutCapture;
    /**
     * 存layout，方便再visible和invisible的时候取出来，注意位置
     */
    private List<View> mLayoutList;
    /**
     * visible与invisible之间切换的动画
     */
    private TranslateAnimation mShowAction;
    /**
     * Focus的Scale动画
     */
    private ScaleAnimation mScaleFocusAnimation;
    /**
     * popupwindow的字的动画
     */
    private ScaleAnimation mScaleWindowAnimation;
    /**
     * 淡入动画
     */
    private AlphaAnimation mAlphaInAnimation;
    /**
     * 淡出动画
     */
    private AlphaAnimation mAlphaOutAnimation;
    /**
     * 显示的surface
     */
    private Surface mSurface;
    /**
     * seekbar中的数字那些的显示
     */
    private TextView mSeekBarTextView;
    /**
     * popup window之后的显示文字
     */
    private AnimationTextView mWindowTextView;
    /**
     * awb 的seekbar
     */
    private AwbSeekBar mAwbSb;
    /**
     * Effect 的 button
     */
    private ImageView mBtnEffect;
    /**
     * flash的button
     */
    private ImageView mBtnFlash;
    /**
     * flash的button
     */
    private ImageView mBtnSense;
    /**
     * 拍照声音
     */
    private MediaActionSound mMediaActionSound;
    /**
     * focus的图
     */
    private AnimationImageView mFocusImage;
    private int mToPreviewWidth;
    private int mToPreviewHeight;
    private List<Surface> mOutputSurfaces;
    private Size mlargest;
    //----------------------------seekbar的值-------------------------
    //初始化的话是实用中间值
    private float valueAF;
    private int valueAE;
    private long valueAETime;
    private int valueISO;

    //----------------------------seekbar的值-------------------------
    /**
     * UI线程的handler
     */
    private Handler mMainHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case FOCUS_DISAPPEAR:
                    if (msg.obj == null) {
                        mFocusImage.stopFocus();
                        break;
                    }
                    Integer valueTimes = (Integer) msg.obj;
                    if (mFocusImage.mTimes == valueTimes.intValue()) {
                        mFocusImage.stopFocus();
                    }
                    break;
                case WINDOW_TEXT_DISAPPEAR:
                    if (msg.obj == null) {
                        break;
                    }
                    Integer valueTimes2 = (Integer) msg.obj;
                    if (mWindowTextView.mTimes == valueTimes2.intValue()) {
                        mWindowTextView.stop();
                    }
                    break;
            }
        }
    };

    public DisplayFragment() {
    }

    public static DisplayFragment newInstance() {
        DisplayFragment displayFragment = new DisplayFragment();
        return displayFragment;
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
        //初始化sharedPreference
        mSp = getActivity().getSharedPreferences("currentcamera" + mCameraId, Context.MODE_PRIVATE);
        mEditor = mSp.edit();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.display_camera_frag, null);
        initAnimation();
        initUIAndListener(v);
        initSeekBarValue();
        initShutter();
        initFocusImage();
        return v;
    }

    /**
     * 初始化动画效果
     */
    private void initAnimation() {
        mShowAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        mShowAction.setDuration(500);
        mScaleFocusAnimation = new ScaleAnimation(2.0f, 1.0f, 2.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mScaleFocusAnimation.setDuration(200);
        mScaleWindowAnimation = new ScaleAnimation(2.0f, 1.0f, 2.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mScaleWindowAnimation.setDuration(500);
        mAlphaInAnimation = new AlphaAnimation(0.0f, 1.0f);
        mAlphaInAnimation.setDuration(500);
        mAlphaOutAnimation = new AlphaAnimation(1.0f, 0.0f);
        mAlphaOutAnimation.setDuration(500);
    }

    /**
     * 初始化
     *
     * @param v
     */
    private void initUIAndListener(View v) {
        mTextureView = (MyTextureView) v.findViewById(R.id.textureview);
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);//设置监听器

        mSeekBarTextView = (TextView) v.findViewById(R.id.txt_sb_txt);
        mSeekBarTextView.setVisibility(View.INVISIBLE);

        mWindowTextView = (AnimationTextView) v.findViewById(R.id.txt_window_txt);
        mWindowTextView.setVisibility(View.INVISIBLE);
        mWindowTextView.setmAnimation(mScaleWindowAnimation);
        mWindowTextView.setmMainHandler(mMainHandler);

        mFocusImage = (AnimationImageView) v.findViewById(R.id.img_focus);
        mFocusImage.setVisibility(View.INVISIBLE);
        mFocusImage.setmMainHandler(mMainHandler);
        mFocusImage.setmAnimation(mScaleFocusAnimation);

        mLayoutBottom = (RelativeLayout) v.findViewById(R.id.layout_bottom);

        mLayoutAf = (LinearLayout) v.findViewById(R.id.layout_focus);
        Switch switchAf = (Switch) v.findViewById(R.id.switch_focus);
        SeekBar sbAf = (SeekBar) v.findViewById(R.id.sb_focus);

        LinearLayout layoutAe = (LinearLayout) v.findViewById(R.id.layout_ae);
        Switch switchAe = (Switch) v.findViewById(R.id.switch_ae);
        SeekBar sbAe = (SeekBar) v.findViewById(R.id.sb_ae);

        LinearLayout layoutAwb = (LinearLayout) v.findViewById(R.id.layout_awb);
        mAwbSb = (AwbSeekBar) v.findViewById(R.id.sb_awb);

        mLayoutIso = (LinearLayout) v.findViewById(R.id.layout_iso);
        Switch switchIso = (Switch) v.findViewById(R.id.switch_iso);
        SeekBar sbIso = (SeekBar) v.findViewById(R.id.sb_iso);

        LinearLayout layoutZoom = (LinearLayout) v.findViewById(R.id.layout_zoom);
        SeekBar sbZoom = (SeekBar) v.findViewById(R.id.sb_zoom);

        mLayoutCapture = (RelativeLayout) v.findViewById(R.id.layout_capture);

        ImageView btnSetting = (ImageView) v.findViewById(R.id.btn_setting);
        final ImageView btnCapture = (ImageView) v.findViewById(R.id.btn_capture);
        ImageView btnChangeCamera = (ImageView) v.findViewById(R.id.btn_change_camera);
        ImageView btnFocus = (ImageView) v.findViewById(R.id.btn_focus);
        ImageView btnAe = (ImageView) v.findViewById(R.id.btn_ae);
        ImageView btnAwb = (ImageView) v.findViewById(R.id.btn_awb);
        ImageView btnIso = (ImageView) v.findViewById(R.id.btn_iso);
        ImageView btnZoom = (ImageView) v.findViewById(R.id.btn_zoom);
        ImageView btnAlbum = (ImageView) v.findViewById(R.id.btn_album);
        ImageView btnFlashLight = (ImageView) v.findViewById(R.id.btn_flashlight);
        mBtnEffect = (ImageView) v.findViewById(R.id.btn_effect);
        mBtnFlash = (ImageView) v.findViewById(R.id.btn_flash);
        mBtnSense = (ImageView) v.findViewById(R.id.btn_sense);

        btnCapture.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        btnCapture.setImageResource(R.drawable.btn_cam_pressed);
                        try {
                            takePicture();
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                        Log.i("setOnTouchListener", "MotionEvent.ACTION_DOWN");
                        break;
                    case MotionEvent.ACTION_UP:
                        btnCapture.setImageResource(R.drawable.btn_cam);
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
        btnIso.setOnClickListener(this);
        btnZoom.setOnClickListener(this);
        mBtnEffect.setOnClickListener(this);
        mBtnFlash.setOnClickListener(this);
        btnAlbum.setOnClickListener(this);
        mBtnSense.setOnClickListener(this);
        btnFlashLight.setOnClickListener(this);
        //switch
        MyOnCheckedChangeListener myOnClickChangeListener = new MyOnCheckedChangeListener();
        switchAf.setOnCheckedChangeListener(myOnClickChangeListener);
        switchAe.setOnCheckedChangeListener(myOnClickChangeListener);
        switchIso.setOnCheckedChangeListener(myOnClickChangeListener);
        switchAf.setChecked(true);
        switchAe.setChecked(true);
        switchIso.setChecked(true);
        //seekbar
        MySeekBarListener listener = new MySeekBarListener();
        sbAf.setOnSeekBarChangeListener(listener);
        sbAe.setOnSeekBarChangeListener(listener);
        sbIso.setOnSeekBarChangeListener(listener);
        sbZoom.setOnSeekBarChangeListener(listener);
        sbAf.setEnabled(false);
        sbIso.setEnabled(false);
        sbAf.setMax(100);
        sbAe.setMax(100);
        sbIso.setMax(100);
        sbZoom.setMax(100);
        sbAf.setProgress(50);
        sbAe.setProgress(50);
        sbIso.setProgress(50);
        sbZoom.setProgress(0);
        //list ,add
        mLayoutList = new ArrayList<View>();
        mLayoutList.add(mLayoutBottom);//位置0
        mLayoutList.add(mLayoutAf);//位置1
        mLayoutList.add(layoutAe);//位置2
        mLayoutList.add(layoutAwb);//位置3
        mLayoutList.add(mLayoutIso);//位置4
        mLayoutList.add(layoutZoom);//位置5
    }

    /**
     * 初始化seekbar上的一些参数
     */
    private void initSeekBarValue() {
        valueAF = 5.0f;
        valueAETime = (214735991 - 13231) / 2;
        valueISO = (10000 - 100) / 2;
        valueAE = 0;
    }

    /**
     * 初始化声音
     */
    private void initShutter() {
        mMediaActionSound = new MediaActionSound();
        mMediaActionSound.load(MediaActionSound.SHUTTER_CLICK);
    }

    /**
     * 这样做是为了获得mFocusImage的高度和宽度
     */
    private void initFocusImage() {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        mFocusImage.setLayoutParams(layoutParams);
        mFocusImage.initFocus();
    }

    private void initOutputSurface() {
        //图片格式
        mFormat = mSp.getInt("format", 256);
        //图片尺寸
        int sizeWidth = mSp.getInt("format_" + mFormat + "_pictureSize_width", 1280);
        int sizeHeight = mSp.getInt("format_" + mFormat + "_pictureSize_height", 960);
        //得到ImageReader对象,5为maxImage，放入队列里面的最大连拍张数(应该是这个意思)
        mImageReader = ImageReader.newInstance(mlargest.getWidth(), mlargest.getHeight(), mFormat, 2);
        if (mFormat == ImageFormat.JPEG) {
            mImageReader.setOnImageAvailableListener(new JpegReaderListener(), mHandler);
        } else if (mFormat == ImageFormat.RAW_SENSOR) {

        }
        //得到SurfaceTexture
        SurfaceTexture texture = mTextureView.getSurfaceTexture();
        //设置默认的图像缓冲区的大小
        texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        //显示的Surface
        mSurface = new Surface(texture);
        //得到surface
        mOutputSurfaces = new ArrayList<Surface>(2);
        mOutputSurfaces.add(mImageReader.getSurface());
        mOutputSurfaces.add(mSurface);
    }

    /**
     * 拍照
     */
    private void takePicture() throws CameraAccessException {
        //创建构建者，配置参数
        mCaptureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_ZERO_SHUTTER_LAG);
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
        previewBuilder2CaptureBuilder();
        //开启保存JPEG图片的线程
        if (mFormat == ImageFormat.JPEG) {
//            mHandler.post(new JpegSaver(mImageReader, mHandler, mFormat));
        }
        mState = STATE_CAPTURE;
        mCameraDevice.createCaptureSession(mOutputSurfaces, mSessionCaptureStateCallback, mHandler);
    }

    /**
     * 将previewBuilder中修改的参数设置到captureBuilder中
     */
    private void previewBuilder2CaptureBuilder() {
        //AWB
        mCaptureBuilder.set(CaptureRequest.CONTROL_AWB_MODE, mPreviewBuilder.get(CaptureRequest.CONTROL_AWB_MODE));
        //AE
//        if (mPreviewBuilder.get(CaptureRequest.CONTROL_AE_MODE) == CameraMetadata.CONTROL_AE_MODE_OFF) {
        //曝光时间
        mCaptureBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, mPreviewBuilder.get(CaptureRequest.SENSOR_EXPOSURE_TIME));
//        } else if (mPreviewBuilder.get(CaptureRequest.CONTROL_AE_MODE) == CameraMetadata.CONTROL_AE_MODE_ON) {
        //曝光增益
        mCaptureBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, mPreviewBuilder.get(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION));
//        }
        //AF
//        if (mPreviewBuilder.get(CaptureRequest.CONTROL_AF_MODE) == CameraMetadata.CONTROL_AF_MODE_OFF) {
        //手动聚焦的值
        mCaptureBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, mPreviewBuilder.get(CaptureRequest.LENS_FOCUS_DISTANCE));
//        }
        //effects
        mCaptureBuilder.set(CaptureRequest.CONTROL_EFFECT_MODE, mPreviewBuilder.get(CaptureRequest.CONTROL_EFFECT_MODE));
        //ISO
        mCaptureBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, mPreviewBuilder.get(CaptureRequest.SENSOR_SENSITIVITY));
        //AF REGIONS
        mCaptureBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, mPreviewBuilder.get(CaptureRequest.CONTROL_AF_REGIONS));
        mCaptureBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START);
        //AE REGIONS
        mCaptureBuilder.set(CaptureRequest.CONTROL_AE_REGIONS, mPreviewBuilder.get(CaptureRequest.CONTROL_AE_REGIONS));
        mCaptureBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START);
        //SCENSE
        mCaptureBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, mPreviewBuilder.get(CaptureRequest.CONTROL_SCENE_MODE));
        //zoom
        mCaptureBuilder.set(CaptureRequest.SCALER_CROP_REGION, mPreviewBuilder.get(CaptureRequest.SCALER_CROP_REGION));
    }

    /**
     * 初始化子线程和handler
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
    private void openCamera(int viewWidth, int viewHeight) throws CameraAccessException, InterruptedException {
        initHandler();//初始化子线程和handler
        //获得camera服务
        mCameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        setUpCameraOutputs(viewWidth, viewHeight);
        configureTransform(viewWidth, viewHeight);
        initOutputSurface();
        //打开相机
        mCameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mHandler);
        newPreviewSession();
    }

    /**
     * 生成PreviewSession对象
     */
    private void newPreviewSession() {
        mPreviewSessionCallback = new PreviewSessionCallback(mFocusImage, mMainHandler, mTextureView);
    }


    /**
     * 得到CameraCharacteristics等信息，设置显示大小
     *
     * @throws CameraAccessException
     */
    private void setUpCameraOutputs(int width, int height) throws CameraAccessException {

        if (mCameraId.equals("0")) {
            //描述CameraDevice属性
            mCameraCharacteristics = mCameraManager.getCameraCharacteristics(mCameraId);
            //流配置
            StreamConfigurationMap map = mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mlargest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new CompareSizesByArea());
            mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), width, height, mlargest);
        } else {
            mPreviewSize = new Size(1280, 720);
        }
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mTextureView.fitWindow(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        } else {
            mTextureView.fitWindow(mPreviewSize.getHeight(), mPreviewSize.getWidth());
        }
    }

    private Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<Size>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }

        // Pick the smallest of those, assuming we found any
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            Log.e("nonono", "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    private class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

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
     * 开始Preview
     */
    private void startPreview() {
        try {
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            //TEMPLATE_PREVIEW--->创建一个请求适合相机预览窗口。
            mPreviewBuilder.addTarget(mSurface);
            //初始化参数
            initPreviewBuilder();
            //3A--->auto
            mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            //3A
            mPreviewBuilder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_AUTO);
            mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
            mPreviewBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_AUTO);
            //创建拍照会话，一旦CameraCaptureSession创建,可以提交请求（capture、captureBurst,或setRepeatingBurst）。
            mState = STATE_PREVIEW;
            mCameraDevice.createCaptureSession(Arrays.asList(mSurface), mSessionPreviewStateCallback, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化预览的builder，这样做是为了一来就调iso的时候ae不为最低
     */
    private void initPreviewBuilder() {
        mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_OFF);
        mPreviewBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, valueAF);
        mPreviewBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, valueAETime);
        mPreviewBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, valueAE);
        mPreviewBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, valueISO);
    }

    /**
     * 换cameraID
     */
    private void reOpenCamera(int viewWidth, int viewHeight) {
        mFocusImage.stopFocus();
        if (mCameraId.equals("1")) {
            mCameraId = "0";
        } else if (mCameraId.equals("0")) {
            mCameraId = "1";
            mPreviewSize = new Size(1280, 720);
        } else {
            mCameraId = "0";
        }
        //关闭相机再开启另外个摄像头
        if (mCameraDevice != null) {
            mCameraDevice.close();
        }
        try {
            setUpCameraOutputs(viewWidth, viewHeight);
            configureTransform(viewWidth, viewHeight);
            mCameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
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

    /**
     * 继续预览
     */
    private void continuePreview() {
        mState = STATE_PREVIEW;
        try {
            mCameraDevice.createCaptureSession(Arrays.asList(mSurface), mSessionPreviewStateCallback, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置焦监听器
     * TextureView的touch
     * awbseekbar的change
     */
    private void setListener() {
        //开始显示的时候将触摸变焦监听器设置了
        mTextureView.setmMyTextureViewTouchEvent(new TextureViewTouchEvent(mCameraCharacteristics, mTextureView, mPreviewBuilder, mCameraCaptureSession, mHandler, mPreviewSessionCallback));
        mAwbSb.setmOnAwbSeekBarChangeListener(new AwbSeekBarChangeListener(getActivity(), mSeekBarTextView, mPreviewBuilder, mCameraCaptureSession, mHandler, mPreviewSessionCallback));
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i("onDestroy", "onDestroy");
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
     * Texture的监听器
     */
    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
            Log.i("SurfaceTextureListener", "onSurfaceTextureAvailable");
            mToPreviewWidth = i;
            mToPreviewHeight = i2;
            try {
                openCamera(i, i2);//打开相机
            } catch (CameraAccessException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
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
            Toast.makeText(getActivity(), "onDisconnected", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(CameraDevice cameraDevice, int i) {
            Log.i("CameraDevice.StateCallback", "onError--->" + i + ",,,null == cameraDevice--->" + (null == cameraDevice));
            Toast.makeText(getActivity(), "onError", Toast.LENGTH_SHORT).show();
        }
    };

    /**
     * 会话状态的回调
     * 继续preview
     */
    private CameraCaptureSession.StateCallback mSessionPreviewStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
            Log.i("CameraCaptureSession.StateCallback", "mSessionStateCallback--->onConfigured");
            try {
                mCameraCaptureSession = cameraCaptureSession;
                setListener();
                cameraCaptureSession.setRepeatingRequest(mPreviewBuilder.build(), mPreviewSessionCallback, mHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
            Log.i("CameraCaptureSession.StateCallback", "mSessionStateCallback--->onConfigureFailed");
            //丢弃掉所有在队列中的拍照的数据
            Toast.makeText(getActivity(), "onConfigureFailed---Preview", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onReady(CameraCaptureSession session) {
            super.onReady(session);
            Log.i("CameraCaptureSession.StateCallback", "mSessionStateCallback--->onReady");
        }

        @Override
        public void onActive(CameraCaptureSession session) {
            super.onActive(session);
            Log.i("CameraCaptureSession.StateCallback", "mSessionStateCallback--->onActive");
        }

        @Override
        public void onClosed(CameraCaptureSession session) {
            super.onClosed(session);
            Log.i("CameraCaptureSession.StateCallback", "mSessionStateCallback--->onClosed");
        }
    };

    private CameraCaptureSession.StateCallback mSessionCaptureStateCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(CameraCaptureSession session) {
            if (mFormat == ImageFormat.RAW_SENSOR) {
                try {
                    //连拍不稳定,capture也不太稳定
                    session.capture(mCaptureBuilder.build(),
                            new DngSessionCallback(getActivity(), mImageReader, mHandler, mMediaActionSound), mHandler);
//                        cameraCaptureSession.setRepeatingRequest(mCaptureBuilder.build(), new DngSessionCallback(getActivity(), mImageReader, mHandler, mMediaActionSound), mHandler);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            } else {
                try {
//                        cameraCaptureSession.setRepeatingRequest(mCaptureBuilder.build(), new JpegSessionCallback(mHandler, mMediaActionSound, mImageReader), mHandler);
                    session.setRepeatingBurst(Arrays.asList(mCaptureBuilder.build()),
                            new JpegSessionCallback(mHandler, mMediaActionSound, mImageReader), mHandler);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {
            Toast.makeText(getActivity(), "onConfigureFailed---Capture", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onClick(View view) {
        mFocusImage.stopFocus();
        switch (view.getId()) {
            case R.id.btn_setting:
                SettingFragment settingFragment = SettingFragment.newInstance();
                Bundle bundle = new Bundle();
                bundle.putString("camera", "camera" + mCameraId);
                settingFragment.setArguments(bundle);
                getActivity().getFragmentManager().beginTransaction().replace(R.id.frame_main, settingFragment).addToBackStack(null).commit();
                break;
            case R.id.btn_change_camera:
                reOpenCamera(mToPreviewWidth, mToPreviewHeight);
                break;
            case R.id.btn_focus:
                showAfFlag = !showAfFlag;
                showLayout(SHOW_AF, showAfFlag);
                break;
            case R.id.btn_ae:
                showAeFlag = !showAeFlag;
                showLayout(SHOW_AE, showAeFlag);
                break;
            case R.id.btn_awb:
                showAwbFlag = !showAwbFlag;
                showLayout(SHOW_AWB, showAwbFlag);
                break;
            case R.id.btn_iso:
                showIsoFlag = !showIsoFlag;
                showLayout(SHOW_ISO, showIsoFlag);
                break;
            case R.id.btn_effect:
                ListView lv = new ListView(getActivity());
                lv.setBackgroundColor(44000000);
                SimpleAdapter listItemAdapter = EffectAdapter.getAdapter(getActivity());
                lv.setAdapter(listItemAdapter);
                PopupWindow window = createPopupWindow(getActivity(), lv);
                lv.setOnItemClickListener(new EffectItemClickListener(mPreviewBuilder, mCameraCaptureSession, mHandler, window, mPreviewSessionCallback, mWindowTextView));
                int xoff = window.getWidth() / 2 - mBtnEffect.getWidth() / 2;
                window.update();
                window.showAsDropDown(mBtnEffect, -xoff, 0);
                break;
            case R.id.btn_flash:
                ListView lv2 = new ListView(getActivity());
                lv2.setBackgroundColor(44000000);
                SimpleAdapter listItemAdapter2 = FlashAdapter.getAdapter(getActivity());
                lv2.setAdapter(listItemAdapter2);
                PopupWindow window2 = createPopupWindow(getActivity(), lv2);
                lv2.setOnItemClickListener(new FlashItemClickListener(mPreviewBuilder, mCameraCaptureSession, mHandler, window2, mBtnFlash, mPreviewSessionCallback, mWindowTextView));
                int xoff2 = window2.getWidth() / 2 - mBtnFlash.getWidth() / 2;
                window2.update();
                window2.showAsDropDown(mBtnFlash, -xoff2, 0);
                break;
            case R.id.btn_zoom:
                showZoomFlag = !showZoomFlag;
                showLayout(SHOW_ZOOM, showZoomFlag);
                break;
            case R.id.btn_album:
                getActivity().getFragmentManager().beginTransaction().replace(R.id.frame_main, AlbumFragment.newInstance()).addToBackStack(null).commit();
                break;
            case R.id.btn_sense:
                ListView lv3 = new ListView(getActivity());
                lv3.setBackgroundColor(44000000);
                SimpleAdapter listItemAdapter3 = SenseAdapter.getAdapter(getActivity());
                lv3.setAdapter(listItemAdapter3);
                PopupWindow window3 = createPopupWindow(getActivity(), lv3);
                lv3.setOnItemClickListener(new SenseItemClickListener(mPreviewBuilder, mCameraCaptureSession, mHandler, window3, mPreviewSessionCallback, mWindowTextView));
                int xoff3 = window3.getWidth() / 2 - mBtnSense.getWidth() / 2;
                window3.update();
                window3.showAsDropDown(mBtnSense, -xoff3, 0);
                break;
            case R.id.btn_flashlight:
                Intent intent = new Intent(getActivity(), FlashActivity.class);
                getActivity().startActivity(intent);
                break;
        }
    }

    /**
     * seekbar的监听器
     */
    private class MySeekBarListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            if (mPreviewBuilder == null || getView() == null) {
                return;
            }
            switch (seekBar.getId()) {
                case R.id.sb_focus:
                    mPreviewBuilder.set(CaptureRequest.LENS_FOCUS_DISTANCE, (float) ((float) i / 10.0));
                    valueAF = (float) ((float) i / 10.0);
                    mSeekBarTextView.setText("聚焦：" + valueAF);
                    break;
                case R.id.sb_ae:
                    Switch switchAE = (Switch) getView().findViewById(R.id.switch_ae);
                    if (switchAE.isChecked()) {
                        //曝光增益
                        Range<Integer> range1 = mCameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
                        int maxmax = range1.getUpper();
                        int minmin = range1.getLower();
                        int all = (-minmin) + maxmax;
                        int time = 100 / all;
                        int ae = ((i / time) - maxmax) > maxmax ? maxmax : ((i / time) - maxmax) < minmin ? minmin : ((i / time) - maxmax);
                        mPreviewBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, ae);
                        mSeekBarTextView.setText("曝光增益：" + ae);
                        valueAE = ae;
                    } else {
                        //曝光时间
                        Range<Long> range = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE);
                        long max = range.getUpper();
                        long min = range.getLower();
                        long ae = ((i * (max - min)) / 100 + min);
                        mPreviewBuilder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, ae);
                        mSeekBarTextView.setText("曝光时间：" + ae);
                        valueAETime = ae;
                    }
                    break;
                case R.id.sb_iso:
                    Range<Integer> range = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);
                    int max1 = range.getUpper();//10000
                    int min1 = range.getLower();//100
                    int iso = ((i * (max1 - min1)) / 100 + min1);
                    mPreviewBuilder.set(CaptureRequest.SENSOR_SENSITIVITY, iso);
                    valueISO = iso;
                    mSeekBarTextView.setText("灵敏度：" + iso);
                    break;
                case R.id.sb_zoom:
                    Rect rect = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
                    int radio = mCameraCharacteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM).intValue() / 2;
                    int centerX = rect.centerX();
                    int centerY = rect.centerY();
                    Rect newRect = new Rect((i * centerX / radio) / 100 + 8, (i * centerY / radio) / 100 + 16, rect.right - ((i * centerX) / 100 / radio) - 1, rect.bottom - ((i * centerY) / 100 / radio) - 1);
                    mPreviewBuilder.set(CaptureRequest.SCALER_CROP_REGION, newRect);
                    mSeekBarTextView.setText("放大：" + i + "%");
                    break;
            }
            updatePreview();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mSeekBarTextView.setVisibility(View.VISIBLE);
            mSeekBarTextView.startAnimation(mAlphaInAnimation);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mSeekBarTextView.startAnimation(mAlphaOutAnimation);
            mSeekBarTextView.setVisibility(View.INVISIBLE);
        }


    }

    /**
     * switch的监听器
     */
    private class MyOnCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            //翻转的时候mPreviewBuilder变为了null,会挂掉
            if (mPreviewBuilder == null || getView() == null) {
                return;
            }
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
                    //AE是永远不会enable为false的
                    Switch switchISO = (Switch) getView().findViewById(R.id.switch_iso);
                    switchISO.setChecked(isChecked);
                    if (isChecked) {
                        mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
                        mLayoutIso.getChildAt(1).setEnabled(false);
                    } else {
                        mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_OFF);
                        mLayoutIso.getChildAt(1).setEnabled(true);
                    }
                    break;
                case R.id.switch_iso:
                    Switch switchAE = (Switch) getView().findViewById(R.id.switch_ae);
                    switchAE.setChecked(isChecked);
                    if (isChecked) {
                        mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
                        mLayoutIso.getChildAt(1).setEnabled(false);
                    } else {
                        mPreviewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_OFF);
                        mLayoutIso.getChildAt(1).setEnabled(true);
                    }
                    break;
            }
            updatePreview();
        }
    }

    /**
     * 显示和隐藏控件
     *
     * @param showWhat
     * @param showOrNot
     */
    private void showLayout(int showWhat, boolean showOrNot) {
        View v = mLayoutList.get(showWhat);
        if (showOrNot) {
            //全部隐藏但是AF/AE的显示出来
            for (int i = 0; i < mLayoutBottom.getChildCount(); i++) {
                if (mLayoutBottom.getChildAt(i).getVisibility() == View.VISIBLE) {
                    mLayoutBottom.getChildAt(i).setVisibility(View.INVISIBLE);
                }
            }
            v.startAnimation(mShowAction);
            v.setVisibility(View.VISIBLE);
        } else {
            //全部隐藏但是capture的显示出来
            for (int i = 0; i < mLayoutBottom.getChildCount(); i++) {
                if (mLayoutBottom.getChildAt(i).getVisibility() == View.VISIBLE) {
                    mLayoutBottom.getChildAt(i).setVisibility(View.INVISIBLE);
                }
            }
            mLayoutCapture.startAnimation(mShowAction);
            mLayoutCapture.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 创建一个包含自定义view的PopupWindow
     *
     * @param cx
     * @return
     */
    private PopupWindow createPopupWindow(Context cx, ListView lv) {
        PopupWindow window = new PopupWindow(cx);
        window.setContentView(lv);
        Resources res = cx.getResources();
        window.setWidth(res.getDimensionPixelOffset(R.dimen.popupwindow_width));
        window.setHeight(res.getDimensionPixelOffset(R.dimen.popupwindow_height) * (lv.getAdapter().getCount()));
        window.setFocusable(true); //设置PopupWindow可获得焦点
        window.setTouchable(true); //设置PopupWindow可触摸
        window.setOutsideTouchable(true); //设置非PopupWindow区域可触摸
        return window;
    }

}
