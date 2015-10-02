package com.example.thodoris.simpleweather;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by thodoris on 13/6/2015.
 */
public class CityPreference {

    SharedPreferences preferences;

    public CityPreference(Activity activity){
        this.preferences = PreferenceManager.getDefaultSharedPreferences(activity);
    }

    void setCity(String city){
        preferences.edit().putString("city", city).apply();
    }

    String getCity(){
        return preferences.getString("city", "");
    }

    void setId(String id){
        preferences.edit().putString("id", id).apply();
    }
}
