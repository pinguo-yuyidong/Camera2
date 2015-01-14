package us.yydcdut.camera2;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.DngCreator;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * Created by yuyidong on 15-1-12.
 */
public class DngImageSaver implements Runnable {
    private Context mContext;
    private ImageReader mRawImageReader;
    private CaptureResult mResult;

    public DngImageSaver(Context mContext, ImageReader mRawImageReader, CaptureResult mResult) {
        this.mContext = mContext;
        this.mRawImageReader = mRawImageReader;
        this.mResult = mResult;
    }


    @Override
    public void run() {
        if (mRawImageReader == null) {
            Log.i("mRawImageReader", "mRawImageReader===null");
            return;
        }
        DngCreator dngCreator = null;
        Image image = mRawImageReader.acquireNextImage();
        while (image == null) {
            image = mRawImageReader.acquireNextImage();
        }
        if (image == null) {
            Log.i("image", "image===null");
            return;
        }
        CameraManager manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        try {
            //前置摄像头没有dng
            CameraCharacteristics cameraCharacteristics = manager.getCameraCharacteristics("0");
            dngCreator = new DngCreator(cameraCharacteristics, mResult);
            dngCreator.writeImage(new FileOutputStream(createFile()), image);
            Log.i("DngImageSaver", createFile().getAbsolutePath());
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("DngRunnable", "Exception eException eException e");
        } finally {
            System.gc();
            try {
                image.close();
                dngCreator.close();
            } catch (Exception e) {
                Log.e("DngRunnable", "dngCreator.close();dngCreator.close();");
            }
        }
    }

    private File createFile() {
        File dir = new File(Environment.getExternalStorageDirectory() + "/Android_L_Test/");
        if (!dir.exists()) {
            dir.mkdir();
        }
        long time = System.currentTimeMillis();
        int random = new Random().nextInt(1000);
        File dir1 = new File(Environment.getExternalStorageDirectory() + "/Android_L_Test/dng/");
        if (!dir1.exists()) {
            dir1.mkdir();
        }
        File meidaFile = new File(dir1, time + "_" + random + ".dng");
        return meidaFile;
    }
}
