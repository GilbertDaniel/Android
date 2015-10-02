package com.thodoris.recorder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity implements MyCustomAdapter.ClickListener{

    Toolbar toolbar;
    RecyclerView recyclerView;
    MyCustomAdapter adapter;

    File path, dir;
    ArrayList<String> recordingsArray = new ArrayList<>();

    SharedPreferences sp;
    private MediaPlayer mPlayer = null;
    private static final String LOG_TAG = "AudioRecordTest";

    boolean someRecordingIsPlaying = false;
    String currentlyPlayingFilename = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TOOLBAR
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Recordings");
        setSupportActionBar(toolbar);

        //RECYCLER VIEW
        recyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

        //RECORD BUTTON
        ImageButton record = (ImageButton) findViewById(R.id.record_button);
        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplication(), RecordScreen.class);
                startActivity(intent);
            }
        });

        //SHARED PREFERENCES
        sp = PreferenceManager.getDefaultSharedPreferences(getApplication());
        if (!sp.contains("track_counter")) {
            sp.edit().putInt("track_counter", 1).apply();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        UpdateRecyclerView();
    }

    private void UpdateRecyclerView() {
        recordingsArray.clear();

        path = Environment.getExternalStorageDirectory();
        dir = new File(path, "Recorder");
        String[] filenamesList = dir.list();
        Collections.addAll(recordingsArray, filenamesList);
        if (recordingsArray.size() > 0) {
            recordingsArray.remove(0); //remove temp file
        }

        Collections.sort(recordingsArray, new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                File f1 = new File(lhs);
                File f2 = new File(rhs);
                return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
            }
        });
        Collections.reverse(recordingsArray);

        adapter = new MyCustomAdapter(recordingsArray, this);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void itemClicked(View v, final int position) {

        final int pos = position;
        final View v_ = v;

        String filename = dir + "/" + recordingsArray.get(pos);
        if (v.getId() == R.id.play_button) {

                if (v.getTag().equals("notPlaying")) {
                    if (!someRecordingIsPlaying) {
                        onPlay(true, filename);
                        v.setTag("isPlaying");
                        currentlyPlayingFilename = filename;
                        someRecordingIsPlaying = true;
                        ((ImageButton) v).setImageResource(R.drawable.ic_action_stop);

                        if (mPlayer != null) {
                            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    stopPlaying();
                                    v_.setTag("notPlaying");
                                    currentlyPlayingFilename = null;
                                    someRecordingIsPlaying = false;
                                    ((ImageButton) v_).setImageResource(R.drawable.ic_action_play);
                                }
                            });
                        }
                    } else {
                        Toast.makeText(this, "Another recording is playing", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    onPlay(false, filename);
                    v.setTag("notPlaying");
                    currentlyPlayingFilename = null;
                    someRecordingIsPlaying = false;
                    ((ImageButton) v).setImageResource(R.drawable.ic_action_play);
                }


            } else {
                PopupMenu popupMenu = new PopupMenu(this, v);
                popupMenu.getMenuInflater().inflate(R.menu.settings_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.delete:
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setMessage("Really delete the recording?");
                                builder.setPositiveButton("Yes", new Dialog.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        File file = new File(dir + "/" + recordingsArray.get(position));
                                        file.delete();
                                        UpdateRecyclerView();
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
                        return true;
                    }
                });

                popupMenu.show();
            }
    }

    private void onPlay(boolean start, String filename) {
        if (start) {
            startPlaying(filename);
        } else {
            stopPlaying();
        }
    }

    private void startPlaying(String filename) {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(filename);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }
}
