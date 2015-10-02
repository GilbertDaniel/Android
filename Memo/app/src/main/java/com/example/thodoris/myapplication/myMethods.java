package com.example.thodoris.myapplication;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by thodoris on 12/6/2015.
 */
public class myMethods {

    public static ArrayList<Data> getData(Context context){

        ArrayList<Data> myDataset = new ArrayList<>();
        File dirFiles = context.getFilesDir();
        String[] filesList = dirFiles.list();
        Arrays.sort(filesList, Collections.reverseOrder());

        for (String strFile : filesList) {

            String stringOfText = "";
            try {
                InputStream inputStream = context.openFileInput(strFile);

                if (inputStream != null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String receiveString = "";
                    StringBuilder stringBuilder = new StringBuilder();

                    while ((receiveString = bufferedReader.readLine()) != null & stringBuilder.length() < 40) {
                        stringBuilder.append(receiveString);
                        stringBuilder.append(' ');
                    }

                    inputStream.close();
                    stringOfText = stringBuilder.toString();
                    stringOfText = stringOfText.trim(); //remove whitespace at beginning and end
                }
            } catch (FileNotFoundException e) {
                Log.e("f", "File not found: " + e.toString());
            } catch (IOException e) {
                Log.e("f", "Can not read file: " + e.toString());
            }

            Data current = new Data();
            current.text = stringOfText;
            current.date = strFile;
            myDataset.add(current);

        }

        return myDataset;
    }

}
