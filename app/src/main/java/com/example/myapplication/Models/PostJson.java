package com.example.myapplication.Models;
import android.widget.Toast;

import com.example.myapplication.Activities.MessageActivity;
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


public class PostJson implements Runnable{
    static final OkHttpClient okHttpClient = new OkHttpClient();
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final String url;
    private final JSONObject json_request;
    JSONObject json_reponse;

    public PostJson(String url, JSONObject json){
        this.url = url;
        this.json_request = json;
    }
    @Override
    public void run() {
        RequestBody body = RequestBody.create(JSON, this.json_request.toString());
        Request request = new Request.Builder()
                .url(this.url)
                .post(body)
                .build();
        Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            json_reponse = new JSONObject(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public JSONObject getReponse(){
        return this.json_reponse;
    }
}
