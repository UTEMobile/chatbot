package com.example.myapplication.Activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.myapplication.Adapters.MessagesAdapter;
import com.example.myapplication.Models.Message;
import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityMessageBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MessageActivity extends AppCompatActivity {

    ActivityMessageBinding binding;
    MessagesAdapter adapter;
    ArrayList<Message> messages;
    String senderRoom, receiverRoom;
    FirebaseDatabase database;
    int countMessage = 0;
    String url_botAPI = "http://192.168.1.79:5000/";
    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");
    String receiverUid;
    JSONObject json = new JSONObject();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        try {

            super.onCreate(savedInstanceState);
            binding = ActivityMessageBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
//        setSupportActionBar(binding.toolbar);


            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }


            String name = getIntent().getStringExtra("name");
            receiverUid = getIntent().getStringExtra("uid");
            String profileImage = getIntent().getStringExtra("profileImage");
            String senderUid = FirebaseAuth.getInstance().getUid();

            binding.name.setText(name);

            senderRoom = senderUid + receiverUid;
            receiverRoom = receiverUid + senderUid;
            database = FirebaseDatabase.getInstance();

            messages = new ArrayList<>();
            adapter = new MessagesAdapter(this, messages, senderRoom, receiverRoom, profileImage);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

            Glide.with(this).load(profileImage)
                    .placeholder(R.drawable.avatar)
                    .into(binding.profileImage);


//            URL url = new URL(profileImage);
//            Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
//            binding.profileImage.setImageBitmap(bmp);


            binding.recyclerView.setLayoutManager(linearLayoutManager);
            binding.recyclerView.setAdapter(adapter);

            database.getReference().child("chats")
                    .child(senderRoom)
                    .child("messages")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            messages.clear();
                            for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                                Message message = snapshot1.getValue(Message.class);
                                message.setMessageId(snapshot1.getKey());
                                messages.add(message);
                            }

                            if (messages.size() != countMessage) {
                                binding.recyclerView.scrollToPosition(messages.size() - 1);
                                countMessage = messages.size();

                            }
                            adapter.notifyDataSetChanged();

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

            binding.backBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });


            binding.sendBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String messageTxt = binding.messageBox.getText().toString().trim();

                    if (messageTxt.equals("")) {
                        Toast.makeText(MessageActivity.this, "Empty message", Toast.LENGTH_SHORT).show();
                        return;
                    }


                    OkHttpClient okHttpClient = new OkHttpClient();
                    RequestBody body = RequestBody.create(JSON, json.toString());
                    Request request = new Request.Builder()
                            .url(url_botAPI+"chat")
                            .post(body)
                            .build();

                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        // called if server is unreachable
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MessageActivity.this, "server down", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        @Override
                        // called if we get a
                        // response from the server
                        public void onResponse(
                                @NotNull Call call,
                                @NotNull Response response)
                                throws IOException {
                            try {
                                JSONObject jsonRes = new JSONObject(response.body().string());
                                final String req = jsonRes.getString("res");
                                sendMessFromBot(req);
//                            System.out.println(req);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    Date date = new Date();
                    Message message = new Message(messageTxt, senderUid, date.getTime());

                    binding.messageBox.setText("");

                    String randomKey = database.getReference().push().getKey();

                    HashMap<String, Object> lastMsgObj = new HashMap<>();
                    lastMsgObj.put("lastMsg", message.getMessage());
                    lastMsgObj.put("lastMsgTime", date.getTime());

                    database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
                    database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);


                    database.getReference().child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .child(randomKey)
                            .setValue(message)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    database.getReference().child("chats")
                                            .child(receiverRoom)
                                            .child("messages")
                                            .child(randomKey)
                                            .setValue(message)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                }
                                            });

                                }
                            });

                }
            });
        } catch (Exception err){
            Log.d("123456", err.toString());
        }
    }

    void sendMessFromBot(String mess){
        String messageTxt = mess;

        if (messageTxt.equals("")) {
            Toast.makeText(MessageActivity.this, "Empty message", Toast.LENGTH_SHORT).show();
            return;
        }


        Date date = new Date();
        Message message = new Message(messageTxt, receiverUid, date.getTime());

//        binding.messageBox.setText("");

        String randomKey = database.getReference().push().getKey();

        HashMap<String, Object> lastMsgObj = new HashMap<>();
        lastMsgObj.put("lastMsg", message.getMessage());
        lastMsgObj.put("lastMsgTime", date.getTime());

        database.getReference().child("chats").child(senderRoom).updateChildren(lastMsgObj);
        database.getReference().child("chats").child(receiverRoom).updateChildren(lastMsgObj);


        database.getReference().child("chats")
                .child(receiverRoom)
                .child("messages")
                .child(randomKey)
                .setValue(message)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        database.getReference().child("chats")
                                .child(senderRoom)
                                .child("messages")
                                .child(randomKey)
                                .setValue(message)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                    }
                                });

                    }
                });

    }
    //    @SuppressLint("MissingSuperCall")
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        if (requestCode == 100) {
//            cardView.setVisibility(View.VISIBLE);
//            Bitmap captureImage = (Bitmap) data.getExtras().get("data");
//            imageView.setImageBitmap(captureImage);
//        }
//    }

    //////////////////////////////////


//    private void zoomImageFromThumb(final View thumbView, int imageResId) {
//        // If there's an animation in progress, cancel it
//        // immediately and proceed with this one.
//        if (currentAnimator != null) {
//            currentAnimator.cancel();
//        }
//        /////////////////////
//        black.setVisibility(View.VISIBLE);
//////////////////////////
//
//        // Load the high-resolution "zoomed-in" image.
//        final ImageView expandedImageView = (ImageView) findViewById(
//                R.id.expanded_image);
//        expandedImageView.setImageResource(imageResId);
//
//        // Calculate the starting and ending bounds for the zoomed-in image.
//        // This step involves lots of math. Yay, math.
//        final Rect startBounds = new Rect();
//        final Rect finalBounds = new Rect();
//        final Point globalOffset = new Point();
//
//        // The start bounds are the global visible rectangle of the thumbnail,
//        // and the final bounds are the global visible rectangle of the container
//        // view. Also set the container view's offset as the origin for the
//        // bounds, since that's the origin for the positioning animation
//        // properties (X, Y).
//        thumbView.getGlobalVisibleRect(startBounds);
//        findViewById(R.id.container)
//                .getGlobalVisibleRect(finalBounds, globalOffset);
//        startBounds.offset(-globalOffset.x, -globalOffset.y);
//        finalBounds.offset(-globalOffset.x, -globalOffset.y);
//
//        // Adjust the start bounds to be the same aspect ratio as the final
//        // bounds using the "center crop" technique. This prevents undesirable
//        // stretching during the animation. Also calculate the start scaling
//        // factor (the end scaling factor is always 1.0).
//        float startScale;
//        if ((float) finalBounds.width() / finalBounds.height()
//                > (float) startBounds.width() / startBounds.height()) {
//            // Extend start bounds horizontally
//            startScale = (float) startBounds.height() / finalBounds.height();
//            float startWidth = startScale * finalBounds.width();
//            float deltaWidth = (startWidth - startBounds.width()) / 2;
//            startBounds.left -= deltaWidth;
//            startBounds.right += deltaWidth;
//        } else {
//            // Extend start bounds vertically
//            startScale = (float) startBounds.width() / finalBounds.width();
//            float startHeight = startScale * finalBounds.height();
//            float deltaHeight = (startHeight - startBounds.height()) / 2;
//            startBounds.top -= deltaHeight;
//            startBounds.bottom += deltaHeight;
//        }
//
//        // Hide the thumbnail and show the zoomed-in view. When the animation
//        // begins, it will position the zoomed-in view in the place of the
//        // thumbnail.
//        thumbView.setAlpha(0f);
//        expandedImageView.setVisibility(View.VISIBLE);
//
//        // Set the pivot point for SCALE_X and SCALE_Y transformations
//        // to the top-left corner of the zoomed-in view (the default
//        // is the center of the view).
//        expandedImageView.setPivotX(0f);
//        expandedImageView.setPivotY(0f);
//
//        // Construct and run the parallel animation of the four translation and
//        // scale properties (X, Y, SCALE_X, and SCALE_Y).
//        AnimatorSet set = new AnimatorSet();
//        set
//                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
//                        startBounds.left, finalBounds.left))
//                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
//                        startBounds.top, finalBounds.top))
//                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
//                        startScale, 1f))
//                .with(ObjectAnimator.ofFloat(expandedImageView,
//                        View.SCALE_Y, startScale, 1f));
//        set.setDuration(shortAnimationDuration);
//        set.setInterpolator(new DecelerateInterpolator());
//        set.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                currentAnimator = null;
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation) {
//                currentAnimator = null;
//            }
//        });
//        set.start();
//        currentAnimator = set;
//
//        // Upon clicking the zoomed-in image, it should zoom back down
//        // to the original bounds and show the thumbnail instead of
//        // the expanded image.
//        final float startScaleFinal = startScale;
//        expandedImageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (currentAnimator != null) {
//                    currentAnimator.cancel();
//                }
//
//                                // Animate the four positioning/sizing properties in parallel,
//                // back to their original values.
//                AnimatorSet set = new AnimatorSet();
//                set.play(ObjectAnimator
//                        .ofFloat(expandedImageView, View.X, startBounds.left))
//                        .with(ObjectAnimator
//                                .ofFloat(expandedImageView,
//                                        View.Y, startBounds.top))
//                        .with(ObjectAnimator
//                                .ofFloat(expandedImageView,
//                                        View.SCALE_X, startScaleFinal))
//                        .with(ObjectAnimator
//                                .ofFloat(expandedImageView,
//                                        View.SCALE_Y, startScaleFinal));
//                set.setDuration(shortAnimationDuration);
//                set.setInterpolator(new DecelerateInterpolator());
//                set.addListener(new AnimatorListenerAdapter() {
//                    @Override
//                    public void onAnimationEnd(Animator animation) {
//                        thumbView.setAlpha(1f);
//                        expandedImageView.setVisibility(View.GONE);
//                        currentAnimator = null;
//
//                        black.setVisibility(View.GONE);
//
//                    }
//
//                    @Override
//                    public void onAnimationCancel(Animator animation) {
//                        thumbView.setAlpha(1f);
//                        expandedImageView.setVisibility(View.GONE);
//                        currentAnimator = null;
//
//                        black.setVisibility(View.GONE);
//
//                    }
//                });
//                set.start();
//                currentAnimator = set;
//            }
//        });
//    }

}