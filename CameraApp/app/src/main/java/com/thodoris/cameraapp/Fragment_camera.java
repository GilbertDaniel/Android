package com.thodoris.cameraapp;

import android.app.ActionBar;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.IOException;


public class Fragment_camera extends android.support.v4.app.Fragment {

    MyMethods myMethods;
    Camera mCamera;
    MyMethods.CameraPreview mPreview;
    FrameLayout preview;

    RelativeLayout previewLayout, discardSaveLayout;
    Button takePictureBtn;
    ImageButton discardBtn, saveBtn, switchCamBtn, flashBtn;

    int frontID = -1;
    int backID = -1;
    int currentID;

    //picture byte[] and Bitmap
    byte[] pictureData;
    Bitmap pictureBitmap;

    @Override
    public void onPause() {
        super.onPause();
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mPreview.getHolder().removeCallback(mPreview);
            mCamera.stopPreview();
            mCamera.release();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera, container, false);

        preview = (FrameLayout) view.findViewById(R.id.camera_preview);

        myMethods = new MyMethods();

        //-------------------Camera Preview Interface Layout
        previewLayout = (RelativeLayout) view.findViewById(R.id.preview_interface);

        //take picture button
        takePictureBtn = (Button) view.findViewById(R.id.btn_takepicture);
        takePictureBtn.setOnClickListener(takePictureBtnClickListener);


        //-------------------Discard-Save Layout
        discardSaveLayout = (RelativeLayout) view.findViewById(R.id.discard_save_layout);

        //Discard button
        discardBtn = (ImageButton) view.findViewById(R.id.discard);
        discardBtn.setOnClickListener(discardBtnClickListener);

        //Save Button
        saveBtn = (ImageButton) view.findViewById(R.id.save);
        saveBtn.setOnClickListener(saveBtnClickListener);

        //progress


        //Check for front and back cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i=0; i<numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                backID = i;
                Log.i("back", "found " +i);
            }
            else if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                frontID = i;
                Log.i("front", "found " +i);
            }
        }

        switchCamBtn = (ImageButton) view.findViewById(R.id.switch_cameras);
        if (frontID < 0) {
            //disable switch cameras button
            switchCamBtn.setVisibility(View.INVISIBLE);
        }
        switchCamBtn.setOnClickListener(switchCamBtnClickListener);

        currentID = backID;
        Log.i("id", String.valueOf(currentID));


        // Inflate the layout for this fragment
        return view;
    }

    View.OnClickListener switchCamBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (currentID == backID) {
                currentID = frontID;
            }
            else {
                currentID = backID;
            }

            Log.i("id", String.valueOf(currentID));
        }
    };

    View.OnClickListener takePictureBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (MyMethods.safeToTakePicture) {
                //http://stackoverflow.com/questions/7627921/android-camera-takepicture-does-not-return-some-times
                mCamera.setPreviewCallback(null);
                System.gc();
                mCamera.takePicture(myShutterCallback, myPictureCallback_RAW, myPictureCallback_JPG);

                previewLayout.setVisibility(View.INVISIBLE);
                discardSaveLayout.setVisibility(View.VISIBLE);

                MyMethods.safeToTakePicture = false;
            }
        }
    };

    View.OnClickListener discardBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mCamera.startPreview();

            previewLayout.setVisibility(View.VISIBLE);
            discardSaveLayout.setVisibility(View.INVISIBLE);
        }
    };

    View.OnClickListener saveBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (pictureBitmap != null) {
                Log.i("bmp", "not null");
                saveBtn.setEnabled(false);
                Toast toast = Toast.makeText(getActivity(), "Saving picture...", Toast.LENGTH_LONG);
                toast.show();

                myMethods.mCamera = mCamera;
                myMethods.previewLayout = previewLayout;
                myMethods.discardSaveLayout = discardSaveLayout;

                myMethods.new savePictureAsyncTask2(pictureBitmap, getActivity(), toast).execute();


            }
        }
    };

    /*Camera Callbacks*/
    Camera.ShutterCallback myShutterCallback = new Camera.ShutterCallback(){

        @Override
        public void onShutter() {

        }
    };

    Camera.PictureCallback myPictureCallback_RAW = new Camera.PictureCallback(){

        @Override
        public void onPictureTaken(byte[] arg0, Camera arg1) {

        }
    };

    Camera.PictureCallback myPictureCallback_JPG = new Camera.PictureCallback(){

        @Override
        public void onPictureTaken(byte[] arg0, Camera arg1) {
            pictureBitmap = BitmapFactory.decodeByteArray(arg0, 0, arg0.length);

            pictureData = arg0;
        }
    };



    @Override
    public void onResume() {
        super.onResume();

        //http://stackoverflow.com/a/25192130
        /** Check if this device has a camera */
        if (myMethods.checkCameraHardware(getActivity())) {
            mCamera = myMethods.getCameraInstance(currentID);
            mPreview = myMethods.new CameraPreview(getActivity(), mCamera);
            preview.addView(mPreview);
        }

        previewLayout.setVisibility(View.VISIBLE);
        discardSaveLayout.setVisibility(View.INVISIBLE);

    }
}
