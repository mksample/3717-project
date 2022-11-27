package com.example.a3717project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    FirebaseDatabase database;
    DatabaseReference reference;
    FirebaseUser user;

    Spinner favourites;
    ArrayList<String> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        favourites = this.findViewById(R.id.favouritespinner);
        list = new ArrayList<>();

        // Pull from database
        database = FirebaseDatabase.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        reference = database.getReference(user.getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String favourite = String.valueOf(ds.getKey());
                    list.add(favourite);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        // Load values from database into spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        favourites.setAdapter(adapter);
        Intent intent = new Intent(this, HomePage.class);
        favourites.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), "I'm here, ma!", Toast.LENGTH_SHORT).show();
                intent.putExtra("name", favourites.getSelectedItem().toString());
                startActivity(intent);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void debugButton(View view) {
        Toast.makeText(getApplicationContext(), "Geoff", Toast.LENGTH_LONG).show();
    }

    public void Back(View view) {
        onBackPressed();
    }
}