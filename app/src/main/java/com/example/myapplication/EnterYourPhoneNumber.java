package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class EnterYourPhoneNumber extends AppCompatActivity {

    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_your_phone_number);
    }

    public void doneClick(View view) {
        Intent intent = new Intent(this, Home.class);
        startActivity(intent);
    }


    public void cameraClick(View view) {

    }
}