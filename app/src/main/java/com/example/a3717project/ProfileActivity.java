package com.example.a3717project;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    FirebaseUser user;
    EditText newPassword;

    TextView email;
    TextView sureText;

    Button yesPass;
    Button noPass;
    Button passChange;
    Button yesDel;
    Button noDel;
    Button deleteAccount;
    Button signOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Assigning all of the buttons and text fields IDs for hiding purposes
        user = FirebaseAuth.getInstance().getCurrentUser();
        newPassword = this.findViewById(R.id.editTextTextPassword);
        email = this.findViewById(R.id.email);
        sureText = this.findViewById(R.id.areyousure);
        yesPass = this.findViewById(R.id.yes_password);
        noPass = this.findViewById(R.id.no_password);
        passChange = this.findViewById(R.id.passChange);
        yesDel = this.findViewById(R.id.yes_delete);
        noDel = this.findViewById(R.id.no_delete);
        deleteAccount = this.findViewById(R.id.delAccount);
        signOut = this.findViewById(R.id.profileBackButton);
        email.setText(user.getEmail());
    }

    public void Back(View view) {
        FirebaseAuth.getInstance().signOut();
        onBackPressed();
    }

    public void changePassword (View view) {
        if (newPassword.getText().toString().length() == 0) {
            Toast.makeText(getApplicationContext(), "Must input password", Toast.LENGTH_SHORT).show();
        } else {
            passChange.setVisibility(View.GONE);
            signOut.setVisibility(View.GONE);
            yesPass.setVisibility(View.VISIBLE);
            noPass.setVisibility(View.VISIBLE);
            deleteAccount.setVisibility(View.GONE);
            sureText.setVisibility(View.VISIBLE);
            newPassword.setVisibility(View.INVISIBLE);
        }
    }

    public void delete(View view) {
        deleteAccount.setVisibility(View.GONE);
        signOut.setVisibility(View.GONE);
        yesDel.setVisibility(View.VISIBLE);
        noDel.setVisibility(View.VISIBLE);
        passChange.setVisibility(View.GONE);
        sureText.setVisibility(View.VISIBLE);
        newPassword.setVisibility(View.GONE);
    }

    public void cancel (View view) {
        passChange.setVisibility(View.VISIBLE);
        signOut.setVisibility(View.VISIBLE);
        deleteAccount.setVisibility(View.VISIBLE);
        yesPass.setVisibility(View.GONE);
        noPass.setVisibility(View.GONE);
        yesDel.setVisibility(View.GONE);
        noDel.setVisibility(View.GONE);
        sureText.setVisibility(View.INVISIBLE);
        newPassword.setVisibility(View.VISIBLE);
    }

    public void confirmPassChange (View view) {
        deleteAccount.setVisibility(View.VISIBLE);
        signOut.setVisibility(View.VISIBLE);
        passChange.setVisibility(View.VISIBLE);
        yesPass.setVisibility(View.GONE);
        noPass.setVisibility(View.GONE);
        sureText.setVisibility(View.INVISIBLE);
        newPassword.setVisibility(View.VISIBLE);
        user.updatePassword(newPassword.getText().toString());
        newPassword.setText("");
    }
    public void confirmDelete (View view) {
        user.delete();
        Back(view);
    }
}