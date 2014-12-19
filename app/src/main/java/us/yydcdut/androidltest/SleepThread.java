package us.yydcdut.androidltest;

import android.os.Handler;

/**
 * Created by yuyidong on 14-12-19.
 */
public class SleepThread implements Runnable {
    private Handler mHandler;
    private int what;
    private long mTime;

    public SleepThread(Handler mHandler, int what, long mTime) {
        this.mHandler = mHandler;
        this.what = what;
        this.mTime = mTime;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(mTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mHandler.sendEmptyMessage(what);
    }
}
