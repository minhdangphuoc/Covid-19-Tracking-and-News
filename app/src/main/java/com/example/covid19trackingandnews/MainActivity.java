package com.example.covid19trackingandnews;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private double latitude = 0;
    private double longitude = 0;
    private String country = "US";
    private RequestQueue queue;
    private StringRequest covid19Data;
    private StringRequest newsData;
    private LocationManager locationManager;
    private String covid19URL;
    private String newsURL;
    private String oldLocation = "";

    // For RecyclerView
    private RecyclerView itemRV;
    private ArrayList<newsitem> newsItemArrayList;

    // First init
    private FirstTimeInit firstTimeInit;

    // Edit Text aka Auto Complete Textview
    private MaterialAutoCompleteTextView inputCountry;
    private ArrayAdapter<String> countryList;
    private String[] countries;

    // For Blinking
    private int tries;

    // ANDROID LIFECYCLE

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        queue = Volley.newRequestQueue(this);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Init startupInit once when App start
        firstTimeInit = (FirstTimeInit) this.getApplicationContext();
        if (!FirstTimeInit.isInit) {
            startupInit();
            // Toast.makeText(getApplicationContext(), "Inited", Toast.LENGTH_LONG).show();
            FirstTimeInit.isInit = true;
        }

        // Get a reference to the AutoCompleteTextView in the layout
        inputCountry = findViewById(R.id.countryInput);

        // Get the string array
        countries = getResources().getStringArray(R.array.countries_array);
        // Create the adapter and set it to the AutoCompleteTextView
        ArrayAdapter<String> countryList = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, countries);
        inputCountry.setAdapter(countryList);

        // Set action listener
        inputCountry.setOnEditorActionListener(actionListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION},
                    0);
            return;
        }
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0,
                0,
                this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // INPUT FUNCTIONS
    // Hide Keyboard
    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        View focus = activity.getCurrentFocus();
        if (focus != null)
            inputMethodManager.hideSoftInputFromWindow(focus.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }


    private void wrongInput(MaterialAutoCompleteTextView txt) {
        final Handler handler = new Handler();


        new Thread(new Runnable() {
            @Override
            public void run() {
                int timeToBlink = 200;    //in milliseconds
                try {
                    Thread.sleep(timeToBlink);
                } catch (Exception e) {
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (txt.getVisibility() == View.VISIBLE) {
                            txt.setVisibility(View.INVISIBLE);

                        } else {
                            txt.setVisibility(View.VISIBLE);
                            txt.setTextColor(Color.RED);
                            txt.setHintTextColor(Color.RED);
                        }
                        if (tries < 10) // blinking 10 times
                        {
                            tries++;
                            wrongInput(txt);
                        } else {
                            txt.setVisibility(View.VISIBLE);
                            txt.setTextColor(Color.BLACK);
                            txt.setHintTextColor(Color.BLACK);
                        }
                    }
                });
            }
        }).start();
    }

    private String capitalizeCase(String text) {
        String firstLetter = "", remaining = "", capitalizeCaseStr = "";

        for (String word : text.split("\\s")) {
            firstLetter = word.substring(0, 1).toUpperCase();
            remaining = word.substring(1);
            capitalizeCaseStr += firstLetter + remaining + " ";
        }

        return capitalizeCaseStr; // remove a unuseful space
    }

    // - Edittext listener
    private final MaterialAutoCompleteTextView.OnEditorActionListener actionListener = new MaterialAutoCompleteTextView.OnEditorActionListener() {
        void warningInput() {
            tries = 0;
            Toast.makeText(getApplicationContext(), "Wrong input, please try again", Toast.LENGTH_LONG).show();
            wrongInput(inputCountry);
        }

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String inStr = inputCountry.getText().toString().toLowerCase();
                hideKeyboard(MainActivity.this);
                if (inStr.length() != 0)
                    if (Arrays.asList(countries).contains(inStr)) setNewCountry(capitalizeCase(inStr));
                    else warningInput();
                else {
                    warningInput();
                }
                handled = true;
            }
            return handled;
        }
    };

    // Run Once
    void startupInit() {
        getCovid19Data();
        getNews();
        TextView CountryName = findViewById(R.id.CountryName);
        CountryName.setText(country);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    // Change to full view after click button
    public void changeToFullView(View view) {
        Intent intentFullView = new Intent(this, FullView.class);
        intentFullView.putExtra("NEWS", (Serializable) newsItemArrayList);
        startActivity(intentFullView);
    }

    // Bundle savedInstanceState
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putDouble("LATITUDE", latitude);
        savedInstanceState.putDouble("LONGITUDE", longitude);
        savedInstanceState.putString("COUNTRY", country);
        savedInstanceState.putSerializable("NEWSLIST", newsItemArrayList);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        latitude = savedInstanceState.getDouble("LATITUDE");
        longitude = savedInstanceState.getDouble("LONGITUDE");
        country = savedInstanceState.getString("COUNTRY");
        newsItemArrayList = (ArrayList<newsitem>) savedInstanceState.getSerializable("NEWSLIST");
        TextView CountryName = findViewById(R.id.CountryName);
        CountryName.setText(country);
        oldLocation = country;
        getCovid19Data();
        getNewsUpdateUI();
    }

    // GPS
    @Override
    public void onLocationChanged(@NonNull Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    // Setter
    private void setNewCountry(String newCountry) {
        country = newCountry;
        if (!oldLocation.equals(country)) {
            oldLocation = country;
            getCovid19Data();
            getNews();
            TextView CountryName = findViewById(R.id.CountryName);
            CountryName.setText(country);
        }
    }

    // Getter
    private void getNewsList(String response) {
        // RV

        newsItemArrayList = new ArrayList<>();
        int in = 0;

        try {
            JSONObject data = new JSONObject(response);
            while (in < data.getInt("totalArticles")) {
                newsItemArrayList.add(new newsitem(data.getJSONArray("articles").getJSONObject(in).getString("url"),
                        data.getJSONArray("articles").getJSONObject(in).getString("image"),
                        data.getJSONArray("articles").getJSONObject(in).getString("title"),
                        data.getJSONArray("articles").getJSONObject(in).getJSONObject("source").getString("name"),
                        data.getJSONArray("articles").getJSONObject(in).getString("publishedAt")
                ));
                in++;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        getNewsUpdateUI();
    }

    private void getNewsUpdateUI() {
        if (newsItemArrayList != null) {
            itemRV = findViewById(R.id.savedNewsView);
            ItemAdapter itemAdapter = new ItemAdapter(this, newsItemArrayList);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            itemRV.setLayoutManager(linearLayoutManager);
            itemRV.setAdapter(itemAdapter);
        } else {
            Toast.makeText(this, "Cannot present list - ArrayList is empty", Toast.LENGTH_LONG).show();
        }
    }

    private void getCovid19Data() {
        covid19URL = "https://corona.lmao.ninja/v2/countries/" + country + "?yesterday&strict&query";
        covid19Data = new StringRequest(Request.Method.GET, covid19URL,
                response -> {
                    // Toast.makeText(this, response, Toast.LENGTH_LONG).show();
                    parseCovidJsonAndUpdateUI(response);
                }, error -> {
            Toast.makeText(this, "Error: Cannot get current covid-19 Data. Please check your internet connection", Toast.LENGTH_LONG).show();
        }
        );

        queue.add(covid19Data);
    }

    public void getLocation(View view) {

        String url = "http://api.geonames.org/findNearbyPlaceNameJSON?lat=" + latitude + "&lng=" + longitude + "&username=minhdang";
        StringRequest countryData = new StringRequest(Request.Method.GET, url,
                response -> {
                    // Toast.makeText(this, response, Toast.LENGTH_LONG).show();
                    parseCountryJsonAndUpdateUI(response);
                }, error -> {
            Toast.makeText(this, "Error: Cannot get current country. Please check your internet connection", Toast.LENGTH_LONG).show();
        }
        );
        queue.add(countryData);
    }

    private void getNews() {
        newsURL = "https://gnews.io/api/v4/search?q=\"covid-19 " + country + "\"&token=77f4b7d52c18185773c8b016a508a168";
        newsData = new StringRequest(Request.Method.GET, newsURL,
                response -> {
                    // Toast.makeText(this, response, Toast.LENGTH_LONG).show();
                    getNewsList(response);
                }, error -> {
            Toast.makeText(this, "Error: Cannot get News. Please check your internet connection", Toast.LENGTH_LONG).show();
        }
        );

        queue.add(newsData);
    }

    // Update UI
    private void parseCountryJsonAndUpdateUI(String response) {
        String newCountry = country;
        try {
            JSONObject data = new JSONObject(response);
            newCountry = data.getJSONArray("geonames").getJSONObject(0).getString("countryName");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setNewCountry(newCountry);
    }

    private void parseCovidJsonAndUpdateUI(String response) {
        int cases = 0;
        int todayCases = 0;
        int deaths = 0;
        int todayDeaths = 0;
        int recovered = 0;
        int todayRecovered = 0;
        try {
            JSONObject data = new JSONObject(response);
            cases = data.getInt("cases");
            todayCases = data.getInt("todayCases");
            deaths = data.getInt("deaths");
            todayDeaths = data.getInt("todayDeaths");
            recovered = data.getInt("recovered");
            todayRecovered = data.getInt("todayRecovered");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        TextView covidData = findViewById(R.id.CovidCases);
        covidData.setText(getResources().getString(R.string.cases) + " " + cases +
                "\n" + getResources().getString(R.string.today_cases) + " " + todayCases +
                "\n" + getResources().getString(R.string.deaths) + " " + deaths +
                "\n" + getResources().getString(R.string.today_deaths) + " " + todayDeaths +
                "\n" + getResources().getString(R.string.recovered) + " " + recovered +
                "\n" + getResources().getString(R.string.today_recovered) + " " + todayRecovered);
    }


}