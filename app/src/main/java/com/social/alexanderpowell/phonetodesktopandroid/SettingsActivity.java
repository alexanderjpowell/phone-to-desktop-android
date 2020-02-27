package com.social.alexanderpowell.phonetodesktopandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private MaterialButton signOutButton;
    private TextView emailTextView;

    private String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_request_id_token))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
        }

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        signOutButton = findViewById(R.id.sign_out_button);
        emailTextView = findViewById(R.id.email_text_view);

        signOutButton.setOnClickListener(this);

        emailTextView.setText("email: " + currentUser.getEmail());
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == signOutButton.getId()) {
            signOut();
        }
    }

    private void signOut() {
        // Firebase sign out
        FirebaseAuth.getInstance().signOut();

        // Google sign out
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "signed out", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        } else {
                            Toast.makeText(getApplicationContext(), "failed to sign out", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
