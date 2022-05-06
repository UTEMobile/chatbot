package com.example.myapplication.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.myapplication.databinding.ActivityEnterYourPhoneNumberBinding;

public class EnterYourPhoneNumberActivity extends AppCompatActivity {

    ActivityEnterYourPhoneNumberBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEnterYourPhoneNumberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();
        binding.phoneNumber.requestFocus();


        binding.doneClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EnterYourPhoneNumberActivity.this, OTPActivity.class);
                intent.putExtra("phoneNumber", binding.phoneNumber.getText().toString().trim());
                startActivity(intent);
            }
        });
    }
}