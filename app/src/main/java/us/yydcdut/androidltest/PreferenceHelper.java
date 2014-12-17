package us.yydcdut.androidltest;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.ImageFormat;
import android.util.Size;

/**
 * Created by yuyidong on 14-12-5.
 */
public class PreferenceHelper {

    /**
     * 初始化所有配置到sharedPreference
     *
     * @param context
     * @param name
     * @param previewSizes
     * @param formats      数组里面存的是编号
     * @param pictureSizes
     */
    public static void writePreferenceForCameraId(Context context, String name, Size[] previewSizes, Integer[] formats, Size[][] pictureSizes) {
        SharedPreferences preferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("previewSizes", previewSizes.length);
        for (int i = 0; i < previewSizes.length; i++) {
            editor.putInt("previewSizes" + i + "_width", previewSizes[i].getWidth());
            editor.putInt("previewSizes" + i + "_height", previewSizes[i].getHeight());
        }
        //有几种格式
        editor.putInt("formats", formats.length);
        for (int i = 0; i < formats.length; i++) {
            //这里判断，如果是nullSize的话，就写0进去
            editor.putInt("formats" + i, formats[i]);
            if (pictureSizes[i] == null) {
                editor.putInt("formats" + i + "pictureSizes", 0);
                continue;
            }
            editor.putInt("formats" + formats[i] + "pictureSizes", pictureSizes[i].length);
            for (int j = 0; j < pictureSizes[i].length; j++) {
                editor.putInt("formats" + formats[i] + "pictureSizes" + j + "_width", pictureSizes[i][j].getWidth());
                editor.putInt("formats" + formats[i] + "pictureSizes" + j + "_height", pictureSizes[i][j].getHeight());
            }
        }
        editor.commit();
        writeCurrentPreferenceForCameraId(context, name, previewSizes, formats, pictureSizes);
    }

    /**
     * 实际用到的配置的初始化
     *
     * @param context
     * @param name
     * @param previewSizes
     * @param formats
     * @param pictureSizes
     */
    private static void writeCurrentPreferenceForCameraId(Context context, String name, Size[] previewSizes, Integer[] formats, Size[][] pictureSizes) {
        String currentName = "current" + name;
        SharedPreferences currentPreferences = context.getSharedPreferences(currentName, Context.MODE_PRIVATE);
        SharedPreferences preferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = currentPreferences.edit();
        editor.putInt("previewSize_width", previewSizes[0].getWidth());
        editor.putInt("previewSize_height", previewSizes[0].getHeight());
        //这里相当于dng和jpeg分别分组
        int formatsNum = preferences.getInt("formats", 0);
//        Log.i("writeCurrentPreferenceForCameraId", "formatsNum--->" + formatsNum);
        for (int i = 0; i < formatsNum; i++) {
            editor.putInt("format_" + formats[i] + "_pictureSize_width", pictureSizes[i][0].getWidth());
            editor.putInt("format_" + formats[i] + "_pictureSize_height", pictureSizes[i][0].getHeight());
//            Log.i("writeCurrentPreferenceForCameraId", "format_" + formats[i] + "_pictureSize_width--->" + pictureSizes[i][0].getWidth() + ",,,format_" + formats[i] + "_pictureSize_height--->" + pictureSizes[i][0].getHeight());
        }
        //当前用的格式
        editor.putInt("format", formats[0]);
        editor.commit();
    }

