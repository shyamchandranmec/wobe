package com.example.admin.wobeassignment.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.exceptions.CleverTapMetaDataNotFoundException;
import com.clevertap.android.sdk.exceptions.CleverTapPermissionsNotSatisfied;
import com.example.admin.wobeassignment.ApplicationLoader;
import com.example.admin.wobeassignment.R;
import com.example.admin.wobeassignment.fragments.GoogleSignInFragment;
import com.example.admin.wobeassignment.managers.AuthManager;
import com.example.admin.wobeassignment.model.BaseModel;
import com.example.admin.wobeassignment.model.UserModel;
import com.example.admin.wobeassignment.utilities.AeSimpleSHA1;
import com.example.admin.wobeassignment.utilities.CommonUtils;
import com.example.admin.wobeassignment.utilities.Constants;
import com.example.admin.wobeassignment.utilities.SharedPreferenceManager;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.internal.CallbackManagerImpl;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.tasks.OnCompleteListener;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Admin on 19-09-2017.
 */

public class LoginActivity extends FragmentActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private EditText etEmail, etPassword;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private static int RC_SIGN_IN = 9001;
    private GoogleSignInAccount acct = null;
    private GoogleApiClient mGoogleApiClient;
    private AuthManager authManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        AppEventsLogger.activateApp(this);
        super.onCreate(savedInstanceState);
        authManager = new AuthManager(this);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        auth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_login);

        /*
           Facebook Login integration.
           Email permissions asked to use the customer email.
           On successful facebook login, name and email is retrieved.
           API call for Social Login is done, to save details in the server database.
           Facebook Logout is done.
         */
        Button fb = (Button) findViewById(R.id.btnFacebookSignIn);
        loginButton = (LoginButton) findViewById(R.id.login_button);
        fb.setOnClickListener(this);

        callbackManager = CallbackManager.Factory.create();
        loginButton.setReadPermissions("email");
        loginButton.registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d("success", "Success");
                        final String accessToken = loginResult.getAccessToken()
                                .getToken();
                        Log.i("accessToken", accessToken);
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        facebookLogOut();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(LoginActivity.this, getResources().getString(R.string.error_message),
                                Toast.LENGTH_SHORT).show();
                    }
                });
        /*loginButton.registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d("success", "Success");
                        String accessToken = loginResult.getAccessToken()
                                .getToken();
                        Log.i("accessToken", accessToken);
                        *//*GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject object,
                                                            GraphResponse response) {
                                        Log.i("LoginActivity", response.toString());
                                        try {
                                            *//**//*
                                               Name and Email is retrieved from Facebook success response
                                            *//**//*
                                            String name = object.getString("name");
                                            String email = object.getString("email");
                                            String facebookId = object.getString("id");

                                            String firstName = null, lastName = null;

                                            *//**//*
                                               Name is split to Firstname and Lastname
                                            *//**//*
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

                                            *//**//*
                                              Firstname and Lastname saved in Shared Preference
                                             *//**//*
                                            SharedPreferenceManager.getInstance(LoginActivity.this).
                                                    saveData(Constants.USERNAME, name);
                                            SharedPreferenceManager.getInstance(LoginActivity.this).
                                                    saveData(Constants.EMAIL, email);
                                            if (facebookId != null && !facebookId.isEmpty() && name != null
                                                    && !name.isEmpty() && email != null && !email.isEmpty()) {

                                                *//**//*
                                                   API call for Social Login
                                                *//**//*
                                                if (CommonUtils.isConnectingToInternet(LoginActivity.this)) {
                                                    makeApiCallForFacebookLogin(firstName, lastName,
                                                            email, "123445555");
                                                } else {
                                                    Toast.makeText(LoginActivity.this, getResources().
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
                        request.executeAsync();*//*
                    }

                    @Override
                    public void onCancel() {
                        facebookLogOut();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(LoginActivity.this, getResources().getString(R.string.error_message),
                                Toast.LENGTH_SHORT).show();
                    }
                });*/


        initialiseViews();
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

                                             /*
                                              Firstname and Lastname saved in Shared Preference
                                             */
                                SharedPreferenceManager.getInstance(LoginActivity.this).
                                        saveData(Constants.USERNAME, name);
                                SharedPreferenceManager.getInstance(LoginActivity.this).
                                        saveData(Constants.EMAIL, email);
                                if (facebookId != null && !facebookId.isEmpty() && name != null
                                        && !name.isEmpty() && email != null && !email.isEmpty()) {
                                                 /*
                                                   API call for Social Login
                                                */
                                    if (CommonUtils.isConnectingToInternet(LoginActivity.this)) {

                                        makeApiCallForFacebookLogin(firstName, lastName,
                                                email, facebookId);
                                    } else {
                                        Toast.makeText(LoginActivity.this, getResources().
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


    private void handleFacebookAccessToken(final AccessToken token) {
        Log.d("hi", "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
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
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            fetchDataFromFb(null);
                        }

                        // ...
                    }
                });
    }

    /*
      Method for Facebook Logout
    */
    private void facebookLogOut() {
        try {
            LoginManager.getInstance().logOut();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
      Method for Social Login API call
      Request parameters - firstname, lastname, email, tokenid
      Successful response - customerid
    */
    private void makeApiCallForFacebookLogin(final String firstName, final String lastName, String email, String tokenId) {
        FirebaseUser user = auth.getCurrentUser();
        String userId = tokenId;
        authManager.getUserDetails(email);
        //getUserDetails(email);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Result code received from Facebook
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            authManager.handleSignInResult(result);
            //handleSignInResult(result);
        } else {
            if (resultCode == RESULT_OK) {
                if (requestCode == CallbackManagerImpl.RequestCodeOffset.Login.toRequestCode()) {
                    callbackManager.onActivityResult(requestCode, resultCode, data);
                }
            }
        }
    }

    /*private void handleSignInResult(GoogleSignInResult result) {
        Log.d("Google sign in", "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            acct = result.getSignInAccount();
            firebaseAuthWithGoogle();
        } else {
            // Signed out, show unauthenticated UI.
        }
    }*/

   /* private void getUserDetails (String email) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();
        DatabaseReference usersRef = ref.child("users");
        usersRef.child(AeSimpleSHA1.SHA1(email)).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.hasChild("email")) {
                    UserModel userModel = snapshot.getValue(UserModel.class);
                    SharedPreferenceManager.getInstance(LoginActivity.this).
                            saveData(Constants.CUSTOMER_ID, userModel.getUserId());
                    SharedPreferenceManager.getInstance(LoginActivity.this).
                            saveData(Constants.FIRST_NAME, userModel.getFirstName());
                    SharedPreferenceManager.getInstance(LoginActivity.this).
                            saveData(Constants.LAST_NAME, userModel.getLastName());
                    SharedPreferenceManager.getInstance(LoginActivity.this).
                            saveData(Constants.EMAIL, userModel.getEmail());
                    SharedPreferenceManager.getInstance(LoginActivity.this).
                            saveData(Constants.CREDITS, Float.toString(userModel.getCredits()));
                    // pushProfileToCleverTap(userModel.getEmail(), userModel.getFirstName(), userModel.getLastName(), userModel.getCredits());
                    goToNextActivity(PasscodeActivity.class);
                } else {
                    Toast.makeText(LoginActivity.this, getResources().
                                    getString(R.string.invalid_user),
                            Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println("error");
            }
        });
    }*/
    /*private void firebaseAuthWithGoogle() {
        Log.d("Google sign in", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("google sign in", "signInWithCredential:success");
                            FirebaseUser user = auth.getCurrentUser();
                            SharedPreferenceManager.getInstance(LoginActivity.this).
                                    saveData(Constants.EMAIL, user.getEmail());
                            getUserDetails(user.getEmail());
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Google sign in", "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }*/
    /*
      Method to initialise views
     */
    private void initialiseViews() {
        Button btnGoogleSignIn = (Button) findViewById(R.id.btnGoogleSignIn);
        btnGoogleSignIn.setOnClickListener(this);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        Button btnLogin = (Button) findViewById(R.id.btnUserLogin);
        btnLogin.setOnClickListener(this);
    }


    /*
      Method to add a fragment to the activity
     */
    private void addFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        GoogleSignInFragment googleSignInFragment = new GoogleSignInFragment();
        fragmentTransaction.add(R.id.fragment_holder, googleSignInFragment, "GoogleSignIn");
        fragmentTransaction.commit();
    }

    /*
      Method to validate email and password
     */
    private void validation() {
        if (!(etEmail.getText().toString().trim().length() > 0)) {
            Toast.makeText(this, getResources().getText(R.string.enter_email), Toast.LENGTH_SHORT).show();
        } else if (!(etPassword.getText().toString().trim().length() > 0)) {
            Toast.makeText(this, getResources().getText(R.string.enter_password), Toast.LENGTH_SHORT).show();
        } else {
            String email = etEmail.getText().toString().trim();
            if (isValidEmail(email)) {
                String password = etPassword.getText().toString().trim();
                if (CommonUtils.isConnectingToInternet(LoginActivity.this)) {
                    //login api call
                    makeApiCallForLogin(email, password);
                } else {
                    Toast.makeText(this, getResources().getString(R.string.check_internet_connection),
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, getResources().getString(R.string.enter_valid_email), Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Method for email validation
    private final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    /*
      Method to handle click listeners of views
   */
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.btnGoogleSignIn:
                FrameLayout frameLayout = (FrameLayout) findViewById(R.id.fragment_layout);
                frameLayout.setVisibility(View.VISIBLE);
                signIn();
                break;
            case R.id.btnFacebookSignIn:
                if (CommonUtils.isConnectingToInternet(LoginActivity.this)) {
                    loginButton.performClick();
                } else {
                    Toast.makeText(this, getResources().getString(R.string.check_internet_connection),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnUserLogin:
                validation();
                break;

        }
    }

    /*
      Method for Login API call.
      Request parameters - email and password.
      Successful Response - customerid
    */
    private void makeApiCallForLogin(final String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Authentication failed", Toast.LENGTH_LONG).show();

                        } else {
                            firebaseUser = auth.getCurrentUser();
                            String uid = firebaseUser.getUid();
                            SharedPreferenceManager.getInstance(getApplicationContext()).
                                    saveData(Constants.CUSTOMER_ID, uid);
                            SharedPreferenceManager.getInstance(LoginActivity.this).
                                    saveData(Constants.EMAIL, firebaseUser.getEmail());
                            pushProfileToCleverTap(email);
                            goToNextActivity(PasscodeActivity.class);
                        }
                    }
                });
    }
    private void pushProfileToCleverTap(String email, String firstName, String lastName, float credits) {
        CleverTapAPI cleverTap;
        try {
            cleverTap = CleverTapAPI.getInstance(getApplicationContext());
            HashMap<String, Object> profileUpdate = new HashMap<String, Object>();
            profileUpdate.put("email", email);                  // String
            profileUpdate.put("firstName", firstName);
            profileUpdate.put("lastName", lastName);
            profileUpdate.put("credits", credits);
            cleverTap.profile.push(profileUpdate);
        } catch (CleverTapMetaDataNotFoundException e) {
            // thrown if you haven't specified your CleverTap Account ID or Token in your AndroidManifest.xml
        } catch (CleverTapPermissionsNotSatisfied e) {
            // thrown if you haven’t requested the required permissions in your AndroidManifest.xml
        }
    }
    private void pushProfileToCleverTap(String email) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();
        DatabaseReference usersRef = ref.child("users");
        usersRef.child(AeSimpleSHA1.SHA1(email)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                CleverTapAPI cleverTap;
                UserModel userModel = snapshot.getValue(UserModel.class);
                SharedPreferenceManager.getInstance(LoginActivity.this).
                        saveData(Constants.FIRST_NAME, userModel.getFirstName());
                SharedPreferenceManager.getInstance(LoginActivity.this).
                        saveData(Constants.LAST_NAME, userModel.getLastName());
                SharedPreferenceManager.getInstance(LoginActivity.this).
                        saveData(Constants.CREDITS, Float.toString(userModel.getCredits()));
                try {
                    cleverTap = CleverTapAPI.getInstance(getApplicationContext());
                    HashMap<String, Object> profileUpdate = new HashMap<String, Object>();
                    profileUpdate.put("email", userModel.getEmail());                  // String
                    profileUpdate.put("firstName", userModel.getFirstName());
                    profileUpdate.put("lastName", userModel.getLastName());
                    profileUpdate.put("credits", userModel.getCredits());
                    cleverTap.profile.push(profileUpdate);
                } catch (CleverTapMetaDataNotFoundException e) {
                    // thrown if you haven't specified your CleverTap Account ID or Token in your AndroidManifest.xml
                } catch (CleverTapPermissionsNotSatisfied e) {
                    // thrown if you haven’t requested the required permissions in your AndroidManifest.xml
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println("error");
            }
        });
    }

    /*
      Method to go to next activity
    */
    protected void goToNextActivity(Class nextActivity) {
        Intent intent = new Intent();
        intent.setClass(this, nextActivity);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.KEY_PASSCODE_ACTIVITY_BUNDLE, Constants.VALUE_LOGIN_ACTIVITY);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}