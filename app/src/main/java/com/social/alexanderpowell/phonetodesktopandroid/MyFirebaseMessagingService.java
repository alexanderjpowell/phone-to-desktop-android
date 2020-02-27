package com.social.alexanderpowell.phonetodesktopandroid;

import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private FirebaseFirestore database;
    private FirebaseAuth mAuth;

    private String TAG = "MyFirebaseMessagingService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);
        //Toast.makeText(getApplicationContext(), "Refreshed token: " + token, Toast.LENGTH_SHORT).show();
        database = FirebaseFirestore.getInstance();
        Map<String, Object> data = new HashMap<>();
        data.put("registrationToken", token);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            database.collection("accounts").document(currentUser.getUid()).set(data, SetOptions.merge());
        }
    }
}