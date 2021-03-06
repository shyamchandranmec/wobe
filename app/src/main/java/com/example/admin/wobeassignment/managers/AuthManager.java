package com.example.admin.wobeassignment.managers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.exceptions.CleverTapMetaDataNotFoundException;
import com.clevertap.android.sdk.exceptions.CleverTapPermissionsNotSatisfied;
import com.example.admin.wobeassignment.R;
import com.example.admin.wobeassignment.activities.PasscodeActivity;
import com.example.admin.wobeassignment.model.UserModel;
import com.example.admin.wobeassignment.utilities.AeSimpleSHA1;
import com.example.admin.wobeassignment.utilities.CommonUtils;
import com.example.admin.wobeassignment.utilities.Constants;
import com.example.admin.wobeassignment.utilities.SharedPreferenceManager;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by shyam on 25/10/17.
 */

public class AuthManager {
    public static int RC_SIGN_IN = 9001;
    private GoogleSignInAccount acct = null;
    private FirebaseAuth auth;
    private Activity context;
    private FirebaseUser firebaseUser;
    private UserManager userManager;
    public AuthManager(Activity context) {
        this.auth = FirebaseAuth.getInstance();
        this.context = context;
        userManager = new UserManager(context);
    }

    public void handleGoogleSigninResult(GoogleSignInResult result) {
        Log.d("Google sign in", "handleGoogleSigninResult:" + result.isSuccess());
        if (result.isSuccess()) {
            acct = result.getSignInAccount();
            firebaseAuthWithGoogle();
        } else {
            Toast.makeText(context, "Authentication failed.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void firebaseAuthWithGoogle() {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("google sign in", "signInWithCredential:success");
                            FirebaseUser user = auth.getCurrentUser();
                            String firstName = acct.getGivenName();
                            String lastName = acct.getFamilyName();
                            String email = acct.getEmail();
                            String userId = acct.getId();
                            UserModel newUserModel = new UserModel(firstName, lastName, email, userId, 10000, 0, 0, 10000, new ArrayList());
                            getExistingUserOrCreateNewUser(user.getEmail(), newUserModel);
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

    public void getExistingUserOrCreateNewUser(String email, final UserModel newUserModel) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();
        DatabaseReference usersRef = ref.child("users");
        usersRef.child(AeSimpleSHA1.SHA1(email)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.hasChild("email")) {
                    UserModel userModel = snapshot.getValue(UserModel.class);
                    userManager.updateSharedPreference(userModel);
                    userManager.loggedIn();
                    goToNextActivity(PasscodeActivity.class);
                } else {
                    /**
                     * newUserModel will be null for normal login
                     * For social login - if the user doesnt exist in the db
                     * then create a new user - this is same as registering a new user
                     * Don't register a new user if the user is trying to login with an
                     * email and password
                     */
                    if(newUserModel != null) {
                        createNewUser(newUserModel);
                    } else {
                        Toast.makeText(context, "Invalid user",
                                Toast.LENGTH_SHORT).show();
                    }

                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println("error");
            }
        });
    }

    public void createUserWithEmailAndPassword(final String firstName, final String lastName, final String email, String password, String tokenId) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Toast.makeText(context, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                        if (!task.isSuccessful()) {
                            Toast.makeText(context, "Authentication failed." + task.getException(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            FirebaseUser user = auth.getCurrentUser();
                            String userId = user.getUid();
                            UserModel userModel = new UserModel(firstName, lastName, email, userId, 10000, 0, 0, 10000, new ArrayList());
                            createNewUser(userModel);
                        }
                    }
                });
    }

    private void makeApiCallForFacebookLogin(final String firstName, final String lastName, String email, String tokenId) {
        FirebaseUser user = auth.getCurrentUser();
        String userId = tokenId;
        UserModel newUserModel = new UserModel(firstName, lastName, email, userId , 10000, 0, 0, 10000, new ArrayList());
        getExistingUserOrCreateNewUser(email, newUserModel);
    }

    public void makeApiCallForLogin(final String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(context, "Authentication failed", Toast.LENGTH_LONG).show();

                        } else {
                            firebaseUser = auth.getCurrentUser();
                            String uid = firebaseUser.getUid();
                            getExistingUserOrCreateNewUser(firebaseUser.getEmail(), null);
                        }
                    }
                });
    }

    private void fetchDataFromFb(AccessToken token) {
        if(token != null) {
            GraphRequest request = GraphRequest.newMeRequest(
                    token,
                    new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object,
                                                GraphResponse response) {
                            Log.i("LoginActivity",
                                    response.toString());
                            try {
                                             /*
                                               Name and Email is retrieved from Facebook success response
                                            */
                                String name = object.getString("name");
                                String email = object.getString("email");
                                String facebookId = object.getString("id");

                                String firstName = null, lastName = null;

                                              /*
                                               Name is split to Firstname and Lastname
                                            */
                                if (name != null) {
                                    String[] parts = name.split("\\s+");
                                    if (parts.length == 1) {
                                        firstName = parts[0];
                                        lastName = null;
                                    } else if (parts.length == 2) {
                                        firstName = parts[0];
                                        lastName = parts[1];
                                    }
                                }

                                if (facebookId != null && !facebookId.isEmpty() && name != null
                                        && !name.isEmpty() && email != null && !email.isEmpty()) {
                                                 /*
                                                   API call for Social Login
                                                */
                                    if (CommonUtils.isConnectingToInternet(context)) {

                                        makeApiCallForFacebookLogin(firstName, lastName,
                                                email, facebookId);
                                    } else {
                                        Toast.makeText(context, context.getResources().
                                                        getString(R.string.check_internet_connection),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                    facebookLogOut();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
            Bundle parameters = new Bundle();
            parameters.putString("fields",
                    "id,name,email,gender, birthday");
            request.setParameters(parameters);
            request.executeAsync();
        }

    }
    public void facebookLogOut() {
        try {
            LoginManager.getInstance().logOut();
            userManager.loggedOut();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void handleFacebookAccessToken(final AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(context, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("hi", "signInWithCredential:success");
                            FirebaseUser user = auth.getCurrentUser();
                            fetchDataFromFb(token);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("hi", "signInWithCredential:failure", task.getException());
                            Toast.makeText(context, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            fetchDataFromFb(null);
                        }

                        // ...
                    }
                });
    }

    private void createNewUser(UserModel userModel) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();
        DatabaseReference usersRef = ref.child("users");
        usersRef.child(AeSimpleSHA1.SHA1(userModel.getEmail())).setValue(userModel);
        userManager.updateSharedPreference(userModel);
        userManager.newUserRegistered();
        goToNextActivity(PasscodeActivity.class);
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
