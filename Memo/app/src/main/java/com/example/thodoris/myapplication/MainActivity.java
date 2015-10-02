package com.example.thodoris.myapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


public class MainActivity extends ActionBarActivity implements MyCustomAdapter.ClickListener {

    private Toolbar toolbar;
    Menu menu;

    RecyclerView recyclerView;
    MyCustomAdapter adapter;

    ArrayList<Data> myDataset;

    @Override
    protected void onResume() {
        super.onResume();

        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

        myDataset = myMethods.getData(this);

        // Create the adapter
        adapter = new MyCustomAdapter(myDataset);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        toolbar.setTitle("Memo (" + myDataset.size() +")");
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ADD TOOLBAR
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        // improve performance if you know that changes in content
        // do not change the size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

        // Data set used by the adapter. This data will be displayed.
        myDataset = myMethods.getData(this);

        // Create the adapter
        MyCustomAdapter adapter = new MyCustomAdapter(myDataset);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);

        toolbar.setTitle("Memo (" + myDataset.size() +")");
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
       if(id == R.id.action_add){
            Intent intent = new Intent(this, NewMemo.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void itemClicked(View v, View parent, int position) {
        final int pos = position;
        if(v.getClass().getName().contains("TextView")) {

            parent.setBackgroundColor(Color.parseColor("#CCECFF"));
            Intent intent = new Intent(MainActivity.this, NewMemo.class);
            intent.putExtra("filename", myDataset.get(position).date);
            startActivity(intent);
        }
        else if (v.getClass().getName().contains("Button")){

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("The memo will be deleted. Are you sure?");

            builder.setPositiveButton("Yes", new Dialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String filename = myDataset.get(pos).date;
                    File file = new File(getApplication().getFilesDir(), filename);
                    Boolean del = file.delete();

                    if (!del) {
                        Toast.makeText(getApplicationContext(), "Error! Memo not deleted", Toast.LENGTH_LONG).show();
                    } else {
                        //remove from recycler view
                        //refresh recycler view
                        myDataset.remove(pos);
                        adapter = new MyCustomAdapter(myDataset);
                        recyclerView.setAdapter(adapter);
                    }
                    dialog.cancel();

                    toolbar.setTitle("Memo (" + myDataset.size() +")");
                    setSupportActionBar(toolbar);
                }

            });

            builder.setNegativeButton("No", new Dialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();

        }
    }

}
