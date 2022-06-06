package com.example.myapplication.Activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.databinding.ActivityOtpactivityBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mukesh.OnOtpCompletionListener;

import java.util.concurrent.TimeUnit;

public class OTPActivity extends AppCompatActivity {
    private ActivityOtpactivityBinding binding;
    private String verificationId;
    private FirebaseAuth auth;
    private ProgressDialog dialog;

    FirebaseDatabase database;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        binding = ActivityOtpactivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        Show keyboard - if keyboard didn't show
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        binding.editText.requestFocus();

        // Get phoneNumber from intent
        Intent intent = getIntent();
        String phoneNumber = intent.getStringExtra("phoneNumber");
        binding.verifyText.setText("Verify " + phoneNumber);

//        Auth and get verificationId
        auth = FirebaseAuth.getInstance();
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(OTPActivity.this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                    }

                    @Override
                    public void onCodeSent(@NonNull String verifyId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(verifyId, forceResendingToken);
                        verificationId = verifyId;

                        dialog.dismiss();
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {

                    }
                }).build();

//        Verify Phone Number
        PhoneAuthProvider.verifyPhoneNumber(options);

//        Set on listener
        binding.editText.setOtpCompletionListener(new OnOtpCompletionListener() {
            @Override
            public void onOtpCompleted(String otp) {
//                Get Credential
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);

//                Sign with Credential
                auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            database = FirebaseDatabase.getInstance();
                            database.getReference().child("users").child(auth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    Intent intent = new Intent(OTPActivity.this, SetupProfileActivity.class);

                                    if(dataSnapshot.exists()) {
                                        //Key exists
                                        intent = new Intent(OTPActivity.this, HomeActivity.class);

                                    } else {
                                        //Key does not exist
                                    }

//                                    Log.d("123456", dataSnapshot.toString());

                                    startActivity(intent);
                                    finishAffinity();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            }
                        );

//                            if (database.getReference().child("users").child(auth.getUid()).get("name") != null){}


                    } else

                    {
                        Toast.makeText(OTPActivity.this, "Failed.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    });

//        Show ProgressDialog when sending OTP SMS
    dialog =new

    ProgressDialog(this);
        dialog.setMessage("Sending OTP...");
        dialog.setCancelable(false);
        dialog.show();
}
}