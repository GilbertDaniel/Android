package com.example.thodoris.simpleweather;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;


public class MyMethods {

    public Boolean isLoading;
    public String status;

    public void HideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void RunAsyncTask(Activity activity) {
        SharedPreferences sp;

        ConnectivityManager connectivityManager;
        NetworkInfo networkInfo;

        sp = PreferenceManager.getDefaultSharedPreferences(activity);
        String city = sp.getString("city", "");

        connectivityManager = (ConnectivityManager) activity.getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            this.isLoading = true;

            //hide main card
            RelativeLayout mainCard = (RelativeLayout) activity.findViewById(R.id.main_card);
            mainCard.setVisibility(View.INVISIBLE);

            activity.invalidateOptionsMenu();
            new GetCoordinatesAsyncTask(activity).execute(city);
        } else {
            this.isLoading = false;
            activity.invalidateOptionsMenu();
            Toast.makeText(activity.getApplicationContext(), "No connection :(", Toast.LENGTH_LONG).show();
        }

    }

    public class GetCoordinatesAsyncTask extends AsyncTask<String, String, String> {

        Activity activity;

        public GetCoordinatesAsyncTask(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected String doInBackground(String... params) {

            String LOG_TAG = "myApp";

            String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/geocode";
            String OUT_JSON = "/json";
            String API_KEY = "AIzaSyABpuLsZN9uceJhB-DwTM2gMqnvQ91rh1I";

            HttpURLConnection conn = null;
            StringBuilder jsonResults = new StringBuilder();
            try {
                StringBuilder sb = new StringBuilder(PLACES_API_BASE + OUT_JSON);
                sb.append("?key=" + API_KEY);
                sb.append("&address=" + URLEncoder.encode(params[0], "utf8"));

                URL url = new URL(sb.toString());
                conn = (HttpURLConnection) url.openConnection();
                InputStreamReader in = new InputStreamReader(conn.getInputStream());

                int read;
                char[] buff = new char[1024];
                while ((read = in.read(buff)) != -1) {
                    jsonResults.append(buff, 0, read);
                }
            } catch (MalformedURLException e) {
                Log.e(LOG_TAG, "Error processing Places API URL", e);
                return null;
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error connecting to Places API", e);
                return null;
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }

            try {
                // Create a JSON object hierarchy from the results
                JSONObject jsonObj = new JSONObject(jsonResults.toString());
                String status = jsonObj.getString("status");

                if (status.equalsIgnoreCase("OK")) {
                    Log.i(LOG_TAG, "ok");
                    JSONArray predsJsonArray = jsonObj.getJSONArray("results");
                    JSONObject firstOfResults = predsJsonArray.getJSONObject(0);
                    JSONObject coords = firstOfResults.getJSONObject("geometry").getJSONObject("location");

                    Log.i(LOG_TAG, coords.getString("lat") + "," + coords.getString("lng"));

                    return coords.getString("lat") + "," + coords.getString("lng");
                }
                else {
                    Log.e(LOG_TAG, "zero results");
                }


            } catch (JSONException e) {
                Log.e(LOG_TAG, "Cannot process JSON results", e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //s is the coordinates
            new getWeatherDataAsyncTask(activity).execute(s);
        }
    }

    public class getWeatherDataAsyncTask extends AsyncTask<String, String, String> {
        Activity activity;

        public getWeatherDataAsyncTask(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected String doInBackground(String... params) {
            String LOG_TAG = "forecast_tag";

            HttpURLConnection conn = null;
            StringBuilder jsonResults = new StringBuilder();
            if (params[0] != null) {
                try {

                    String FORECAST_API_KEY = "87c19f6141c18894aabb512460d0ca7d";

                    StringBuilder sb = new StringBuilder("https://api.forecast.io/forecast/");
                    sb.append(FORECAST_API_KEY + "/");
                    sb.append(URLEncoder.encode(params[0], "utf8"));

                    URL url = new URL(sb.toString());
                    conn = (HttpURLConnection) url.openConnection();
                    InputStreamReader in = new InputStreamReader(conn.getInputStream());

                    int read;
                    char[] buff = new char[1024];
                    while ((read = in.read(buff)) != -1) {
                        jsonResults.append(buff, 0, read);
                    }

                    return jsonResults.toString();

                } catch (MalformedURLException e) {
                    Log.e(LOG_TAG, "Error processing Places API URL", e);
                    return null;
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error connecting to Places API", e);
                    return null;
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }
            else {
                return null;
            }

        }

        @Override
        protected void onPostExecute(String s) {

            TextView temp_tv = (TextView) activity.findViewById(R.id.temp);

            if (s != null) {
                try {
                    JSONObject jsonObject = new JSONObject(s);

                    String currently = jsonObject.getString("currently");

                    JSONObject jsonObjCurrently = new JSONObject(currently);
                    String temp = jsonObjCurrently.getString("temperature");
                    String icon = jsonObjCurrently.getString("icon");
                    String summary = jsonObjCurrently.getString("summary");

                    //fahrenheit to celsius
                    Double celsius = (Double.parseDouble(temp) - 32) / 1.8;
                    Long celsius_long = Math.round(celsius);
                    int celsius_int = celsius_long.intValue();
                    String celsius_string = String.valueOf(celsius_int);
                    temp_tv.setText(celsius_string);

                    Typeface roboto = Typeface.createFromAsset(activity.getAssets(), "fonts/Roboto-Thin.ttf");
                    temp_tv.setTypeface(roboto);
                    TextView celsius_tv = (TextView) activity.findViewById(R.id.celsius_symbol);
                    celsius_tv.setTypeface(roboto);

                    //icon
                    ImageView imageView = (ImageView) activity.findViewById(R.id.weather_icon);
                    String imageName = "";
                    imageName = icon.replace("-", "_");
                    int resId = activity.getResources().getIdentifier(imageName, "drawable", activity.getApplication().getPackageName());
                    imageView.setImageResource(resId);

                    //summary
                    TextView sum_tv = (TextView) activity.findViewById(R.id.summary);
                    sum_tv.setText(summary);
                    sum_tv.setTypeface(roboto);

                    //show main card
                    RelativeLayout mainCard = (RelativeLayout) activity.findViewById(R.id.main_card);
                    mainCard.setVisibility(View.VISIBLE);


                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
            else {
                //error - something went wrong
                status = "error";
            }

            isLoading = false;
            activity.invalidateOptionsMenu();

            super.onPostExecute(s);
        }
    }

    public boolean isLocationServiceEnabled(Activity activity){
        Context context = activity.getApplicationContext();

        LocationManager locationManager;
        boolean gps_enabled;


        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        return gps_enabled;

    }

    public void setCityTextView(Activity activity, String city) {
        //SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
        //String city = sp.getString("city", "");
        //get the name of the city
        int comma_index;
        if (city != null) {
            comma_index = city.indexOf(",");
            if (comma_index != -1) {
                city = city.substring(0, comma_index);
            }
        }
        TextView city_tv = (TextView) activity.findViewById(R.id.city);
        city_tv.setText(city);
        Typeface roboto = Typeface.createFromAsset(activity.getAssets(), "fonts/Roboto-Thin.ttf");
        city_tv.setTypeface(roboto);
    }

}
