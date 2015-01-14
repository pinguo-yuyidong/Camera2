package us.yydcdut.camera2.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import us.yydcdut.camera2.PreferenceHelper;
import us.yydcdut.camera2.R;

/**
 * Created by yuyidong on 15-1-12.
 */
public class ChangeCameraActivity extends Activity {
    private Context mContext = ChangeCameraActivity.this;
    private Switch mSwitchCameraFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity);

        Switch switchCameraId = (Switch) findViewById(R.id.switch_camera);
        String cameraId = PreferenceHelper.getCameraId(mContext);
        if (cameraId.equals("0")) {
            switchCameraId.setChecked(false);
        } else {
            switchCameraId.setChecked(true);
        }
        switchCameraId.setOnCheckedChangeListener(mListener);

        mSwitchCameraFormat = (Switch) findViewById(R.id.switch_format);
        String cameraFormat = PreferenceHelper.getCameraFormat(mContext);
        if (cameraFormat.equals("JPEG")) {
            mSwitchCameraFormat.setChecked(false);
        } else {
            mSwitchCameraFormat.setChecked(true);
        }
        mSwitchCameraFormat.setOnCheckedChangeListener(mListener);
    }

    private CompoundButton.OnCheckedChangeListener mListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.switch_camera:
                    if (isChecked == false) {
                        PreferenceHelper.writeCameraId(mContext, "0");
                    } else {
                        PreferenceHelper.writeCameraId(mContext, "1");
                        PreferenceHelper.writeCameraFormat(mContext, "JPEG");
                        mSwitchCameraFormat.setChecked(false);
                    }
                    break;
                case R.id.switch_format:
                    if (isChecked == false) {
                        PreferenceHelper.writeCameraFormat(mContext, "JPEG");
                    } else {
                        PreferenceHelper.writeCameraFormat(mContext, "DNG");
                    }
                    break;
            }
        }
    };

}
