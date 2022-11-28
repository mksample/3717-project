package com.example.a3717project;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class recycleViewAdapter extends RecyclerView.Adapter<recycleViewAdapter.MyViewHolder> {

    // data
    Context c;
    ArrayList<Favourite> places;

    // firebase
    FirebaseDatabase database;
    DatabaseReference reference;
    FirebaseUser user;

    private final String appid = "48896255671a059ef4e3ba1657a6c114";
    private final String WeatherUrl = "https://api.openweathermap.org/data/2.5/weather";
    DecimalFormat df = new DecimalFormat("#.##");


    public recycleViewAdapter(Context c, ArrayList<Favourite> places) {
        this.c = c;
        this.places = places;

        // Load database
        database = FirebaseDatabase.getInstance();

        // Load user information
        user = FirebaseAuth.getInstance().getCurrentUser();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(c);
        View view = inflater.inflate(R.layout.row_layout, parent, false);
        return new MyViewHolder((view));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Favourite place = places.get(position);
        holder.title.setText(place.getTitle());
        unfavouriteButton b = new unfavouriteButton(place.getTitle(), holder.unfavourite, place.getLatitude(), place.getLongitude());
        displayWeather(new LatLng(place.getLatitude(), place.getLongitude()), holder);
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView title, weather;
        Button unfavourite;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            weather = itemView.findViewById(R.id.weather);
            unfavourite = itemView.findViewById(R.id.unfavourite);
        }
    }

    // Displays the weather in the popup screen display
    private void displayWeather(LatLng loc, MyViewHolder holder) {
        String query = "";
        query = WeatherUrl + "?lat=" + loc.latitude + "&lon=" + loc.longitude + "&appid=" + appid;
        recycleViewAdapter.WeatherRequestRunner runner = new recycleViewAdapter.WeatherRequestRunner(holder);
        runner.execute(query);
    }

    private class unfavouriteButton {
        String title;
        Button button;
        double lat;
        double lng;

        public unfavouriteButton(String title, Button b, double lat, double lng) {
            this.title = title;
            this.button = b;
            this.lat = lat;
            this.lng = lng;
            b.setOnClickListener(v -> onUnfavouriteButtonClick());
        }

        @SuppressLint("SetTextI18n")
        public void onFavoriteButtonClick() {

            // Create favourite
            Favourite favourite = new Favourite();
            favourite.setTitle(title);
            favourite.setLatitude(lat);
            favourite.setLongitude(lng);

            // Load database reference
            reference = database.getReference(user.getUid());
            Task setFavourite = reference.child(title).setValue(favourite);
            button.setText("Unfavourite");
            button.setOnClickListener(v -> onUnfavouriteButtonClick());
            setFavourite.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(c, "Could not add a favourite! Try again later...", Toast.LENGTH_SHORT).show();
                    button.setText("Favourite");
                    button.setOnClickListener(v -> onFavoriteButtonClick());
                }
            });
        }

        @SuppressLint("SetTextI18n")
        public void onUnfavouriteButtonClick() {
            // Remove favourite
            // Load database reference
            reference = database.getReference(user.getUid());
            Task setFavourite = reference.child(title).removeValue();
            button.setText("Favourite");
            button.setOnClickListener(v -> onFavoriteButtonClick());
            setFavourite.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(c, "Could not remove favourite! Try again later...", Toast.LENGTH_SHORT).show();
                    button.setText("Unfavourite");
                    button.setOnClickListener(v -> onUnfavouriteButtonClick());
                }
            });
        }
    }

    // Performs a task in the background
    private class WeatherRequestRunner extends AsyncTask<String, Void, String> {
        MyViewHolder holder;
        WeatherRequestRunner(MyViewHolder holder) {
            this.holder = holder;
        }

        @Override
        protected String doInBackground(String... strings) {
            RequestQueue queue = Volley.newRequestQueue(c);
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

                        holder.weather.setText("Temperature: " + df.format(temp) + " \u2103\n"
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
                    Toast.makeText(c, error.toString(), Toast.LENGTH_SHORT).show();
                }
            });
            queue.add(request);
            return null;
        }
    }
}
