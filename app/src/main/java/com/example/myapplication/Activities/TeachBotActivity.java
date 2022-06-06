package com.example.myapplication.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.Models.PostJson;
import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityMessageBinding;
import com.example.myapplication.databinding.ActivityTeachBotBinding;
import com.google.firebase.database.annotations.NotNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TeachBotActivity extends AppCompatActivity {
    ActivityTeachBotBinding binding;
//    String url_botAPI = "http://35.209.231.238:8080/";
    String url_botAPI = "https://middlechatbotapi.herokuapp.com/";
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    EditText txtInput;
    EditText txtOutput;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTeachBotBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        txtInput = findViewById(R.id.messageBoxInput);
        txtOutput = findViewById(R.id.messageBoxOutput);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        binding.sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject json = new JSONObject();
                String reponse = "";
//                AlertDialog  alert = new AlertDialog.Builder(TeachBotActivity.this)
//                        .setTitle("Save this teaching")
//                        .setMessage("Are you sure you want to save this teaching?")
//                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//
//                            }
//                        })
//                        // A null listener allows the button to dismiss the dialog and take no further action.
//                        .setNegativeButton(android.R.string.no, null)
//                        .setIcon(android.R.drawable.ic_dialog_alert)
//                        .show();

                if (txtInput.getText().toString().equals("")||txtOutput.getText().toString().equals("")) {
                    Toast.makeText(TeachBotActivity.this, "Please enter in full!", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    json.put("input",txtInput.getText());
                    json.put("output",txtOutput.getText());
                    reponse = doPostTeach(json,url_botAPI+"teach");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Toast.makeText(TeachBotActivity.this,reponse,Toast.LENGTH_SHORT).show();
                txtInput.setText("");
                txtOutput.setText("");
            }
        });
    }
    String doPostTeach(JSONObject json,String url) throws JSONException {
        PostJson exPost = new PostJson(url,json);
        String reponse = "";
        Thread thread = new Thread(exPost);
        thread.start();
        try {
            thread.join();
            reponse = exPost.getReponse().getString("res_teach");
            thread.interrupt();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return reponse;
    }
}