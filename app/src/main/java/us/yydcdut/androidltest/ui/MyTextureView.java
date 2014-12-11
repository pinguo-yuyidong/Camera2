package us.yydcdut.androidltest.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

/**
 * Created by yuyidong on 14-12-10.
 */
public class MyTextureView extends TextureView {
    private int mWidth = 0;
    private int mHeight = 0;

    public MyTextureView(Context context) {
        super(context);
    }

    public MyTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MyTextureView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (mWidth == 0 || mHeight == 0) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * mWidth / mHeight) {
                setMeasuredDimension(width, width * mHeight / mWidth);
            } else {
                setMeasuredDimension(height * mWidth / mHeight, height);
            }
        }
//        Log.i("MyTextureView--->onMeasure", "widthMeasureSpec--->" + widthMeasureSpec + ",,,heightMeasureSpec--->" + heightMeasureSpec);
//        Log.i("MyTextureView--->onMeasure", "width--->" + width + ",,,height--->" + height);
    }

    public void fitWindow(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        mWidth = width;
        mHeight = height;
        requestLayout();
    }
}
