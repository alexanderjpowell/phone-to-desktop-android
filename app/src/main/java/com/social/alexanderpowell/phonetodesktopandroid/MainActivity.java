package com.social.alexanderpowell.phonetodesktopandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageView shareButton, sendButton, settingsButton;
    private TextInputEditText editText;
    private FirebaseFirestore database;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = FirebaseFirestore.getInstance();

        shareButton = findViewById(R.id.shareButton);
        sendButton = findViewById(R.id.sendButton);
        settingsButton = findViewById(R.id.settingsButton);
        editText = findViewById(R.id.edit);

        shareButton.setOnClickListener(this);
        sendButton.setOnClickListener(this);
        settingsButton.setOnClickListener(this);

        createNotificationChannel();

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channel_id")
                .setSmallIcon(R.drawable.outline_send_24)
                .setContentTitle("My notification")
                .setContentText("Much longer text that cannot fit one line...")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        //notificationManager.notify(1, builder.build());

        ////
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult().getToken();

                        // Log and toast
                        String msg = getString(R.string.msg_token_fmt, token);
                        Log.d(TAG, msg);
                    }
                });
        ////

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_request_id_token))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == shareButton.getId()) {
            shareText();
        } else if (view.getId() == sendButton.getId()) {
            sendText();
        } else if (view.getId() == settingsButton.getId()) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "channel_name";//getString(R.string.<>)
            String description = "channel_description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("channel_id", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void shareText() {
        hideKeyboard(this);

        editText.clearFocus();

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, editText.getText().toString());
        sendIntent.setType("text/plain");
        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }

    private void sendText() {
        hideKeyboard(this);

        editText.clearFocus();

        // Write to firestore db
        Snackbar.make(sendButton, "Sent to web interface", Snackbar.LENGTH_SHORT).show();

        Map<String, Object> data = new HashMap<>();
        data.put("text", editText.getText().toString());
        database.collection("accounts")
                .document(mAuth.getCurrentUser().getUid())
                .set(data, SetOptions.merge());
    }

    private static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
