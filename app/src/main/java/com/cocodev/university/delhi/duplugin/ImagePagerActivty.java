package com.cocodev.university.delhi.duplugin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.cocodev.university.delhi.duplugin.Utility.BlankFragment;
import com.cocodev.university.delhi.duplugin.Utility.CustomViewPager;

import java.util.ArrayList;
import java.util.Iterator;

public class ImagePagerActivty extends AppCompatActivity implements BlankFragment.PhotoTapListener {
    public static final String RESOURCE = "resource";
    public static final String POSITION = "position";
    private CustomViewPager mViewPager;
    private ArrayList<String> imageList;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_pager_activty);

        mViewPager = (CustomViewPager) findViewById(R.id.imagePagerActivity_viewPager);
        Intent i = getIntent();
        imageList = i.getStringArrayListExtra(RESOURCE);
        position = i.getIntExtra(POSITION,0);
        Iterator<String> iterator = imageList.iterator();
        final ArrayList<BlankFragment> fragments = new ArrayList<BlankFragment>();
        while(iterator.hasNext()){
            String photo = iterator.next();
            BlankFragment temp = BlankFragment.newInstance(photo);
            fragments.add(temp);
        }
        mViewPager.setAdapter(new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return  fragments.get(position);
            }

            @Override
            public int getCount() {
                return fragments.size();
            }
        });
        mViewPager.setCurrentItem(position);
    }

    @Override
    public void onPhotoTap(ImageView view, float x, float y) {

    }
}

