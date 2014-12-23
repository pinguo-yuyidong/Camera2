package us.yydcdut.androidltest;

import android.os.Handler;
import android.os.Message;

/**
 * Created by yuyidong on 14-12-19.
 */
public class SleepThread implements Runnable {
    private Handler mMainHandler;
    private int what;
    private long mTime;
    private Object mObject;

    public SleepThread(Handler mainHandler, int what, long mTime, Object mObject) {
        this.mMainHandler = mainHandler;
        this.what = what;
        this.mTime = mTime;
        this.mObject = mObject;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(mTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Message message = mMainHandler.obtainMessage();
        message.what = what;
        message.obj = mObject;
        mMainHandler.sendMessage(message);

    }
}
