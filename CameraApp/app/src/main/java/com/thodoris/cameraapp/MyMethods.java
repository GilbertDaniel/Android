package com.thodoris.cameraapp;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class MyMethods {
    public Camera mCamera;
    public RelativeLayout previewLayout, discardSaveLayout;

    public static boolean arrayIsChanged = false;
    public static boolean safeToTakePicture = false;

    public boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public Camera getCameraInstance(int camId){
        Camera c = null;
        try {
            c = Camera.open(camId); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
        public SurfaceHolder mHolder;
        private Camera mCamera;
        Context c;

        public CameraPreview(Context context, Camera camera) {
            super(context);
            c = context;
            mCamera = camera;

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);
            // deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            // The Surface has been created, now tell the camera where to draw the preview.
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                Log.d("myTag", "Error setting camera preview: " + e.getMessage());
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // empty. Take care of releasing the Camera preview in your activity.
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            // If your preview can change or rotate, take care of those events here.
            // Make sure to stop the preview before resizing or reformatting it.

            if (mHolder.getSurface() == null){
                // preview surface does not exist
                return;
            }

            // stop preview before making changes
            try {
                mCamera.stopPreview();

                Camera.Parameters parameters = mCamera.getParameters();
                Display display = ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

                if(display.getRotation() == Surface.ROTATION_0)
                {
                    parameters.setPreviewSize(height, width);
                    mCamera.setDisplayOrientation(90);
                }

                if(display.getRotation() == Surface.ROTATION_90)
                {
                    parameters.setPreviewSize(width, height);
                }

                if(display.getRotation() == Surface.ROTATION_180)
                {
                    parameters.setPreviewSize(height, width);
                }

                if(display.getRotation() == Surface.ROTATION_270)
                {
                    parameters.setPreviewSize(width, height);
                    mCamera.setDisplayOrientation(180);
                }

                mCamera.setParameters(parameters);

            } catch (Exception e){
                // ignore: tried to stop a non-existent preview
            }

            // set preview size and make any resize, rotate or
            // reformatting changes here

            // start preview with new settings
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();
                safeToTakePicture = true;

            } catch (Exception e){
                Log.d("myTag", "Error starting camera preview: " + e.getMessage());
            }
        }
    }

    public void saveBitmapToFile2(Bitmap bitmap, Activity activity) {

        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File dir = new File(path, "CameraApp");
        if (!dir.exists() && !dir.mkdirs()) {
            Toast.makeText(activity, "Picture could not be saved", Toast.LENGTH_SHORT).show();
            return;
        }

        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", java.util.Locale.getDefault()).format(new Date());
        String filename = "CAM" + timeStamp + ".jpg";

        File file = new File(dir, filename);

        FileOutputStream out;

        try {
            out = new FileOutputStream(file);

            //rotate bitmap
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap newBitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            bitmap.recycle();

            out.flush();
            out.close();
            //Toast.makeText(activity, "ok", Toast.LENGTH_SHORT).show();
            Log.i("ok","ok");
        } catch (Exception e) {
            e.printStackTrace();
            //Toast.makeText(activity, "Picture could not be saved", Toast.LENGTH_SHORT).show();
        }
    }

    public class savePictureAsyncTask2 extends AsyncTask<Void, Void, Void> {

        Bitmap data;
        Activity activity;
        Toast toast;

        public savePictureAsyncTask2(Bitmap bitmap, Activity activity, Toast toast) {
            this.data = bitmap;
            this.activity = activity;
            this.toast = toast;
        }

        @Override
        protected Void doInBackground(Void... params) {
            saveBitmapToFile2(data, activity);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            toast.setText("Picture saved!");
            toast.setDuration(Toast.LENGTH_SHORT);
            toast.show();

            activity.findViewById(R.id.save).setEnabled(true);

            arrayIsChanged = true;
            safeToTakePicture = true;
        }
    }

    public ArrayList<String> getPicturePaths() {
        ArrayList<String> picturesPaths = new ArrayList<>();
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File dir = new File(path, "CameraApp");
        File[] files = dir.listFiles();

        for (File file : files ) {
            String filename = file.toString();
            picturesPaths.add(filename);
        }
        Collections.reverse(picturesPaths);

        return picturesPaths;
    }


}
