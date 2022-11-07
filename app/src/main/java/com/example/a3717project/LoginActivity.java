package com.example.a3717project;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText email, password;
    private ProgressBar progressBar;
    private FirebaseAuth authorization;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authorization = FirebaseAuth.getInstance();

        email = findViewById(R.id.username);
        password = findViewById(R.id.password);

        Button signin = findViewById(R.id.login);
        Button register = findViewById(R.id.register);

        progressBar = findViewById(R.id.loading);

        signin.setOnClickListener(v -> loginUser());
        register.setOnClickListener(v -> newUser());
    }

    private void newUser() {
        progressBar.setVisibility(View.VISIBLE);

        String emailText = email.getText().toString();
        String passText = password.getText().toString();

        boolean isEmpty = false;

        if (TextUtils.isEmpty(emailText)) {
            Toast.makeText(getApplicationContext(), "Email required", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            isEmpty = true;
        }
        if (TextUtils.isEmpty(passText) && !isEmpty) {
            Toast.makeText(getApplicationContext(), "Password required", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            isEmpty = true;
        }

        if (!isEmpty) {
            authorization.createUserWithEmailAndPassword(emailText, passText).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Registration successful! Logging in...", Toast.LENGTH_LONG).show();
                    loginUser();
                    progressBar.setVisibility(View.GONE);
                } else {
                    Toast.makeText(getApplicationContext(), "Registration failed! Try again later...", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }

    private void loginUser() {
        progressBar.setVisibility(View.VISIBLE);

        String emailText = email.getText().toString();
        String passText = password.getText().toString();

        boolean isEmpty = false;

        if (TextUtils.isEmpty(emailText)) {
            Toast.makeText(getApplicationContext(), "Email required", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            isEmpty = true;
        }
        if (TextUtils.isEmpty(passText) && !isEmpty) {
            Toast.makeText(getApplicationContext(), "Password required", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            isEmpty = true;
        }

        if (!isEmpty) {
            authorization.signInWithEmailAndPassword(emailText, passText).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    progressBar.setVisibility(View.GONE);
                    Intent intent = new Intent(LoginActivity.this, HomePage.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Login failed! Please try again", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }

    public void OpenHomePage(View view) {
        Intent intent = new Intent(this, HomePage.class);
        startActivity(intent);
    }
}