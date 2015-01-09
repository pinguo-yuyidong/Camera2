package us.yydcdut.androidltest.view;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;

import us.yydcdut.androidltest.R;
import us.yydcdut.androidltest.SleepThread;
import us.yydcdut.androidltest.ui.DisplayFragment;

/**
 * Created by yuyidong on 14-12-23.
 */
public class AnimationImageView extends ImageView {
    private Handler mMainHandler;
    private Animation mAnimation;
    private Context mContext;
    /**
     * 防止又换了个text，但是上次哪个还没有消失即将小时就把新的text的给消失了
     */
    public int mTimes = 0;

    public AnimationImageView(Context context) {
        super(context);
        mContext = context;
    }

    public AnimationImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public AnimationImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public AnimationImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
    }

    public void setmMainHandler(Handler mMainHandler) {
        this.mMainHandler = mMainHandler;
    }

    public void setmAnimation(Animation mAnimation) {
        this.mAnimation = mAnimation;
    }

    public void initFocus() {
        this.setVisibility(VISIBLE);
        new Thread(new SleepThread(mMainHandler, DisplayFragment.FOCUS_DISAPPEAR, 1000, null)).start();
    }

    public void startFocusing() {
        mTimes++;
        this.setVisibility(View.VISIBLE);
        this.startAnimation(mAnimation);
        this.setBackground(mContext.getDrawable(R.drawable.focus));
        new Thread(new SleepThread(mMainHandler, DisplayFragment.FOCUS_DISAPPEAR, 1000, Integer.valueOf(mTimes))).start();
    }

    public void focusFailed() {
        mTimes++;
        this.setBackground(mContext.getDrawable(R.drawable.focus_failed));
        new Thread(new SleepThread(mMainHandler, DisplayFragment.FOCUS_DISAPPEAR, 800, Integer.valueOf(mTimes))).start();
    }

    public void focusSuccess() {
        mTimes++;
        this.setVisibility(View.VISIBLE);
        this.setBackground(mContext.getDrawable(R.drawable.focus_succeed));
        new Thread(new SleepThread(mMainHandler, DisplayFragment.FOCUS_DISAPPEAR, 800, Integer.valueOf(mTimes))).start();
    }

    public void stopFocus() {
        this.setVisibility(INVISIBLE);
    }
}
