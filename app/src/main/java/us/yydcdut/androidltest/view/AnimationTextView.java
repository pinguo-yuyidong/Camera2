package us.yydcdut.androidltest.view;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.widget.TextView;

import us.yydcdut.androidltest.SleepThread;

/**
 * Created by yuyidong on 14-12-23.
 */
public class AnimationTextView extends TextView {
    private Handler mMainHandler;
    private Animation mAnimation;
    /**
     * 防止又换了个text，但是上次哪个还没有消失即将小时就把新的text的给消失了
     */
    public int mTimes = 0;

    public AnimationTextView(Context context) {
        super(context);
    }

    public AnimationTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AnimationTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AnimationTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setmMainHandler(Handler mMainHandler) {
        this.mMainHandler = mMainHandler;
    }

    public void setmAnimation(Animation mAnimation) {
        this.mAnimation = mAnimation;
    }

    public void start(String text, int message) {
        if (mAnimation == null || mMainHandler == null) {
            return;
        }
        this.setVisibility(VISIBLE);
        mTimes++;
        this.setText(text);
        this.startAnimation(mAnimation);
        new Thread(new SleepThread(mMainHandler, message, 1000, Integer.valueOf(mTimes))).start();
    }

    public void stop() {
        this.setVisibility(GONE);
    }
}
