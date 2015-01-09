package us.yydcdut.androidltest.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import us.yydcdut.androidltest.R;


/**
 * Created by marui on 13-12-3.
 */
public class TitleView extends RelativeLayout {

    //Views
    private View mTitleBackBtn;
    private TextView mTitleText;
    private TextView mTitleRightBtn;
    //
    private OnTitleViewClickListener mClickListener;

    public TitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mTitleText = (TextView) findViewById(R.id.title_text_title);
        mTitleBackBtn = findViewById(R.id.title_back_btn);
        mTitleRightBtn = (TextView) findViewById(R.id.title_right_btn);


        if (isInEditMode()) {
            return;
        }

        mTitleBackBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mClickListener != null) {
                    mClickListener.onBackClick();
                }
            }
        });

        mTitleRightBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mClickListener != null) {
                    mClickListener.onRightBtnClick();
                }
            }
        });
    }


    public TitleView(Context context) {
        this(context, null);
    }

    public void hideRightBtn() {
        if (mTitleRightBtn != null) {
            mTitleRightBtn.setVisibility(View.GONE);
        }
    }

    public void showRightBtn() {
        if (mTitleRightBtn != null) {
            mTitleRightBtn.setVisibility(View.VISIBLE);
        }
    }

//    public void showRightImageBtn() {
//        if (mTitleRightImageBtn != null) {
//            mTitleRightImageBtn.setVisibility(View.VISIBLE);
//        }
//    }

    public void hideBackBtn() {
        if (mTitleBackBtn != null) {
            mTitleBackBtn.setVisibility(View.GONE);
        }
    }


    public void setTiTleText(int resId) {
        mTitleText.setText(resId);
    }

    public void setTiTleText(CharSequence text) {
        mTitleText.setText(text);
    }

    public void setRightBtnText(int resId) {
        mTitleRightBtn.setText(resId);
    }

    public String getRightBtnText() {
        return (String) mTitleRightBtn.getText();
    }

    public void setRightBtnEnable(boolean isEnable) {
        mTitleRightBtn.setEnabled(isEnable);
    }

    public void setRightBtnClickState(boolean state) {
        mTitleRightBtn.setClickable(state);
        mTitleRightBtn.setEnabled(state);
    }

    public void setRightBtnText(CharSequence text) {
        mTitleRightBtn.setText(text);
    }

    public void setOnTitleViewClickListener(OnTitleViewClickListener listener) {
        mClickListener = listener;
    }

    public interface OnTitleViewClickListener {
        public void onBackClick();

        public void onRightBtnClick();
    }
}
