package com.example.admin.wobeassignment.fragments;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.admin.wobeassignment.ApplicationLoader;
import com.example.admin.wobeassignment.R;
import com.example.admin.wobeassignment.activities.PasscodeActivity;
import com.example.admin.wobeassignment.model.BaseModel;
import com.example.admin.wobeassignment.utilities.CommonUtils;
import com.example.admin.wobeassignment.utilities.Constants;
import com.example.admin.wobeassignment.utilities.SharedPreferenceManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Admin on 19-09-2017.
 */

public class GoogleSignInFragment extends android.support.v4.app.Fragment implements
        GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;
    private static String TAG = GoogleSignInFragment.class.toString();
    private Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {

        context = getContext();
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_google_sign_in,
                container, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }


        if (CommonUtils.isConnectingToInternet(getActivity())) {
            googleSignIn();
        } else {
            Toast.makeText(context, getResources().getString(R.string.check_internet_connection),
                    Toast.LENGTH_SHORT).show();
        }

        if (CommonUtils.isConnectingToInternet(getActivity())) {
            signIn();
        } else {
            Toast.makeText(context, getResources().getString(R.string.check_internet_connection),
                    Toast.LENGTH_SHORT).show();
        }
        return rootView;
    }


    /*
      Method to build the GoogleApiClient
     */
    private void googleSignIn() {
        try {
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.
                    DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();

            if (null == mGoogleApiClient) {
                mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                        .enableAutoManage(getActivity() /* FragmentActivity */,
                                this/* OnConnectionFailedListener */)
                        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                        .build();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
      Method to enable Google Sign In, where google show their fragment to sign in
     */
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /*
          Google sends back result
         */
        if (resultCode == getActivity().RESULT_OK) {
            if (requestCode == RC_SIGN_IN) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                handleSignInResult(result);
            }
        }
    }

    /*
      Method to handle result sent by Google.

      On Success, retrieve email and name.
      Save email and name in Shared Preference.
      Make Social Login API call
     */
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleGoogleSigninResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            String userName = acct.getDisplayName();
            String firstName = null, lastName = null;

            if (userName != null) {
                String[] parts = userName.split("\\s+");
                if (parts.length == 1) {
                    firstName = parts[0];
                    lastName = null;
                } else if (parts.length == 2) {
                    firstName = parts[0];
                    lastName = parts[1];
                }
            }

            String email = acct.getEmail();
            String googleId = acct.getId();
            SharedPreferenceManager.getInstance(context).saveData(Constants.USERNAME, userName);
            SharedPreferenceManager.getInstance(context).saveData(Constants.EMAIL, email);
            if (CommonUtils.isConnectingToInternet(getActivity())) {
                //make Social Login API call
                makeApiCall(firstName, lastName, email, "123445555");
            } else {
                Toast.makeText(context, getResources().getString(R.string.check_internet_connection),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Signed out, show unauthenticated UI.
            Toast.makeText(getActivity(), getResources().getString(R.string.authentication_failed),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /*
      Method to make social login API call
      Request parameters - firstname, lastname, email, tokenid
      Success response - customerid
     */
    private void makeApiCall(final String firstName, final String lastName, String email, String tokenId) {
        String url = String.format(Constants.SOCIAL_LOGIN_URL, firstName, lastName, email, tokenId);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response != null && response.getString("returnStatus").equalsIgnoreCase("SUCCESS")) {
                                BaseModel model = new Gson().fromJson
                                        (response.toString(), BaseModel.class);
                                String customerId = model.getCustomerID().toString();
                                SharedPreferenceManager.getInstance(context).saveData(Constants.CUSTOMER_ID,
                                        customerId);
                                SharedPreferenceManager.getInstance(context).saveData(Constants.FIRST_NAME,
                                        firstName);
                                SharedPreferenceManager.getInstance(context).saveData(Constants.LAST_NAME,
                                        lastName);
                                goToNextActivity(PasscodeActivity.class);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        ApplicationLoader.getRequestQueue().add(jsonObjectRequest);
    }

    /*
      Method to go to next activity
     */
    protected void goToNextActivity(Class nextActivity) {
        Intent intent = new Intent();
        intent.setClass(getContext(), nextActivity);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.KEY_PASSCODE_ACTIVITY_BUNDLE, Constants.VALUE_FOR_GOOGLE_SIGN);
        intent.putExtras(bundle);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.stopAutoManage(getActivity());
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.stopAutoManage(getActivity());
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.stopAutoManage(getActivity());
            mGoogleApiClient.disconnect();
        }
    }
}

