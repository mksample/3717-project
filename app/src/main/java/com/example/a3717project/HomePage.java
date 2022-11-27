package com.example.a3717project;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.OnMapReadyCallback;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class HomePage extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener, BottomNavigationView.OnNavigationItemSelectedListener {
    // Views and objects
    SupportMapFragment mapFragment;
    TextView weatherDisplay;
    TextView popupWeatherDisplay;

    // Map data
    GoogleMap mMap;
    Location defaultLocation;
    LatLng defaultLatLng = new LatLng(49.25010954461797, -123.00275621174804);
    Marker currentMarker;
    int defaultZoom = 10;


    // API info
    private final String WeatherUrl = "https://api.openweathermap.org/data/2.5/weather";
    private final String appid = "48896255671a059ef4e3ba1657a6c114";

    // Formatting
    DecimalFormat df = new DecimalFormat("#.##");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        // Get layout views
        weatherDisplay = findViewById(R.id.weatherInfo);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.maps);

        // Set default location
        defaultLocation = new Location("");
        defaultLocation.setLatitude(defaultLatLng.latitude);
        defaultLocation.setLongitude(defaultLatLng.longitude);

        // Load the map
        Objects.requireNonNull(mapFragment).getMapAsync(this);
    }

    // When the map is ready, set the location to BCIT with a marker
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        // Map formatting/filtering here
        mMap = googleMap;
        StringBuilder sbValue = new StringBuilder(sbMethod());
        PlacesTask placesTask = new PlacesTask();
        placesTask.execute(sbValue.toString());

        // Sets the camera and location to BCIT when the map is ready
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, defaultZoom));

        // Sets a listener for querying and displaying weather data when the map is clicked
        // (Uses lat and long aka LatLng)
        googleMap.setOnMapClickListener(this);
        googleMap.setOnMarkerClickListener(this);
    }

    /** Called when the user clicks a campground marker. */
    @Override
    public boolean onMarkerClick(final Marker marker) {
        currentMarker = marker;
        onMarkerShowPopupWindow(marker);

        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
    }

    /** Called when the user clicks on the map */
    @Override
    public void onMapClick(LatLng loc)
    {
        weatherDisplay.setText("");
        displayWeatherMain(loc);
    }

    public void onFavoriteButtonClick(View view) {
        // FIRESTORE HERE, STORE currentMarker.getTitle() AND currentMarker.getPosition()
    }

    // Displays the weather in the main screen display
    private void displayWeatherMain(LatLng loc) {
        String query = "";
        query = WeatherUrl + "?lat=" + loc.latitude + "&lon=" + loc.longitude + "&appid=" + appid;
        MainWeatherRequestRunner runner = new MainWeatherRequestRunner();
        runner.execute(query);
    }

    // Displays the weather in the popup screen display
    private void displayWeatherPopup(LatLng loc) {
        String query = "";
        query = WeatherUrl + "?lat=" + loc.latitude + "&lon=" + loc.longitude + "&appid=" + appid;
        PopupWeatherRequestRunner runner = new PopupWeatherRequestRunner();
        runner.execute(query);
    }

    // Shows a popup window when the marker is clicked
    public void onMarkerShowPopupWindow(Marker marker) {
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.campground_details, null);
        TextView info = popupView.findViewById(R.id.info);
        popupWeatherDisplay = info;
        String title = marker.getTitle() + "\n\n";
        info.setText(title);
        displayWeatherMain(marker.getPosition());
        displayWeatherPopup(marker.getPosition());


        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
    }

    // Performs a task in the background
    private class MainWeatherRequestRunner extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            RequestQueue queue = Volley.newRequestQueue(HomePage.this);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, strings[0], null, new Response.Listener<JSONObject>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONObject jsonObjectMain = response.getJSONObject("main");
                        double temp = jsonObjectMain.getDouble("temp") - 273.15;
                        double feelslike = jsonObjectMain.getDouble("feels_like") - 273.15;
                        int pressure = jsonObjectMain.getInt("pressure");
                        int humidity = jsonObjectMain.getInt("humidity");

                        JSONArray weather = response.getJSONArray("weather");
                        JSONObject jsonObjectWeather = weather.getJSONObject(0);
                        String description = jsonObjectWeather.getString("description");

                        JSONObject jsonObjectWind = response.getJSONObject("wind");
                        double speed = jsonObjectWind.getDouble("speed");
                        int degree = jsonObjectWind.getInt("deg");

                        JSONObject jsonObjectClouds = response.getJSONObject("clouds");
                        int cloud = jsonObjectClouds.getInt("all");

                        JSONObject jsonObjectSys = response.getJSONObject("sys");
                        String currentCountry = jsonObjectSys.getString("country");
                        String currentCity = response.getString("name");

                        weatherDisplay.append("Current weather of " + currentCity + " (" + currentCountry + ")\n"
                                + "Temperature: " + df.format(temp) + " \u2103\n"
                                + "Feels like: " + df.format(feelslike) + " \u2103\n"
                                + "Humidity: " + humidity + "%\n"
                                + "Description: " + description + "\n"
                                + "Wind speed: " + speed + "\n"
                                + "Wind degree: " + degree + "\n"
                                + "Cloudiness: " + cloud + "%\n"
                                + "Pressure: " + pressure + " hPa");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(HomePage.this, error.toString(), Toast.LENGTH_SHORT).show();
                }
            });
            queue.add(request);
            return null;
        }
    }

    // Performs a task in the background
    private class PopupWeatherRequestRunner extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            RequestQueue queue = Volley.newRequestQueue(HomePage.this);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, strings[0], null, new Response.Listener<JSONObject>() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONObject jsonObjectMain = response.getJSONObject("main");
                        double temp = jsonObjectMain.getDouble("temp") - 273.15;
                        double feelslike = jsonObjectMain.getDouble("feels_like") - 273.15;
                        int pressure = jsonObjectMain.getInt("pressure");
                        int humidity = jsonObjectMain.getInt("humidity");

                        JSONArray weather = response.getJSONArray("weather");
                        JSONObject jsonObjectWeather = weather.getJSONObject(0);
                        String description = jsonObjectWeather.getString("description");

                        JSONObject jsonObjectWind = response.getJSONObject("wind");
                        double speed = jsonObjectWind.getDouble("speed");
                        int degree = jsonObjectWind.getInt("deg");

                        JSONObject jsonObjectClouds = response.getJSONObject("clouds");
                        int cloud = jsonObjectClouds.getInt("all");

                        JSONObject jsonObjectSys = response.getJSONObject("sys");
                        String currentCountry = jsonObjectSys.getString("country");
                        String currentCity = response.getString("name");

                        popupWeatherDisplay.append("Current weather of " + currentCity + " (" + currentCountry + ")\n"
                                + "Temperature: " + df.format(temp) + " \u2103\n"
                                + "Feels like: " + df.format(feelslike) + " \u2103\n"
                                + "Humidity: " + humidity + "%\n"
                                + "Description: " + description + "\n"
                                + "Wind speed: " + speed + "\n"
                                + "Wind degree: " + degree + "\n"
                                + "Cloudiness: " + cloud + "%\n"
                                + "Pressure: " + pressure + " hPa");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(HomePage.this, error.toString(), Toast.LENGTH_SHORT).show();
                }
            });
            queue.add(request);
            return null;
        }
    }

    public void RefreshHomePage(MenuItem item){
        Intent intent = new Intent(this, HomePage.class);
        finish();
        startActivity(intent);
    }
    public void OpenFavouritesPage(MenuItem item) {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }
    public void OpenProfilePage(MenuItem item) {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        switch (item.getItemId()) {
            case R.id.home:
                break;
            case R.id.Favourites:
                break;
            case R.id.profile:
                break;
        }
        if (fragment != null) {
            System.out.println("TEST");
        }
        return true;
    }

    public StringBuilder sbMethod() {

        //use your current location here
        double mLatitude = defaultLocation.getLatitude();
        double mLongitude = defaultLocation.getLongitude();

        StringBuilder sb = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        sb.append("location=" + mLatitude + "," + mLongitude);
        sb.append("&keyword=campground");
        sb.append("&radius=50000");
        sb.append("&types=" + "campground");
        sb.append("&sensor=true");

        sb.append("&key=" + "AIzaSyCfL7NtlMex_8QipyWIaEsNVT4Nq56d1UU");

        Log.d("Map", "url: " + sb.toString());

        System.out.println("QUERY: " + sb);
        return sb;
    }

    private class PlacesTask extends AsyncTask<String, Integer, String> {

        String data = null;

        // Invoked by execute() method of this object
        @Override
        protected String doInBackground(String... url) {
            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String result) {
            ParserTask parserTask = new ParserTask();

            // Start parsing the Google places in JSON format
            // Invokes the "doInBackground()" method of the class ParserTask
            parserTask.execute(result);
        }
    }


    private static String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            assert iStream != null;
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }


    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {

        JSONObject jObject;

        // Invoked by execute() method of this object
        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;
            Place_JSON placeJson = new Place_JSON();

            try {
                jObject = new JSONObject(jsonData[0]);

                places = placeJson.parse(jObject);

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            }
            return places;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(List<HashMap<String, String>> list) {

            // Clears all the existing markers;
            mMap.clear();

            for (int i = 0; i < list.size(); i++) {

                // Creating a marker
                MarkerOptions markerOptions = new MarkerOptions();

                // Getting a place from the places list
                HashMap<String, String> hmPlace = list.get(i);

                // Getting latitude of the place
                double lat = Double.parseDouble(Objects.requireNonNull(hmPlace.get("lat")));

                // Getting longitude of the place
                double lng = Double.parseDouble(Objects.requireNonNull(hmPlace.get("lng")));

                // Getting name
                String name = hmPlace.get("place_name");

                Log.d("Map", "place: " + name);

                // Getting vicinity
                String vicinity = hmPlace.get("vicinity");

                LatLng latLng = new LatLng(lat, lng);

                // Setting the position for the marker
                markerOptions.position(latLng);

                markerOptions.title(name + " : " + vicinity);

                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));

                // Placing a marker on the touched position
                Marker m = mMap.addMarker(markerOptions);
            }
        }
    }

    public static class Place_JSON {

        /**
         * Receives a JSONObject and returns a list
         */
        public List<HashMap<String, String>> parse(JSONObject jObject) {

            JSONArray jPlaces = null;
            try {
                /** Retrieves all the elements in the 'places' array */
                jPlaces = jObject.getJSONArray("results");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            /** Invoking getPlaces with the array of json object
             * where each json object represent a place
             */
            assert jPlaces != null;
            return getPlaces(jPlaces);
        }

        private List<HashMap<String, String>> getPlaces(JSONArray jPlaces) {
            int placesCount = jPlaces.length();
            List<HashMap<String, String>> placesList = new ArrayList<HashMap<String, String>>();
            HashMap<String, String> place = null;

            /** Taking each place, parses and adds to list object */
            for (int i = 0; i < placesCount; i++) {
                try {
                    /** Call getPlace with place JSON object to parse the place */
                    place = getPlace((JSONObject) jPlaces.get(i));
                    placesList.add(place);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return placesList;
        }

        /**
         * Parsing the Place JSON object
         */
        private HashMap<String, String> getPlace(JSONObject jPlace) {

            HashMap<String, String> place = new HashMap<String, String>();
            String placeName = "-NA-";
            String vicinity = "-NA-";
            String latitude = "";
            String longitude = "";
            String reference = "";

            try {
                // Extracting Place name, if available
                if (!jPlace.isNull("name")) {
                    placeName = jPlace.getString("name");
                }

                // Extracting Place Vicinity, if available
                if (!jPlace.isNull("vicinity")) {
                    vicinity = jPlace.getString("vicinity");
                }

                latitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lat");
                longitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lng");
                reference = jPlace.getString("reference");

                place.put("place_name", placeName);
                place.put("vicinity", vicinity);
                place.put("lat", latitude);
                place.put("lng", longitude);
                place.put("reference", reference);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return place;
        }
    }

}