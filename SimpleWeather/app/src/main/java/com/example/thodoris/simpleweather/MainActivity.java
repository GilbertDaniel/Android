package com.example.thodoris.simpleweather;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    AutoCompleteTextView cityAuto;
    Utils utils = new Utils();
    CityPreference cityPreference;

    MyMethods myMethods = new MyMethods();

    Toolbar toolbar;
    Menu menu;

    boolean isLoading;
    boolean searchBarIsShown;

    ImageView backBtn;

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchBarIsShown = false;

        //set toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        backBtn = (ImageView) findViewById(R.id.search_back);
        backBtn.setOnClickListener(backBtnClickListener);

        cityPreference = new CityPreference(MainActivity.this);

        //autocomplete view
        cityAuto = (AutoCompleteTextView) findViewById(R.id.toolbar_city_autocomplete);
        cityAuto.setAdapter(new PlacesAutoCompleteAdapter(this, android.R.layout.simple_dropdown_item_1line));
        String city = cityPreference.getCity();
        cityAuto.setText(city);

        cityAuto.setOnItemClickListener(cityAutoClickListener);
        cityAuto.setOnTouchListener(cityAutoTouchListener);
        cityAuto.setOnEditorActionListener(cityAutoEditorActionListener);

        //location Button
        ImageButton locationBtn = (ImageButton) findViewById(R.id.location_button);
        locationBtn.setOnClickListener(locationBtnClickListener);

        //on create, fetch the data
        myMethods.RunAsyncTask(MainActivity.this);
        myMethods.setCityTextView(MainActivity.this, city);

        //Google Play API
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


    }

    View.OnClickListener backBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (searchBarIsShown) {
                searchBarIsShown = false;
                invalidateOptionsMenu();

                //hide keyboard
                Activity activity = MainActivity.this;
                myMethods.HideKeyboard(activity);
            }
        }
    };

    AdapterView.OnItemClickListener cityAutoClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            String item = (String) parent.getItemAtPosition(position);
            cityPreference.setCity(item);

            myMethods.RunAsyncTask(MainActivity.this);
            isLoading = myMethods.isLoading;
            myMethods.setCityTextView(MainActivity.this, item);

            //hide autocomplete
            searchBarIsShown = false;
            invalidateOptionsMenu();

            //hide keyboard
            Activity activity = MainActivity.this;
            myMethods.HideKeyboard(activity);
        }
    };

    View.OnTouchListener cityAutoTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            final int DRAWABLE_RIGHT = 2;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (cityAuto.getRight() - cityAuto.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    cityAuto.setText("");
                    return true;
                }
            }
            return false;
        }
    };

    View.OnClickListener locationBtnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Boolean isLocationEnabled = myMethods.isLocationServiceEnabled(MainActivity.this);

            if(isLocationEnabled) {
                //connect to GoogleApiClient
                mGoogleApiClient.disconnect();
                mGoogleApiClient.connect();
            }
            else {
                //show dialog to enable location
                Toast.makeText(MainActivity.this, "Enable GPS", Toast.LENGTH_SHORT).show();
            }

        }
    };

    TextView.OnEditorActionListener cityAutoEditorActionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                myMethods.HideKeyboard(MainActivity.this);

                searchBarIsShown = false;
                invalidateOptionsMenu();
            }
            return false;
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;

        isLoading = myMethods.isLoading;

        MenuItem refreshItem = menu.findItem(R.id.refresh);
        if (isLoading) {
            refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
            refreshItem.setEnabled(false);
        }
        else {
            refreshItem.setActionView(null);
            refreshItem.setEnabled(true);
        }

        MenuItem searchItem = menu.findItem(R.id.search);
        if(searchBarIsShown) {
            searchItem.setVisible(false);
            refreshItem.setVisible(false);
            backBtn.setVisibility(View.VISIBLE);

            cityAuto.setVisibility(View.VISIBLE);
            cityAuto.requestFocus();
            cityAuto.setSelection(cityAuto.getText().length());
            final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(cityAuto, InputMethodManager.SHOW_IMPLICIT);
        }
        else {
            searchItem.setVisible(true);
            refreshItem.setVisible(true);
            backBtn.setVisibility(View.GONE);
            cityAuto.clearFocus();
            cityAuto.setVisibility(View.GONE);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if (id == R.id.refresh) {
            myMethods.RunAsyncTask(MainActivity.this);
            myMethods.setCityTextView(MainActivity.this, cityPreference.getCity());
        }
        else if (id == R.id.search) {
            if(!searchBarIsShown) {
                searchBarIsShown = true;
                invalidateOptionsMenu();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            Log.i("lat", String.valueOf(mLastLocation.getLatitude()));
            Log.i("lng", String.valueOf(mLastLocation.getLongitude()));

            Double Lat = mLastLocation.getLatitude();
            Double Lng = mLastLocation.getLongitude();

            String LatLng = String.valueOf(Lat) + "," + String.valueOf(Lng);

            //hide main card
            RelativeLayout mainCard = (RelativeLayout) findViewById(R.id.main_card);
            mainCard.setVisibility(View.INVISIBLE);

            myMethods.isLoading = true;
            invalidateOptionsMenu();

            myMethods.new getWeatherDataAsyncTask(MainActivity.this).execute(LatLng);

            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(Lat, Lng, 1);
                if (addresses.size() > 0) {
                    String locality = addresses.get(0).getLocality();
                    myMethods.setCityTextView(MainActivity.this, locality);
                    Log.i("locality", locality);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        else {
            Toast.makeText(MainActivity.this, "There was a problem. Please try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("sus", "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i("fail", "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
        Toast.makeText(MainActivity.this, "Connection failed", Toast.LENGTH_SHORT).show();
    }

    private class PlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
        private ArrayList<String> resultList;

        public PlacesAutoCompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return resultList.get(index);
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = utils.autocomplete(constraint.toString());

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    }
                    else {
                        notifyDataSetInvalidated();
                    }
                }
            };

            return filter;

        }
    }

}
