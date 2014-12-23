package us.yydcdut.androidltest.adpater;

import android.content.Context;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import us.yydcdut.androidltest.R;

/**
 * Created by yuyidong on 14-12-18.
 */
public class EffectAdapter {

    public static SimpleAdapter getAdapter(Context context) {
        ArrayList<HashMap<String, String>> listItem = getEffectList();
        SimpleAdapter listItemAdapter = new SimpleAdapter(context, listItem, R.layout.item_lv_menu, new String[]{"text"}, new int[]{R.id.txt_item_menu});
        return listItemAdapter;
    }

    private static ArrayList<HashMap<String, String>> getEffectList() {
        ArrayList<HashMap<String, String>> listItem = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map1 = new HashMap<String, String>();
        map1.put("text", "aqua");
        listItem.add(map1);
        HashMap<String, String> map2 = new HashMap<String, String>();
        map2.put("text", "blackboard");
        listItem.add(map2);
        HashMap<String, String> map3 = new HashMap<String, String>();
        map3.put("text", "monocolor");
        listItem.add(map3);
        HashMap<String, String> map4 = new HashMap<String, String>();
        map4.put("text", "negative");
        listItem.add(map4);
        HashMap<String, String> map5 = new HashMap<String, String>();
        map5.put("text", "posterization");
        listItem.add(map5);
        HashMap<String, String> map6 = new HashMap<String, String>();
        map6.put("text", "sepia");
        listItem.add(map6);
        HashMap<String, String> map7 = new HashMap<String, String>();
        map7.put("text", "solarisation");
        listItem.add(map7);
        HashMap<String, String> map8 = new HashMap<String, String>();
        map8.put("text", "whiteboard");
        listItem.add(map8);
        HashMap<String, String> map9 = new HashMap<String, String>();
        map9.put("text", "off");
        listItem.add(map9);
        return listItem;
    }
}
