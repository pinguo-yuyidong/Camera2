package us.yydcdut.camera2;

import android.graphics.ImageFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.Type;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

/**
 * Created by yuyidong on 15-1-23.
 */
public class Progress {
    private Allocation mInputNormalAllocation;
    private Allocation mOutputAllocation;
    private Allocation mPrevAllocation;
    private Size mSize;
    private HandlerThread mProcessingThread;
    private Handler mProcessingHandler;
    private ScriptC_Normal mScriptC;

    public Progress(RenderScript rs, Size dimensions) {

        mSize = dimensions;

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

        mProcessingThread = new HandlerThread("ViewfinderProcessor");
        mProcessingThread.start();
        mProcessingHandler = new Handler(mProcessingThread.getLooper());

        IOAllocation ioAllocation = new IOAllocation(mInputNormalAllocation);
        mScriptC = new ScriptC_Normal(rs);
        mScriptC.set_gPrevFrame(mPrevAllocation);
    }

    public Surface getInputNormalSurface() {
        return mInputNormalAllocation.getSurface();
    }

    public void setOutputSurface(Surface output) {
        mOutputAllocation.setSurface(output);
    }


    class IOAllocation implements Runnable, Allocation.OnBufferAvailableListener {
        private int mPendingFrames = 0;
        private int mFrameCounter = 0;

        private Allocation mInputAllocation;

        IOAllocation(Allocation mInputAllocation) {
            this.mInputAllocation = mInputAllocation;
            this.mInputAllocation.setOnBufferAvailableListener(this);
        }

        @Override
        public void run() {
            // Find out how many frames have arrived
            int pendingFrames;
            synchronized (this) {
                pendingFrames = mPendingFrames;
                mPendingFrames = 0;

                // Discard extra messages in case processing is slower than frame rate
                mProcessingHandler.removeCallbacks(this);
            }

            // Get to newest input
            for (int i = 0; i < pendingFrames; i++) {
                mInputAllocation.ioReceive();
            }

            mScriptC.set_gFrameCounter(mFrameCounter++);
            mScriptC.set_gCurrentFrame(mInputAllocation);
            mScriptC.set_gCutPointX(0);
            mScriptC.set_gDoMerge(0);

            // Run processing pass
            mScriptC.forEach_mergeHdrFrames(mPrevAllocation, mOutputAllocation);
            mOutputAllocation.ioSend();
        }

        @Override
        public void onBufferAvailable(Allocation a) {
            Log.i("onBufferAvailable", "onBufferAvailable");
            synchronized (this) {
                mPendingFrames++;
                mProcessingHandler.post(this);
            }
        }
    }


}
