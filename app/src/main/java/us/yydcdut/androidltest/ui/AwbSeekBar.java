package us.yydcdut.androidltest.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.SeekBar;

/**
 * Created by yuyidong on 14-12-15.
 */
public class AwbSeekBar extends SeekBar {

    private int mProgress;
    private AwbSeekBar mAwbSeekBar = this;
    private OnAwbSeekBarChangeListener mOnAwbSeekBarChangeListener;

    public AwbSeekBar(Context context) {
        super(context);
        init();
    }

    public AwbSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AwbSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public AwbSeekBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void setmOnAwbSeekBarChangeListener(OnAwbSeekBarChangeListener mOnAwbSeekBarChangeListener) {
        this.mOnAwbSeekBarChangeListener = mOnAwbSeekBarChangeListener;
    }

    private void init() {
        this.setMax(70);
        this.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mProgress = progress;
                if (mOnAwbSeekBarChangeListener != null) {
                    if (0 <= mProgress && mProgress < 5) {
                        mOnAwbSeekBarChangeListener.doInProgress1();
                    } else if (5 <= mProgress && mProgress < 15) {
                        mOnAwbSeekBarChangeListener.doInProgress2();
                    } else if (15 <= mProgress && mProgress < 25) {
                        mOnAwbSeekBarChangeListener.doInProgress3();
                    } else if (25 <= mProgress && mProgress < 35) {
                        mOnAwbSeekBarChangeListener.doInProgress4();
                    } else if (35 <= mProgress && mProgress < 45) {
                        mOnAwbSeekBarChangeListener.doInProgress5();
                    } else if (45 <= mProgress && mProgress < 55) {
                        mOnAwbSeekBarChangeListener.doInProgress6();
                    } else if (55 <= mProgress && mProgress < 65) {
                        mOnAwbSeekBarChangeListener.doInProgress7();
                    } else if (65 <= mProgress && mProgress < 70) {
                        mOnAwbSeekBarChangeListener.doInProgress8();
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mOnAwbSeekBarChangeListener.onStartTrackingTouch(seekBar);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int num = 0;
                if (0 <= mProgress && mProgress < 5) {
                    mAwbSeekBar.setProgress(0);
                    num = 0;
                } else if (5 <= mProgress && mProgress < 15) {
                    mAwbSeekBar.setProgress(10);
                    num = 10;
                } else if (15 <= mProgress && mProgress < 25) {
                    mAwbSeekBar.setProgress(20);
                    num = 20;
                } else if (25 <= mProgress && mProgress < 35) {
                    mAwbSeekBar.setProgress(30);
                    num = 30;
                } else if (35 <= mProgress && mProgress < 45) {
                    mAwbSeekBar.setProgress(40);
                    num = 40;
                } else if (45 <= mProgress && mProgress < 55) {
                    mAwbSeekBar.setProgress(50);
                    num = 50;
                } else if (55 <= mProgress && mProgress < 65) {
                    mAwbSeekBar.setProgress(60);
                    num = 60;
                } else if (65 <= mProgress && mProgress < 70) {
                    mAwbSeekBar.setProgress(70);
                    num = 70;
                }
                if (mOnAwbSeekBarChangeListener != null) {
                    mOnAwbSeekBarChangeListener.onStopTrackingTouch(num);
                }
            }
        });
    }

    public interface OnAwbSeekBarChangeListener {
        public abstract void doInProgress1();

        public abstract void doInProgress2();

        public abstract void doInProgress3();

        public abstract void doInProgress4();

        public abstract void doInProgress5();

        public abstract void doInProgress6();

        public abstract void doInProgress7();

        public abstract void doInProgress8();

        public abstract void onStopTrackingTouch(int num);

        public abstract void onStartTrackingTouch(SeekBar seekBar);
    }

}
