package com.thodoris.recorder;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MyCustomAdapter extends RecyclerView.Adapter<MyCustomAdapter.ViewHolder>{

    private ArrayList<String> recordingsArray;
    Activity activity;

    public static ClickListener clickListener;

    // Adapter's Constructor
    public MyCustomAdapter(ArrayList<String> myDataset, Activity activity) {
        this.recordingsArray = myDataset;
        this.activity = activity;
    }

    // Create new views. This is invoked by the layout manager.
    @Override
    public MyCustomAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create a new view by inflating the row item xml.
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row, parent, false);

        // Set the view to the ViewHolder
        ViewHolder holder = new ViewHolder(v);
        return holder;
    }

    // Replace the contents of a view. This is invoked by the layout manager.
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        //TITLE
        String title = recordingsArray.get(position);
        title = title.substring(0, title.indexOf(Utils.extension));
        holder.title.setText(title);

        //DURATION
        File path = Environment.getExternalStorageDirectory();
        final File filename = new File(path, "Recorder/" + recordingsArray.get(position));
        MediaPlayer mp = MediaPlayer.create(activity, Uri.parse(filename.toString()));
        int duration = mp.getDuration(); //in milliseconds

        //milliseconds to readable format
        long seconds = duration/1000;
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        String durationStr = String.format("%02d:%02d:%02d", h, m, s);
        holder.duration.setText(durationStr);

        //PLAY-PAUSE BUTTON
        holder.play.setTag("notPlaying");
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return recordingsArray.size();
    }

    // Create the ViewHolder class to keep references to your views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView duration;
        public ImageButton play;
        public ImageButton settings;

        public ViewHolder(View v) {
            super(v);

            title = (TextView) v.findViewById(R.id.title);
            duration = (TextView) v.findViewById(R.id.duration);
            play = (ImageButton) v.findViewById(R.id.play_button);
            settings = (ImageButton) v.findViewById(R.id.item_settings);

            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null) {
                        clickListener.itemClicked(v, getAdapterPosition());
                    }
                }
            });

            settings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null) {
                        clickListener.itemClicked(v, getAdapterPosition());
                    }
                }
            });
        }

    }

    public interface ClickListener{
        void itemClicked(View v, int position);
    }

    public void setClickListener(ClickListener clickListener){
        MyCustomAdapter.clickListener = clickListener;
    }
}