package us.yydcdut.camera2;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.Type;
import android.util.Size;
import android.view.Surface;


/**
 * Created by yuyidong on 15-1-23.
 */
public class Progress implements Allocation.OnBufferAvailableListener {
    private Allocation mInputNormalAllocation;
    private Allocation mOutputAllocation;
    private Allocation mPrevAllocation;
    private Size mPreviewSize;
    private ScriptC_yuv2rgb mScriptC;


    private GetFrameBitmap mGetFrameBitmap = null;
    private GetFrameByteArray mGetFrameByteArray = null;

    public Progress(RenderScript rs, Size dimensions, GetFrameBitmap mGetFrameBitmap) {
        mPreviewSize = dimensions;
        this.mGetFrameBitmap = mGetFrameBitmap;

        createAllcation(rs, dimensions);

        mInputNormalAllocation.setOnBufferAvailableListener(this);

        mScriptC = new ScriptC_yuv2rgb(rs);
        mScriptC.set_gPrevFrame(mPrevAllocation);
        mScriptC.set_gCurrentFrame(mInputNormalAllocation);
    }

    public Progress(RenderScript rs, Size dimensions, GetFrameByteArray mGetFrameByteArray) {
        mPreviewSize = dimensions;
        this.mGetFrameByteArray = mGetFrameByteArray;

        createAllcation(rs, dimensions);

        mScriptC = new ScriptC_yuv2rgb(rs);
        mScriptC.set_gPrevFrame(mPrevAllocation);
        mScriptC.set_gCurrentFrame(mInputNormalAllocation);
        mInputNormalAllocation.setOnBufferAvailableListener(this);

    }

    public Progress(RenderScript rs, Size dimensions) {
        mPreviewSize = dimensions;

        createAllcation(rs, dimensions);

        mScriptC = new ScriptC_yuv2rgb(rs);
        mScriptC.set_gPrevFrame(mPrevAllocation);
        mScriptC.set_gCurrentFrame(mInputNormalAllocation);
        mInputNormalAllocation.setOnBufferAvailableListener(this);
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


    public Surface getInputNormalSurface() {
        return mInputNormalAllocation.getSurface();
    }

    public void setOutputSurface(Surface output) {
        mOutputAllocation.setSurface(output);
    }

    @Override
    public void onBufferAvailable(Allocation a) {
        // Get to newest input
        mInputNormalAllocation.ioReceive();

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

    public interface GetFrameBitmap {
        public void getBitmap(Bitmap bitmap);
    }

    public interface GetFrameByteArray {
        public void getByteArray(byte[] bytes);
    }

}