    /**
     * 初始化存储摄像头
     *
     * @param context
     */
    public static void writeCurrentCameraid(Context context, String cameraId) {
        SharedPreferences currentPreferences = context.getSharedPreferences("current", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = currentPreferences.edit();
        editor.putString("cameraid", cameraId);
        editor.commit();
    }

    /**
     * 获取哪个摄像头
     *
     * @param context
     * @return
     */
    public static String getCurrentCameraid(Context context) {
        SharedPreferences currentPreferences = context.getSharedPreferences("current", Context.MODE_PRIVATE);
        return currentPreferences.getString("cameraid", "0");
    }

    /**
     * 判断相机参数是否已经初始化过了
     *
     * @param context
     * @return
     */
    public static boolean checkFirstInit(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("current", Context.MODE_PRIVATE);
        int num = preferences.getInt("first_time", 0);
        if (num == 1) {
            return true;
        } else {
//            SharedPreferences.Editor editor = preferences.edit();
//            editor.putInt("first_time", 1);
//            editor.commit();
            return false;
        }
    }

    /**
     * 得到cameraid文件中的图片格式，，中文的
     *
     * @param context
     * @param cameraId
     * @return
     */
    public static String[] getFormatsName(Context context, String cameraId) {
        SharedPreferences preferences = context.getSharedPreferences(cameraId, Context.MODE_PRIVATE);
        int formatsNum = preferences.getInt("formats", 0);
        String[] formats = new String[formatsNum];
        for (int i = 0; i < formatsNum; i++) {
            int format = preferences.getInt("formats" + i, 0);
            if (null != getFormatName(format)) {
                formats[i] = getFormatName(format);
            } else {
                formats[i] = "null";
            }
        }
        return formats;
    }

    /**
     * 得到图片格式的所有序号
     *
     * @param context
     * @param cameraId
     * @return
     */
    public static int[] getFormatsNumber(Context context, String cameraId) {
        SharedPreferences preferences = context.getSharedPreferences(cameraId, Context.MODE_PRIVATE);
        int formatsNum = preferences.getInt("formats", 0);
        int[] formats = new int[formatsNum];
        for (int i = 0; i < formatsNum; i++) {
            int format = preferences.getInt("formats" + i, 0);
            if (null != getFormatName(format)) {
                formats[i] = format;
            } else {
                formats[i] = -1;
            }
        }
        return formats;
    }

    /**
     * 将序号转换成名字
     *
     * @param format
     * @return
     */
    private static String getFormatName(int format) {
        String name = null;
        switch (format) {
            case ImageFormat.JPEG:
                name = "JPEG";
                break;
            case ImageFormat.NV16:
                name = "NV16";
                break;
            case ImageFormat.NV21:
                name = "NV21";
                break;
            case ImageFormat.RAW10:
                name = "RAW10";
                break;
            case ImageFormat.RAW_SENSOR:
                name = "RAW_SENSOR";
                break;
            case ImageFormat.RGB_565:
                name = "RGB_565";
                break;
            case ImageFormat.YUV_420_888:
                name = "YUV_420_888";
                break;
            case ImageFormat.YUY2:
                name = "YUY2";
                break;
            case ImageFormat.YV12:
                name = "YV12";
                break;
            case ImageFormat.UNKNOWN:
                name = null;
                break;
        }
        return name;
    }

    /**
     * 获取该格式的所有格式大小尺寸
     *
     * @param context
     * @param cameraId
     * @return
     */
    public static String[] getFormatSize(Context context, String cameraId) {
        SharedPreferences sp = context.getSharedPreferences(cameraId, Context.MODE_PRIVATE);
        SharedPreferences currentSp = context.getSharedPreferences("current" + cameraId, Context.MODE_PRIVATE);
        //得到当前format
        int currentFormat = currentSp.getInt("format", 256);
        //获得当前format的数目
        int sizeNum = sp.getInt("formats" + currentFormat + "pictureSizes", 0);
        String[] str = new String[sizeNum];
        //获取
        for (int i = 0; i < sizeNum; i++) {
            str[i] = sp.getInt("formats" + currentFormat + "pictureSizes" + i + "_width", 0) + "*" + sp.getInt("formats" + currentFormat + "pictureSizes" + i + "_height", 0);
        }
        return str;
    }

    public static String[] getPreviewSize(Context context, String cameraId) {
        SharedPreferences sp = context.getSharedPreferences(cameraId, Context.MODE_PRIVATE);
        //得到当前format
        int previewNum = sp.getInt("previewSizes", 0);
        String[] str = new String[previewNum];
        for (int i = 0; i < previewNum; i++) {
            str[i] = sp.getInt("previewSizes" + i + "_width", 0) + "*" + sp.getInt("previewSizes" + i + "_height", 0);
        }
        return str;
    }

}
