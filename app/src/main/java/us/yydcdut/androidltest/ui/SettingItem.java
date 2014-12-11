package us.yydcdut.androidltest.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import us.yydcdut.androidltest.R;

public class SettingItem extends RelativeLayout {
    private TextView mTitle;
    private ImageView mIcon;
    private ImageView mImage;
    private ImageView mNew;

    public SettingItem(Context context) {
        super(context);
    }

    public SettingItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SettingItem(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        initView(context, attributeSet);
    }

    private void initView(Context context, AttributeSet attributeSet) {
        TypedArray a = context.obtainStyledAttributes(attributeSet, R.styleable.SettingItem);
        float textSize = a.getDimension(R.styleable.SettingItem_customTextSize, 15f);
        int textColor = a.getColor(R.styleable.SettingItem_customTextColor, Color.parseColor("#7b8085"));

        View root = LayoutInflater.from(context).inflate(R.layout.setting_item_layout, this);
        mTitle = (TextView) root.findViewById(R.id.title);
        mTitle.setTextSize(textSize);
        mTitle.setTextColor(textColor);
        mIcon = (ImageView) root.findViewById(R.id.option_item_icon);
        mImage = (ImageView) root.findViewById(R.id.image);
        mNew = (ImageView) root.findViewById(R.id.image_new);
    }

    public TextView getTitle() {
        return mTitle;
    }

    public ImageView getIcon() {
        return mIcon;
    }

    public ImageView getNextImage() {
        return mImage;
    }

    public ImageView getNewImage() {
        return mNew;
    }
}
