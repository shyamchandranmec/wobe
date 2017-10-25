package com.example.admin.wobeassignment.managers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.example.admin.wobeassignment.R;
import com.example.admin.wobeassignment.activities.LoginActivity;
import com.example.admin.wobeassignment.activities.PasscodeActivity;
import com.example.admin.wobeassignment.model.UserModel;
import com.example.admin.wobeassignment.utilities.AeSimpleSHA1;
import com.example.admin.wobeassignment.utilities.Constants;
import com.example.admin.wobeassignment.utilities.SharedPreferenceManager;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by shyam on 25/10/17.
 */

public class AuthManager {
    private static int RC_SIGN_IN = 9001;
    private GoogleSignInAccount acct = null;
    private FirebaseAuth auth;
    private Activity context;
    public AuthManager(Activity context) {
        this.auth = FirebaseAuth.getInstance();
        this.context = context;
    }

    public void handleSignInResult(GoogleSignInResult result) {
        Log.d("Google sign in", "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            acct = result.getSignInAccount();
            firebaseAuthWithGoogle();
        } else {
            // Signed out, show unauthenticated UI.
        }
    }

    public void firebaseAuthWithGoogle() {
        Log.d("Google sign in", "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("google sign in", "signInWithCredential:success");
                            FirebaseUser user = auth.getCurrentUser();
                            SharedPreferenceManager.getInstance(context).
                                    saveData(Constants.EMAIL, user.getEmail());
                            getUserDetails(user.getEmail());
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Google sign in", "signInWithCredential:failure", task.getException());
                            Toast.makeText(context, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    public void getUserDetails (String email) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();
        DatabaseReference usersRef = ref.child("users");
        usersRef.child(AeSimpleSHA1.SHA1(email)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.hasChild("email")) {
                    UserModel userModel = snapshot.getValue(UserModel.class);
                    SharedPreferenceManager.getInstance(context).
                            saveData(Constants.CUSTOMER_ID, userModel.getUserId());
                    SharedPreferenceManager.getInstance(context).
                            saveData(Constants.FIRST_NAME, userModel.getFirstName());
                    SharedPreferenceManager.getInstance(context).
                            saveData(Constants.LAST_NAME, userModel.getLastName());
                    SharedPreferenceManager.getInstance(context).
                            saveData(Constants.EMAIL, userModel.getEmail());
                    SharedPreferenceManager.getInstance(context).
                            saveData(Constants.CREDITS, Float.toString(userModel.getCredits()));
                    // pushProfileToCleverTap(userModel.getEmail(), userModel.getFirstName(), userModel.getLastName(), userModel.getCredits());
                    goToNextActivity(PasscodeActivity.class);
                } else {
                    Toast.makeText(context, context.getResources().
                                    getString(R.string.invalid_user),
                            Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println("error");
            }
        });
    }
    protected void goToNextActivity(Class nextActivity) {
        Intent intent = new Intent();
        intent.setClass(context, nextActivity);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.KEY_PASSCODE_ACTIVITY_BUNDLE, Constants.VALUE_LOGIN_ACTIVITY);
        intent.putExtras(bundle);
        context.startActivity(intent);
        context.finish();
    }
}
