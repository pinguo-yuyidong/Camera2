package us.yydcdut.androidltest.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.TextureView;

/**
 * Created by yuyidong on 14-12-10.
 */
public class MyTextureView extends TextureView {
    private int mRatioWidth = 0;
    private int mRatioHeight = 0;
    private MyTextureViewTouchEvent mMyTextureViewTouchEvent;

    public MyTextureView(Context context) {
        super(context);
    }

    public MyTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * mRatioWidth / mRatioHeight) {
                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
            } else {
                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
            }
        }
    }

    public void fitWindow(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        mRatioWidth = width;
        mRatioHeight = height;
        requestLayout();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mMyTextureViewTouchEvent.onAreaTouchEvent(event);
    }

    public void setmMyTextureViewTouchEvent(MyTextureViewTouchEvent myTextureViewTouchEvent) {
        this.mMyTextureViewTouchEvent = myTextureViewTouchEvent;
    }

    public interface MyTextureViewTouchEvent {
        public boolean onAreaTouchEvent(MotionEvent event);
    }

}
