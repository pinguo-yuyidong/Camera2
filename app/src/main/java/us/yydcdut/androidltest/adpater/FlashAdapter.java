package us.yydcdut.androidltest.adpater;

import android.content.Context;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import us.yydcdut.androidltest.R;

/**
 * Created by yuyidong on 14-12-18.
 */
public class FlashAdapter {

    public static SimpleAdapter getAdapter(Context context) {
        ArrayList<HashMap<String, String>> listItem = getFlashList();
        SimpleAdapter listItemAdapter = new SimpleAdapter(context, listItem, R.layout.item_lv_menu, new String[]{"text"}, new int[]{R.id.txt_item_menu});
        return listItemAdapter;
    }

    private static ArrayList<HashMap<String, String>> getFlashList() {
        ArrayList<HashMap<String, String>> listItem = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map1 = new HashMap<String, String>();
        map1.put("text", "OFF");
        listItem.add(map1);
        HashMap<String, String> map2 = new HashMap<String, String>();
        map2.put("text", "SINGLE");
        listItem.add(map2);
        HashMap<String, String> map3 = new HashMap<String, String>();
        map3.put("text", "TORCH");
        listItem.add(map3);
        HashMap<String, String> map4 = new HashMap<String, String>();
        map4.put("text", "AOTO");
        listItem.add(map4);
        HashMap<String, String> map5 = new HashMap<String, String>();
        map5.put("text", "ON");
        listItem.add(map5);
        return listItem;
    }
}
