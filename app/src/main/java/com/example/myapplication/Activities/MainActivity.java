package com.example.myapplication.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (user != null){
            Intent intent = new Intent(this, HomeActivity.class);
            startActivity(intent);
        } else {
            setContentView(R.layout.activity_main);
        }

    }
    public void agreeClick(View view) {
        Intent intent = new Intent(this, EnterYourPhoneNumberActivity.class);
        startActivity(intent);
    }
}