package us.yydcdut.androidltest.ui;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import us.yydcdut.androidltest.PreferenceHelper;
import us.yydcdut.androidltest.R;

/**
 * Created by yuyidong on 14-12-5.
 */
public class SettingFragment extends Fragment implements View.OnClickListener {
    /**
     * 配置文件
     */
    private String mCurrentCameraName;
    /**
     * 该cameraid所有的配置的文件
     */
    private String mCameraName;
    /**
     * current的SharedPerference
     */
    private SharedPreferences mSp;
    private SharedPreferences.Editor mEditor;

    public SettingFragment() {
    }

    public static SettingFragment newInstance() {
        SettingFragment instance = new SettingFragment();

        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //传递进来用的哪个cameraid
        Bundle bundle = getArguments();
        String name = null;
        if (bundle != null) {
            name = bundle.getString("camera");
        } else {
            name = "camera0";
        }
        mCurrentCameraName = "current" + name;
        mCameraName = name;
        mSp = getActivity().getSharedPreferences(mCurrentCameraName, Context.MODE_PRIVATE);
        mEditor = mSp.edit();

        View v = inflater.inflate(R.layout.setting_frag, null);
        ImageView btnBack = (ImageView) v.findViewById(R.id.title_back_btn);
        btnBack.setOnClickListener(this);
        TextView txtTitle = (TextView) v.findViewById(R.id.title_text_title);
        txtTitle.setText(returnCameraName(name) + " 设置");
        SettingItem itemFormat = (SettingItem) v.findViewById(R.id.setting_item_format);
        SettingItem itemSize = (SettingItem) v.findViewById(R.id.setting_item_size);
        SettingItem itemPreview = (SettingItem) v.findViewById(R.id.setting_item_preview);
        itemFormat.getTitle().setText("图片格式");
        itemFormat.setOnClickListener(this);
        itemSize.getTitle().setText("图片大小");
        itemSize.setOnClickListener(this);
        itemPreview.getTitle().setText("预览大小");
        itemPreview.setOnClickListener(this);
        return v;
    }

    private String returnCameraName(String camera) {
        if (camera.equals("camera0")) {
            return "后置摄像头";
        } else if (camera.equals("camera1")) {
            return "后置摄像头";
        } else {
            return "摄像头";
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.setting_item_format:
                formatDialog();
                break;
            case R.id.title_back_btn:
                getActivity().getFragmentManager().beginTransaction().replace(R.id.frame_main, DisplayFragment.newInstance()).commit();
                break;
            case R.id.setting_item_size:
                sizeDialog();
                break;
            case R.id.setting_item_preview:
                previewDialog();
                break;
        }
    }

    private void formatDialog() {
        //格式的中文名字
        String[] formatsName = PreferenceHelper.getFormatsName(getActivity(), mCameraName);
        //格式的序号
        int[] formatsNumber = PreferenceHelper.getFormatsNumber(getActivity(), mCameraName);
        //<数组序号，名字>
        final Map<Integer, Integer> map1 = new HashMap<Integer, Integer>();
        //<序号，数组序号>
        Map<Integer, Integer> map3 = new HashMap<Integer, Integer>();
        for (int i = 0; i < formatsName.length; i++) {
            map1.put(i, formatsNumber[i]);
            map3.put(formatsNumber[i], i);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("选择图片格式");
        //选择的格式的编号
        int which = mSp.getInt("format", 0);
        //选择的格式再对应数组中的位置
        builder.setSingleChoiceItems(formatsName, map3.get(which), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //通过数组号找序号
//                Log.i("formatDialog", "format--->" + map1.get(i));
                mEditor.putInt("format", map1.get(i));
                mEditor.commit();
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    private void sizeDialog() {
        //获取当前format的所有size
        String[] formatSize = PreferenceHelper.getFormatSize(getActivity(), mCameraName);
        //获取当前的format的设置的size的数组序号
        final int which = mSp.getInt("format", 0);
        //当前大小
        String size = mSp.getInt("format_" + which + "_pictureSize_width", 0) + "*" + mSp.getInt("format_" + which + "_pictureSize_height", 0);
        //Log.i("sizeDialog", "size-->" + size);
        //<名字, 数组序号>
        Map<String, Integer> map1 = new HashMap<String, Integer>();
        //<数组序号, 名字>
        final Map<Integer, String> map3 = new HashMap<Integer, String>();
        for (int i = 0; i < formatSize.length; i++) {
            map1.put(formatSize[i], i);
            map3.put(i, formatSize[i]);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("选择图片大小");
        //map1.get(size)，该大小再数组中的位置
        builder.setSingleChoiceItems(formatSize, map1.get(size), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
//                Log.i("sizeDialog", "formatSize-->" + map3.get(i));
                String size = map3.get(i);
                String[] arr = size.split("\\*");
                String width = arr[0];
                String height = arr[1];
                mEditor.putInt("format_" + which + "_pictureSize_width", Integer.parseInt(width));
                mEditor.putInt("format_" + which + "_pictureSize_height", Integer.parseInt(height));
                mEditor.commit();
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    private void previewDialog() {
        //获取当前cameraid的所有preview尺寸
        String[] previewSize = PreferenceHelper.getPreviewSize(getActivity(), mCameraName);
        final int which = mSp.getInt("format", 0);
        int width = mSp.getInt("previewSize_width", 0);
        int height = mSp.getInt("previewSize_height", 0);
        String size = width + "*" + height;
        //<名字, 数组序号>
        Map<String, Integer> map1 = new HashMap<String, Integer>();
        //<数组序号, 名字>
        final Map<Integer, String> map3 = new HashMap<Integer, String>();
        for (int i = 0; i < previewSize.length; i++) {
            map1.put(previewSize[i], i);
            map3.put(i, previewSize[i]);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("选择预览大小");
        //map1.get(size)，该大小再数组中的位置
        builder.setSingleChoiceItems(previewSize, map1.get(size), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Log.i("previewDialog", "previewSize-->" + map3.get(i));
                String size = map3.get(i);
                String[] arr = size.split("\\*");
                String width = arr[0];
                String height = arr[1];
                mEditor.putInt("previewSize_width", Integer.parseInt(width));
                mEditor.putInt("previewSize_height", Integer.parseInt(height));
                mEditor.commit();
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }
}
