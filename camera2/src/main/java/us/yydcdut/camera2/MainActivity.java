package us.yydcdut.camera2;

import android.app.Activity;
import android.os.Bundle;

import us.yydcdut.camera2.ui.PreviewFragment;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getFragmentManager().beginTransaction().replace(R.id.layout_frame_main, PreviewFragment.newInstance()).commit();
//        getFragmentManager().beginTransaction().replace(R.id.layout_frame_main, new YUVCameraFragment()).commit();
    }


}
