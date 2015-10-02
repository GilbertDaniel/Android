package com.thodoris.recorder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class RecordScreen extends AppCompatActivity {
    boolean isRecording;

    private static final String LOG_TAG = "AudioRecordTest";
    private static String tempFileName = null;

    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;

    ImageButton record, discard, save;
    File path, dir;

    Handler handler;
    TextView time_tv;
    long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_screen);

        path = Environment.getExternalStorageDirectory();
        dir = new File(path, "Recorder");
        if (!dir.exists() && !dir.mkdirs()) {
            Toast.makeText(getApplication(), "Error creating folder", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            tempFileName = dir.getAbsolutePath();
            tempFileName += "/temp" + Utils.extension;

            isRecording = true;
            onRecord(isRecording);

            //TIME
            time_tv = (TextView) findViewById(R.id.time);
            Typeface roboto = Typeface.createFromAsset(getApplication().getAssets(), "fonts/Roboto-Thin.ttf");
            time_tv.setTypeface(roboto);

            handler = new Handler();
            startTime = System.currentTimeMillis();
            handler.postDelayed(getTime, 1000);


            //RECORD-STOP BUTTON
            record = (ImageButton) findViewById(R.id.record_button);
            record.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    onRecord(!isRecording);
                    if (isRecording) {
                        //Recording stops
                        record.setImageResource(R.drawable.ic_action_mic_dark);
                        handler.removeCallbacks(getTime);
                    } else {
                        //Recording starts
                        record.setImageResource(R.drawable.ic_action_stop);
                        startTime = System.currentTimeMillis();
                        handler.postDelayed(getTime, 1000);
                    }
                    isRecording = !isRecording;
                }
            });

            //DISCARD BUTTON
            discard = (ImageButton) findViewById(R.id.discard);
            discard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RecordScreen.this);
                    builder.setMessage("Really delete the recording?");
                    builder.setPositiveButton("Yes", new Dialog.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            stopRecording();
                            isRecording = false;
                            handler.removeCallbacks(getTime);
                            finish();
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
            });

            //SAVE BUTTON
            save = (ImageButton) findViewById(R.id.save);
            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //pause recording
                    stopRecording();
                    isRecording = false;
                    record.setImageResource(R.drawable.ic_action_mic_dark);

                    //inflate popup
                    final Dialog dialog = new Dialog(RecordScreen.this);
                    dialog.setContentView(R.layout.save_window);
                    dialog.setTitle("Save Recording");

                    //popup cancel
                    Button popup_cancel = (Button) dialog.findViewById(R.id.cancel);
                    popup_cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.cancel();
                        }
                    });

                    //popup save
                    final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplication());
                    final int counter = sp.getInt("track_counter", 0);
                    final EditText title_tv = (EditText) dialog.findViewById(R.id.title_edit_text);
                    title_tv.setHint("Track " + String.valueOf(counter));

                    Button popup_save = (Button) dialog.findViewById(R.id.save);

                    handler.removeCallbacks(getTime);
                    popup_save.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //save record
                            String filename;
                            if (!title_tv.getText().toString().isEmpty()) {
                                filename = title_tv.getText().toString();
                            } else {
                                filename = "Track " + String.valueOf(counter);
                                int c = counter + 1;
                                sp.edit().putInt("track_counter", c).apply();
                            }
                            filename += Utils.extension;

                            try {
                                File src = new File(dir.getAbsoluteFile() + "/temp" + Utils.extension);
                                File dest = new File(dir.getAbsoluteFile() + "/" + filename);
                                copy(src, dest);
                                Log.i("copy", "ok");
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.i("copy", "failed");
                            }

                            //finish popup and go to Main Activity
                            dialog.cancel();
                            finish();
                        }
                    });

                    dialog.show();
                }
            });

        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RecordScreen.this);
        builder.setMessage("The recording will be lost. Are you sure?");
        builder.setPositiveButton("Yes", new Dialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                stopRecording();
                finish();
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

    private void onRecord(boolean start) {
        if (start) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(tempFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
    }

    private void stopRecording() {
        if (mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }

    public void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    private class StartTimer implements Runnable{

        @Override
        public void run() {

        }
    }

    Runnable getTime = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;

            //milliseconds to readable format
            long seconds = millis/1000;
            long s = seconds % 60;
            long m = (seconds / 60) % 60;
            long h = (seconds / (60 * 60)) % 24;
            String timeElapsed = String.format("%02d:%02d:%02d", h, m, s);
            time_tv.setText(timeElapsed);

            handler.postDelayed(getTime, 1000);
        }
    };

}
