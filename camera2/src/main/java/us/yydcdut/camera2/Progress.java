package us.yydcdut.camera2;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.Type;
import android.util.Size;
import android.view.Surface;


/**
 * Created by yuyidong on 15-1-23.
 */
public class Progress {
    private Allocation mInputNormalAllocation;
    private Allocation mOutputAllocation;
    private Allocation mPrevAllocation;
    private HandlerThread mProcessingThread;
    private Handler mProcessingHandler;
    private Size mPreviewSize;
    private ScriptC_yuv2rgb mScriptC;


    private GetFrameBitmap mGetFrameBitmap = null;
    private GetFrameByteArray mGetFrameByteArray = null;

    public Progress(RenderScript rs, Size dimensions, GetFrameBitmap mGetFrameBitmap) {
        mPreviewSize = dimensions;
        this.mGetFrameBitmap = mGetFrameBitmap;

        cerateLooper();

        createAllcation(rs, dimensions);

        IOAllocation ioAllocation = new IOAllocation(mInputNormalAllocation);
        mScriptC = new ScriptC_yuv2rgb(rs);
        mScriptC.set_gPrevFrame(mPrevAllocation);

    }

    public Progress(RenderScript rs, Size dimensions, GetFrameByteArray mGetFrameByteArray) {
        mPreviewSize = dimensions;
        this.mGetFrameByteArray = mGetFrameByteArray;

        cerateLooper();

        createAllcation(rs, dimensions);

        mScriptC = new ScriptC_yuv2rgb(rs);
        mScriptC.set_gPrevFrame(mPrevAllocation);
        IOAllocation ioAllocation = new IOAllocation(mInputNormalAllocation);


    }

    public Progress(RenderScript rs, Size dimensions) {
        mPreviewSize = dimensions;

        cerateLooper();

        createAllcation(rs, dimensions);

        mScriptC = new ScriptC_yuv2rgb(rs);
        mScriptC.set_gPrevFrame(mPrevAllocation);
        IOAllocation ioAllocation = new IOAllocation(mInputNormalAllocation);


    }

    private void createAllcation(RenderScript rs, Size dimensions) {
        Type.Builder yuvTypeBuilder = new Type.Builder(rs, Element.YUV(rs));
        yuvTypeBuilder.setX(dimensions.getWidth());
        yuvTypeBuilder.setY(dimensions.getHeight());
        yuvTypeBuilder.setYuvFormat(ImageFormat.YUV_420_888);
        mInputNormalAllocation = Allocation.createTyped(rs, yuvTypeBuilder.create(),
                Allocation.USAGE_IO_INPUT | Allocation.USAGE_SCRIPT);

        Type.Builder rgbTypeBuilder = new Type.Builder(rs, Element.RGBA_8888(rs));
        rgbTypeBuilder.setX(dimensions.getWidth());
        rgbTypeBuilder.setY(dimensions.getHeight());
        mPrevAllocation = Allocation.createTyped(rs, rgbTypeBuilder.create(),
                Allocation.USAGE_SCRIPT);
        mOutputAllocation = Allocation.createTyped(rs, rgbTypeBuilder.create(),
                Allocation.USAGE_IO_OUTPUT | Allocation.USAGE_SCRIPT);

    }

    private void cerateLooper() {
        mProcessingThread = new HandlerThread("ViewfinderProcessor");
        mProcessingThread.start();
        mProcessingHandler = new Handler(mProcessingThread.getLooper());
    }


    public Surface getInputNormalSurface() {
        return mInputNormalAllocation.getSurface();
    }

    public void setOutputSurface(Surface output) {
        mOutputAllocation.setSurface(output);
    }


    class IOAllocation implements Allocation.OnBufferAvailableListener {

        private Allocation mInputAllocation;

        IOAllocation(Allocation mInputAllocation) {
            this.mInputAllocation = mInputAllocation;
            this.mInputAllocation.setOnBufferAvailableListener(this);
        }


        @Override
        public void onBufferAvailable(Allocation a) {
            synchronized (this) {
                // Get to newest input
                mInputAllocation.ioReceive();
                mScriptC.set_gCurrentFrame(mInputAllocation);

                // Run processing pass
                mScriptC.forEach_yuv2rgbFrames(mPrevAllocation, mOutputAllocation);
                mOutputAllocation.ioSend();
                if (mGetFrameBitmap != null) {
                    Bitmap bitmap = Bitmap.createBitmap(mPreviewSize.getWidth(), mPreviewSize.getHeight(), Bitmap.Config.ARGB_8888);
                    mOutputAllocation.copyTo(bitmap);
                    mGetFrameBitmap.getBitmap(bitmap);
                } else if (mGetFrameByteArray != null) {
                    byte[] b = new byte[mPreviewSize.getWidth() * mPreviewSize.getHeight() * 2];
                    mOutputAllocation.copyTo(b);
                    mGetFrameByteArray.getByteArray(b);
                }
            }
        }
    }

    public interface GetFrameBitmap {
        public void getBitmap(Bitmap bitmap);
    }

    public interface GetFrameByteArray {
        public void getByteArray(byte[] bytes);
    }

}
