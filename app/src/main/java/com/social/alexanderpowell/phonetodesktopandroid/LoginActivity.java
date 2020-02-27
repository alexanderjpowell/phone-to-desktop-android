package com.social.alexanderpowell.phonetodesktopandroid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private SignInButton signInButton;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private FirebaseFirestore database;
    private static int RC_SIGN_IN = 101;

    private String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signInButton = findViewById(R.id.sign_in_button);
        signInButton.setStyle(SignInButton.SIZE_WIDE, SignInButton.COLOR_AUTO);
        signInButton.setOnClickListener(this);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_request_id_token))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();

        database = FirebaseFirestore.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == signInButton.getId()) {
            signIn();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Toast.makeText(LoginActivity.this, "Sign in successful" + account.getEmail(), Toast.LENGTH_SHORT).show();

            // Now that Google sign in is successful, sign in with Firebase
            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                //
                                FirebaseInstanceId.getInstance().getInstanceId()
                                        .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                                if (!task.isSuccessful()) {
                                                    Log.w(TAG, "getInstanceId failed", task.getException());
                                                    return;
                                                }

                                                Map<String, Object> data = new HashMap<>();

                                                // Get new Instance ID token
                                                String token = task.getResult().getToken();
                                                data.put("registrationToken", token);

                                                database.collection("accounts").document(mAuth.getUid()).set(data, SetOptions.merge());

                                                // Log and toast
                                                String msg = getString(R.string.msg_token_fmt, token);
                                                Log.d(TAG, msg);
                                            }
                                        });
                                //

                                FirebaseUser user = mAuth.getCurrentUser();
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(getApplicationContext(), "Firebase Authentication Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(LoginActivity.this, "signInResult:failed code=" + e.getStatusCode(), Toast.LENGTH_LONG).show();
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
}
