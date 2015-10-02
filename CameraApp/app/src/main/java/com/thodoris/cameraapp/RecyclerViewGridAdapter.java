package com.thodoris.cameraapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;

public class RecyclerViewGridAdapter extends RecyclerView.Adapter<RecyclerViewGridAdapter.ViewHolder> {

    ArrayList<String> picturesPaths;

    public RecyclerViewGridAdapter(ArrayList<String> picturesPaths) {
        this.picturesPaths = picturesPaths;
    }

    @Override
    public RecyclerViewGridAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerViewGridAdapter.ViewHolder holder, int position) {
        holder.thumbnail.setImageBitmap(null);
//        ExifInterface exif;
//        try {
//            exif = new ExifInterface(picturesPaths.get(position));
//            byte[] imageData=exif.getThumbnail();
//            if (imageData!=null) {
//                Bitmap thumbnail = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
//                holder.thumbnail.setImageBitmap(thumbnail);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            holder.thumbnail.setImageBitmap(null);
//        }


        int targetW = 100;
        int targetH = targetW;
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(picturesPaths.get(position), bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(picturesPaths.get(position), bmOptions);
        holder.thumbnail.setImageBitmap(bitmap);
        holder.thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
    }

    @Override
    public int getItemCount() {
        return picturesPaths.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        public ImageView thumbnail;

        public ViewHolder(View itemView) {
            super(itemView);

            thumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
        }
    }


}
