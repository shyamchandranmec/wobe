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
    private static int RC_SIGN_IN = 9001;
    GoogleSignInAccount acct = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        AppEventsLogger.activateApp(this);
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

         /*
           Facebook Login integration.
           Email permissions asked to use the customer email.
           On successful facebook login, name and email is retrieved.
           API call for Social Login is done, to save details in the server database.
           Facebook Logout is done.
         */
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
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        facebookLogOut();
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
                                SharedPreferenceManager.getInstance(RegisterActivity.this).
                                        saveData(Constants.USERNAME, name);
                                SharedPreferenceManager.getInstance(RegisterActivity.this).
                                        saveData(Constants.EMAIL, email);
                                if (facebookId != null && !facebookId.isEmpty() && name != null
                                        && !name.isEmpty() && email != null && !email.isEmpty()) {
                                                 /*
                                                   API call for Social Login
                                                */
                                    if (CommonUtils.isConnectingToInternet(RegisterActivity.this)) {

                                        makeApiCallForFacebookLogin(firstName, lastName,
                                                email, facebookId);
                                    } else {
                                        Toast.makeText(RegisterActivity.this, getResources().
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
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            fetchDataFromFb(null);
                        }

                        // ...
                    }
                });
    }

    private void makeApiCallForFacebookLogin(final String firstName, final String lastName, String email, String tokenId) {
        FirebaseUser user = auth.getCurrentUser();
        String userId = tokenId;
        UserModel userModel = new UserModel(firstName, lastName, email, userId + "fb", 10000, 0, 0, 10000, new ArrayList());
        createNewUser(userModel);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Result code received from Facebook
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        } else {
            if (resultCode == RESULT_OK) {
                if (requestCode == CallbackManagerImpl.RequestCodeOffset.Login.toRequestCode()) {
                    callbackManager.onActivityResult(requestCode, resultCode, data);
                }
            }
        }

    }
    private void firebaseAuthWithGoogle() {
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
                            String firstName = acct.getGivenName();
                            String lastName = acct.getFamilyName();
                            String email = acct.getEmail();
                            String userId = acct.getId();
                            UserModel userModel = new UserModel(firstName, lastName, email, userId, 10000, 0, 0, 10000, new ArrayList());
                            createNewUser(userModel);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Google sign in", "signInWithCredential:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            createNewUser(null);
                        }

                        // ...
                    }
                });
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d("Google sign in", "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            acct = result.getSignInAccount();
            firebaseAuthWithGoogle();
        } else {
            // Signed out, show unauthenticated UI.
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
                            makeApiCall(firstName, lastName, email, password, tokenId);
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


    /*
      Method to add fragment
    */
    /*private void addFragment() {
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        GoogleSignInFragment googleSignInFragment = new GoogleSignInFragment();
        fragmentTransaction.add(R.id.fragment_holder, googleSignInFragment, "GoogleSignIn");
        fragmentTransaction.commit();
    }*/
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

    /*
     Method for Register API call.
     Request parameters - firstname, lastname, email, password and tokenid.
     Successful Response - customerid
   */
    private void makeApiCall(final String firstName, final String lastName, final String email, String password, String tokenId) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Toast.makeText(RegisterActivity.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                        if (!task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Authentication failed." + task.getException(),
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

    private void createNewUser(UserModel userModel) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();
        DatabaseReference usersRef = ref.child("users");
        usersRef.child(AeSimpleSHA1.SHA1(userModel.getEmail())).setValue(userModel);
        SharedPreferenceManager.getInstance(RegisterActivity.this).
                saveData(Constants.CUSTOMER_ID, userModel.getUserId());
        SharedPreferenceManager.getInstance(RegisterActivity.this).
                saveData(Constants.FIRST_NAME, userModel.getFirstName());
        SharedPreferenceManager.getInstance(RegisterActivity.this).
                saveData(Constants.LAST_NAME, userModel.getLastName());
        SharedPreferenceManager.getInstance(RegisterActivity.this).
                saveData(Constants.EMAIL, userModel.getEmail());
        SharedPreferenceManager.getInstance(RegisterActivity.this).
                saveData(Constants.CREDITS, Float.toString(userModel.getCredits()));
        pushProfileToCleverTap(userModel.getEmail(), userModel.getFirstName(), userModel.getLastName(), userModel.getCredits());
        goToNextActivity(PasscodeActivity.class);
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

    /*
      Method to go to next activity
    */
    protected void goToNextActivity(Class nextActivity) {
        Intent intent = new Intent();
        intent.setClass(this, nextActivity);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.KEY_PASSCODE_ACTIVITY_BUNDLE, Constants.VALUE_REGISTER_ACTIVITY);
        intent.putExtras(bundle);
        startActivity(intent);
        finish();
    }
}

