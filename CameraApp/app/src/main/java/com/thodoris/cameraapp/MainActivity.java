package com.thodoris.cameraapp;


import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;

public class MainActivity extends ActionBarActivity {
    ViewPager viewPager;
    FragmentAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //pager
        viewPager = (ViewPager) findViewById(R.id.pager);
        mAdapter = new FragmentAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mAdapter);
        //

    }

}
