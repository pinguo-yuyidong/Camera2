package us.yydcdut.androidltest.ui;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import us.yydcdut.androidltest.R;

/**
 * Created by yuyidong on 14-12-22.
 */
public class ShowPhotoFrag extends Fragment {
    private List<String> mList;
    private int mIndex;
    private DisplayImageOptions mOptions;

    public static ShowPhotoFrag newInstance() {
        ShowPhotoFrag showPhotoFrag = new ShowPhotoFrag();
        return showPhotoFrag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mList = getPhotosPaths();
        Bundle bundle = getArguments();
        if (bundle != null) {
            mIndex = bundle.getInt("current");
        } else {
            mIndex = 0;
        }
        initImageLoader();
    }

    private void initImageLoader() {
        mOptions = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.ic_launcher)
                .showImageOnFail(R.drawable.ic_launcher)
                .showStubImage(R.drawable.ic_launcher)
                .cacheInMemory(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }

    private List<String> getPhotosPaths() {
        List<String> list = new ArrayList<String>();
        File dir = new File(Environment.getExternalStorageDirectory() + "/Android_L_Test/jpeg/");
        File[] files = dir.listFiles();
        for (File file : files) {
            list.add("file:///" + Environment.getExternalStorageDirectory() + "/Android_L_Test/jpeg/" + file.getName());
        }
        return list;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.show_photo_frag, null);
        ViewPager viewPager = (ViewPager) v.findViewById(R.id.vp);
        viewPager.setPageTransformer(true, new MyPageTransformer());
        viewPager.setAdapter(new MyPagerAdapter());
        viewPager.setCurrentItem(mIndex);
        return v;
    }

    class MyPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView imageView = new ImageView(getActivity());
            imageView.setMaxWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
            imageView.setMaxHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
            ImageLoader.getInstance().displayImage(mList.get(position), imageView);
            ((ViewPager) container).addView(imageView);
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    class MyPageTransformer implements ViewPager.PageTransformer {
        private float MIN_SCALE = 0.85f;

        private float MIN_ALPHA = 0.5f;

        @Override
        public void transformPage(View view, float position) {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            if (position < -1) {
                view.setAlpha(0);
            } else if (position <= 1) {
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0) {
                    view.setTranslationX(horzMargin - vertMargin / 2);
                } else {
                    view.setTranslationX(-horzMargin + vertMargin / 2);
                }
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);
                view.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));
            } else {
                view.setAlpha(0);
            }
        }
    }


}
