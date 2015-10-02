package com.example.thodoris.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class MyCustomAdapter extends RecyclerView.Adapter<MyCustomAdapter.ViewHolder>{

    private ArrayList<Data> mDataset;
    public static ClickListener clickListener;


    // Adapter's Constructor
    public MyCustomAdapter(ArrayList<Data> myDataset) {
        mDataset = myDataset;
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

        holder.memoText.setText(mDataset.get(position).text);
        // Get element from your dataset at this position and set the text for the specified element
        String fileDate = mDataset.get(position).date;
        fileDate = fileDate.substring(4);
        String secs, mins, hours, day, month, year;
        secs = fileDate.substring(13,15);
        mins = fileDate.substring(11,13);
        hours = fileDate.substring(9,11);
        day = fileDate.substring(6,8);
        month = fileDate.substring(4,6);
        year = fileDate.substring(0,4);

        String prettyDate = day + "/" + month + "/" + year + " " + hours + ":" + mins;
        holder.memoDate.setText(prettyDate);
    }

    public void setClickListener(ClickListener clickListener){
        this.clickListener = clickListener;
    }


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    // Create the ViewHolder class to keep references to your views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView memoText;
        public TextView memoDate;
        public Button memoDelete;

        /**
         * Constructor
         * @param v The container view which holds the elements from the row item xml
         */
        public ViewHolder(View v) {
            super(v);

            memoText = (TextView) v.findViewById(R.id.memo_text);
            memoDate = (TextView) v.findViewById(R.id.memo_date);
            memoDelete = (Button) v.findViewById(R.id.memoDelete);

            final View view = v;

            memoText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null) {
                        clickListener.itemClicked(v, view, getPosition());
                    }
                }
            });

            memoDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null) {
                        clickListener.itemClicked(v, view, getPosition());
                    }
                }
            });
        }

    }

    public interface ClickListener{
        public void itemClicked(View v, View parent, int position);
    }
}