package com.example.a3717project;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.OnMapReadyCallback;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.Objects;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;


public class HomePage extends AppCompatActivity implements OnMapReadyCallback  {
    // Views and objects
    SupportMapFragment mapFragment;
    TextView weatherDisplay;

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

        // Load the map
        Objects.requireNonNull(mapFragment).getMapAsync(this);
    }

    // When the map is ready, set the location to BCIT with a marker
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        // Map formatting/filtering here


        // Sets the camera and location to BCIT when the map is ready
        LatLng comp3717Lecture = new LatLng(49.25010954461797, -123.00275621174804);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(comp3717Lecture, 15));

        // Sets a listener for querying and displaying weather data when the map is clicked
        // (Uses lat and long aka LatLng)
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng loc)
            {
                weatherDisplay.setText("");
                String query = "";
                query = WeatherUrl + "?lat=" + loc.latitude + "&lon=" + loc.longitude + "&appid=" + appid;
                AsyncTaskRunner runner = new AsyncTaskRunner();
                runner.execute(query);
            }
        });
    }

    // Performs a task in the background
    private class AsyncTaskRunner extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            RequestQueue queue = Volley.newRequestQueue(HomePage.this);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, strings[0], null, new Response.Listener<JSONObject>() {
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

                        weatherDisplay.setText("Current weather of " + currentCity + " (" + currentCountry + ")\n"
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

}