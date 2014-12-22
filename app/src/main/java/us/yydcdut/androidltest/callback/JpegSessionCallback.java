package us.yydcdut.androidltest.callback;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaActionSound;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Random;

/**
 * Created by yuyidong on 14-12-17.
 */
public class JpegSessionCallback extends CameraCaptureSession.CaptureCallback {
    private Handler mHandler;
    private MediaActionSound mMediaActionSound;
    private ImageReader mImageReader;

    public JpegSessionCallback(Handler mHandler, MediaActionSound mMediaActionSound, ImageReader mImageReader) {
        this.mHandler = mHandler;
        this.mMediaActionSound = mMediaActionSound;
        this.mImageReader = mImageReader;
    }

    @Override
    public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
        Log.i("JpegSessionCallback", "onCaptureCompleted");
        super.onCaptureCompleted(session, request, result);
        Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
        if (afState == null || aeState == null) {
            return;
        }
        Log.i("JpegSessionCallback", "要开始进入了,,afState.intValue()--->" + afState.intValue());
        //聚焦完成才能拍照
        if (afState.intValue() == CameraMetadata.CONTROL_AF_STATE_FOCUSED_LOCKED || afState.intValue() == CameraMetadata.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
            Log.i("JpegSessionCallback", "进去了一层,,aeState.intValue()--->" + aeState.intValue());
            if (aeState.intValue() == CameraMetadata.CONTROL_AE_STATE_LOCKED || aeState == CameraMetadata.CONTROL_AE_STATE_PRECAPTURE) {
                Log.i("JpegSessionCallback", "进去了两层");
//                mHandler.post(new JpegSaver());
                new Thread(new JpegSaver()).start();
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mMediaActionSound.play(MediaActionSound.SHUTTER_CLICK);
                    }
                });

            }
        }
    }


    class JpegSaver implements Runnable {

        @Override
        public void run() {
            Log.i("JpegSaver", "JpegSaverJpegSaver");
            mImageReader.setOnImageAvailableListener(ReaderListener, mHandler);
        }

        /**
         * 通过不同format来保存图片的回调
         */
        ImageReader.OnImageAvailableListener ReaderListener = new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader imageReader) {
                Image image = imageReader.acquireLatestImage();
                checkParentDir();
                File file;
                checkJpegDir();
                file = createJpeg();
                //这里到最后会出现null,所有trycatch一下
                try {
                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);
                    try {
                        save(bytes, file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    image.close();
                } catch (Exception e) {
                    Log.i("Exception e", "ImageFormat.JPEG,,,,,,,,Exception eException e");
                    e.getStackTrace();
                }
            }
        };

        /**
         * 判断父文件是否存在
         */
        private void checkParentDir() {
            File dir = new File(Environment.getExternalStorageDirectory() + "/Android_L_Test/");
            if (!dir.exists()) {
                dir.mkdir();
            }
        }

        /**
         * 判断文件夹是否存在
         */
        private void checkJpegDir() {
            File dir = new File(Environment.getExternalStorageDirectory() + "/Android_L_Test/jpeg/");
            if (!dir.exists()) {
                dir.mkdir();
            }
        }


        /**
         * 创建jpeg的文件
         *
         * @return
         */
        private File createJpeg() {
            long time = System.currentTimeMillis();
            int random = new Random().nextInt(1000);
            File dir = new File(Environment.getExternalStorageDirectory() + "/Android_L_Test/jpeg/");
            Log.i("JpegSaver", time + "_" + random + ".jpg");
            return new File(dir, time + "_" + random + ".jpg");
        }


        /**
         * 保存
         *
         * @param bytes
         * @param file
         * @throws IOException
         */
        private void save(byte[] bytes, File file) throws IOException {
            Log.i("JpegSaver", "save");
            OutputStream os = null;
            try {
                os = new FileOutputStream(file);
                os.write(bytes);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (os != null) {
                    os.close();
                }
            }
        }
    }
}
