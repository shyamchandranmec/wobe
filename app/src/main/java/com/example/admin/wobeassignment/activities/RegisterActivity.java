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
import com.example.admin.wobeassignment.model.TransactionModel;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.tasks.OnCompleteListener;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.Task;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Admin on 20-09-2017.
 */

public class RegisterActivity extends FragmentActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private Button fb, btnRegister;
    private EditText etFirstName, etLastName, etEmail, etPassword;
    private FirebaseAuth auth;
    private  GoogleApiClient mGoogleApiClient;
    private GoogleSignInAccount acct = null;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        AppEventsLogger.activateApp(this);
        authManager = new AuthManager(this);
        super.onCreate(savedInstanceState);
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
        setContentView(R.layout.activity_register);
        fb = (Button) findViewById(R.id.btnFacebookSignUp);
        loginButton = (LoginButton) findViewById(R.id.login_button);
        fb.setOnClickListener(this);

        callbackManager = CallbackManager.Factory.create();
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d("success", "Success");
                        final String accessToken = loginResult.getAccessToken()
                                .getToken();
                        Log.i("accessToken", accessToken);
                        authManager.handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        authManager.facebookLogOut();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(RegisterActivity.this, getResources().getString(R.string.error_message),
                                Toast.LENGTH_SHORT).show();
                    }
                });
        initialiseViews();
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Result code received from Facebook
        if (requestCode == AuthManager.RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            authManager.handleGoogleSigninResult(result);
        } else {
            if (resultCode == RESULT_OK) {
                if (requestCode == CallbackManagerImpl.RequestCodeOffset.Login.toRequestCode()) {
                    callbackManager.onActivityResult(requestCode, resultCode, data);
                }
            }
        }

    }


    /*
    Method to initialise views
   */
    private void initialiseViews() {
        Button btnGoogleSignUp = (Button) findViewById(R.id.btnGoogleSignUp);
        btnGoogleSignUp.setOnClickListener(this);
        etFirstName = (EditText) findViewById(R.id.etFirstname);
        etLastName = (EditText) findViewById(R.id.etLastname);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(this);
    }

    /*
      Method to validate firstname, lastname, email and password
     */
    private void validation() {
        if (!(etFirstName.getText().toString().trim().length() > 0)) {
            Toast.makeText(this, getResources().getText(R.string.enter_first_name), Toast.LENGTH_SHORT).show();
        } else if (!(etLastName.getText().toString().trim().length() > 0)) {
            Toast.makeText(this, getResources().getText(R.string.enter_last_name), Toast.LENGTH_SHORT).show();
        } else if (!(etEmail.getText().toString().trim().length() > 0)) {
            Toast.makeText(this, getResources().getText(R.string.enter_email), Toast.LENGTH_SHORT).show();
        } else if (!(etPassword.getText().toString().trim().length() > 0)) {
            Toast.makeText(this, getResources().getText(R.string.enter_password), Toast.LENGTH_SHORT).show();
        } else {
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String tokenId = "1234567890";

            if (firstName.length() >= 4) {
                if (isValidEmail(email)) {
                    if (password.length() >= 6) {
                        if (CommonUtils.isConnectingToInternet(RegisterActivity.this)) {
                            //register api call
                            authManager.createUserWithEmailAndPassword(firstName, lastName, email, password, tokenId);
                        } else {
                            Toast.makeText(this, getResources().getString(R.string.check_internet_connection),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, getResources().getString(R.string.min_password), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, getResources().getString(R.string.enter_valid_email), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, getResources().getString(R.string.min_first_name), Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Method for email validation
    private final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, AuthManager.RC_SIGN_IN);
    }

    /*
      Method to handle click listeners of views
   */
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.btnGoogleSignUp:
                FrameLayout frameLayout = (FrameLayout) findViewById(R.id.fragment_layout);
                frameLayout.setVisibility(View.VISIBLE);
                signIn();
                break;
            case R.id.btnFacebookSignUp:
                if (CommonUtils.isConnectingToInternet(RegisterActivity.this)) {
                    loginButton.performClick();
                } else {
                    Toast.makeText(this, getResources().getString(R.string.check_internet_connection),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnRegister:
                validation();
                break;
        }
    }
}

