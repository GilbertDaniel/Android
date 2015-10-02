package com.thodoris.cameraapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


public class FragmentAdapter extends FragmentPagerAdapter {
    public FragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        switch (i){
            case 0:
                return new Fragment_camera();
            case 1:
                return new Fragment_gallery();
            default: break;
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }
}
