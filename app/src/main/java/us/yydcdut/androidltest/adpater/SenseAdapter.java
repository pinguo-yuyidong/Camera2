package us.yydcdut.androidltest.adpater;

import android.content.Context;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

import us.yydcdut.androidltest.R;

/**
 * Created by yuyidong on 14-12-23.
 */
public class SenseAdapter {

    public static SimpleAdapter getAdapter(Context context) {
        ArrayList<HashMap<String, String>> listItem = getSenseList();
        SimpleAdapter listItemAdapter = new SimpleAdapter(context, listItem, R.layout.item_lv_menu, new String[]{"text"}, new int[]{R.id.txt_item_menu});
        return listItemAdapter;
    }

    private static ArrayList<HashMap<String, String>> getSenseList() {
        ArrayList<HashMap<String, String>> listItem = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map1 = new HashMap<String, String>();
        map1.put("text", "DISABLED");
        listItem.add(map1);
        HashMap<String, String> map2 = new HashMap<String, String>();
        map2.put("text", "FACE_PRIORITY");
        listItem.add(map2);
        HashMap<String, String> map3 = new HashMap<String, String>();
        map3.put("text", "ACTION");
        listItem.add(map3);
        HashMap<String, String> map4 = new HashMap<String, String>();
        map4.put("text", "PORTRAIT");
        listItem.add(map4);
        HashMap<String, String> map5 = new HashMap<String, String>();
        map5.put("text", "LANDSCAPE");
        listItem.add(map5);
        HashMap<String, String> map6 = new HashMap<String, String>();
        map6.put("text", "NIGHT");
        listItem.add(map6);
        HashMap<String, String> map7 = new HashMap<String, String>();
        map7.put("text", "NIGHT_PORTRAIT");
        listItem.add(map7);
        HashMap<String, String> map8 = new HashMap<String, String>();
        map8.put("text", "THEATRE");
        listItem.add(map8);
        HashMap<String, String> map9 = new HashMap<String, String>();
        map9.put("text", "BEACH");
        listItem.add(map9);
        HashMap<String, String> map10 = new HashMap<String, String>();
        map10.put("text", "SNOW");
        listItem.add(map10);
        HashMap<String, String> map11 = new HashMap<String, String>();
        map11.put("text", "SUNSET");
        listItem.add(map11);
        HashMap<String, String> map12 = new HashMap<String, String>();
        map12.put("text", "STEADYPHOTO");
        listItem.add(map12);
        HashMap<String, String> map13 = new HashMap<String, String>();
        map13.put("text", "FIREWORKS");
        listItem.add(map13);
        HashMap<String, String> map14 = new HashMap<String, String>();
        map14.put("text", "SPORTS");
        listItem.add(map14);
        HashMap<String, String> map15 = new HashMap<String, String>();
        map15.put("text", "PARTY");
        listItem.add(map15);
        HashMap<String, String> map16 = new HashMap<String, String>();
        map16.put("text", "CANDLELIGHT");
        listItem.add(map16);
        HashMap<String, String> map17 = new HashMap<String, String>();
        map17.put("text", "BARCODE");
        listItem.add(map17);


        return listItem;
    }
}
