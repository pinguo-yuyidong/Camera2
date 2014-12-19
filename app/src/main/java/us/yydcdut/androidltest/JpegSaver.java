package us.yydcdut.androidltest;

import android.graphics.ImageFormat;
import android.media.Image;
import android.media.ImageReader;
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
 * Created by yuyidong on 14-12-5.
 */
public class JpegSaver implements Runnable {

    private ImageReader mImageReader;
    private Handler mHandler;
    private int mFormat;

    public JpegSaver(ImageReader mImageReader, Handler mHandler, int format) {
        this.mImageReader = mImageReader;
        this.mHandler = mHandler;
        this.mFormat = format;
    }

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
            if (mFormat == ImageFormat.JPEG) {
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
        }
    };

    /**
     * 判断父文件是否存在
     */
    private void checkParentDir() {
        Log.i("JpegSaver", "checkParentDir");
        File dir = new File(Environment.getExternalStorageDirectory() + "/Android_L_Test/");
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    /**
     * 判断文件夹是否存在
     */
    private void checkJpegDir() {
        Log.i("JpegSaver", "checkJpegDir");
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
