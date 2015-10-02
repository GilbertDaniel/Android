package com.example.thodoris.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;


public class NewMemo extends ActionBarActivity {

    private Toolbar toolbar;

    String filename = null; //on destroy make it null
    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_memo);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editText = (EditText) findViewById(R.id.editText);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if(extras != null){
            //recycler view item was clicked
            toolbar.setTitle("Edit memo");
            setSupportActionBar(toolbar);

            if(extras.containsKey("filename")){
                filename = intent.getStringExtra("filename");
                String stringOfText = "";
                try {
                    InputStream inputStream = openFileInput(filename);

                    if (inputStream != null) {
                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        String receiveString = "";
                        StringBuilder stringBuilder = new StringBuilder();

                        while ((receiveString = bufferedReader.readLine()) != null) {
                            stringBuilder.append(receiveString);
                            stringBuilder.append('\n');
                        }

                        inputStream.close();
                        stringOfText = stringBuilder.toString();
                        stringOfText = stringOfText.trim(); //remove whitespace at beginning and end
                        editText.setText(stringOfText);
                        editText.setSelection(editText.getText().length()); //put cursor at the end
                    }
                } catch (FileNotFoundException e) {
                    Log.e("f", "File not found: " + e.toString());
                } catch (IOException e) {
                    Log.e("f", "Can not read file: " + e.toString());
                }
            }
            else {
                filename = null;
            }
        }
        else {
            //add memo button was clicked
            toolbar.setTitle("New memo");
            setSupportActionBar(toolbar);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_memo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save) {

            String text = editText.getText().toString();
            FileOutputStream outputStream;

            //if(text.trim().length() > 0) {

                //write the file with the above filename
                try {
                    if(filename != null){
                        File file = new File(getApplication().getFilesDir(), filename);
                        Boolean del = file.delete();

                        if(!del){
                            Toast.makeText(getApplicationContext(), "Error!", Toast.LENGTH_LONG).show();
                        }
                    }
                    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    filename = "MEMO" + timeStamp;

                    outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                    outputStream.write(text.getBytes());
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Error! Memo not saved.", Toast.LENGTH_LONG).show();
                }

            //}

            finish();

        }

        return super.onOptionsItemSelected(item);
    }
}
