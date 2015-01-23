package us.yydcdut.camera2;

import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.FieldPacker;
import android.renderscript.RSRuntimeException;
import android.renderscript.RenderScript;
import android.renderscript.Script;
import android.renderscript.ScriptC;
import android.renderscript.Type;

/**
 * Created by yuyidong on 15-1-23.
 */
public class ScriptC_Normal extends ScriptC {
    private static final String __rs_resource_name = "hdr_merge";

    public ScriptC_Normal(RenderScript rs) {
        super(rs,
                __rs_resource_name,
                hdr_mergeBitCode.getBitCode32(),
                hdr_mergeBitCode.getBitCode64());
        __ALLOCATION = Element.ALLOCATION(rs);
        mExportVar_gCutPointX = 0;
        __I32 = Element.I32(rs);
        mExportVar_gDoMerge = 0;
        mExportVar_gFrameCounter = 0;
        __U8_4 = Element.U8_4(rs);
    }

    private Element __ALLOCATION;
    private Element __I32;
    private Element __U8_4;
    private FieldPacker __rs_fp_ALLOCATION;
    private FieldPacker __rs_fp_I32;


    private final static int mExportVarIdx_gPrevFrame = 1;
    private Allocation mExportVar_gPrevFrame;

    public synchronized void set_gPrevFrame(Allocation v) {
        setVar(mExportVarIdx_gPrevFrame, v);
        mExportVar_gPrevFrame = v;
    }

    private final static int mExportVarIdx_gFrameCounter = 4;
    private int mExportVar_gFrameCounter;

    public synchronized void set_gFrameCounter(int v) {
        setVar(mExportVarIdx_gFrameCounter, v);
        mExportVar_gFrameCounter = v;
    }

    private final static int mExportVarIdx_gCurrentFrame = 0;
    private Allocation mExportVar_gCurrentFrame;

    public synchronized void set_gCurrentFrame(Allocation v) {
        setVar(mExportVarIdx_gCurrentFrame, v);
        mExportVar_gCurrentFrame = v;
    }

    private final static int mExportVarIdx_gCutPointX = 2;
    private int mExportVar_gCutPointX;

    public synchronized void set_gCutPointX(int v) {
        setVar(mExportVarIdx_gCutPointX, v);
        mExportVar_gCutPointX = v;
    }

    private final static int mExportVarIdx_gDoMerge = 3;
    private int mExportVar_gDoMerge;

    public synchronized void set_gDoMerge(int v) {
        setVar(mExportVarIdx_gDoMerge, v);
        mExportVar_gDoMerge = v;
    }

    public void forEach_mergeHdrFrames(Allocation ain, Allocation aout) {
        forEach_mergeHdrFrames(ain, aout, null);
    }

    public void forEach_mergeHdrFrames(Allocation ain, Allocation aout, Script.LaunchOptions sc) {
        // check ain
        if (!ain.getType().getElement().isCompatible(__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        }
        // check aout
        if (!aout.getType().getElement().isCompatible(__U8_4)) {
            throw new RSRuntimeException("Type mismatch with U8_4!");
        }
        Type t0, t1;        // Verify dimensions
        t0 = ain.getType();
        t1 = aout.getType();
        if ((t0.getCount() != t1.getCount()) ||
                (t0.getX() != t1.getX()) ||
                (t0.getY() != t1.getY()) ||
                (t0.getZ() != t1.getZ()) ||
                (t0.hasFaces() != t1.hasFaces()) ||
                (t0.hasMipmaps() != t1.hasMipmaps())) {
            throw new RSRuntimeException("Dimension mismatch between parameters ain and aout!");
        }

        forEach(mExportForEachIdx_mergeHdrFrames, ain, aout, null, sc);
    }

    //private final static int mExportForEachIdx_root = 0;
    private final static int mExportForEachIdx_mergeHdrFrames = 1;

    public Script.KernelID getKernelID_mergeHdrFrames() {
        return createKernelID(mExportForEachIdx_mergeHdrFrames, 59, null, null);
    }
}
