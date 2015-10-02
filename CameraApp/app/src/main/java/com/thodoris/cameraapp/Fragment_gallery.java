package com.thodoris.cameraapp;

import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;


public class Fragment_gallery extends android.support.v4.app.Fragment {
    RecyclerViewGridAdapter adapter;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    MyMethods myMethods = new MyMethods();
    ArrayList<String> picturesPaths, oldPicturePaths;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && MyMethods.arrayIsChanged) {
            picturesPaths = myMethods.getPicturePaths();
            adapter = new RecyclerViewGridAdapter(picturesPaths);
            recyclerView.setAdapter(adapter); //todo put it in async

            MyMethods.arrayIsChanged = false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_gallery, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new GridLayoutManager(getActivity(), 3);
        recyclerView.setLayoutManager(mLayoutManager);

        picturesPaths = myMethods.getPicturePaths();
        oldPicturePaths = picturesPaths;

        ImageView imageView = (ImageView) view.findViewById(R.id.empty_gallery);
        TextView textView = (TextView) view.findViewById(R.id.empty_text);
        if (picturesPaths.isEmpty()) {
            imageView.setVisibility(View.VISIBLE);
            textView.setVisibility(View.VISIBLE);
        }
        else {
            imageView.setVisibility(View.INVISIBLE);
            textView.setVisibility(View.INVISIBLE);

            adapter = new RecyclerViewGridAdapter(picturesPaths);
            recyclerView.setAdapter(adapter);

        }

        // Inflate the layout for this fragment
        return view;
    }

}
