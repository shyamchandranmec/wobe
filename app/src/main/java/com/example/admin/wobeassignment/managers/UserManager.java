package com.example.admin.wobeassignment.managers;

import android.app.Activity;

import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.exceptions.CleverTapMetaDataNotFoundException;
import com.clevertap.android.sdk.exceptions.CleverTapPermissionsNotSatisfied;
import com.example.admin.wobeassignment.activities.DashboardActivity;
import com.example.admin.wobeassignment.model.UserModel;
import com.example.admin.wobeassignment.utilities.AeSimpleSHA1;
import com.example.admin.wobeassignment.utilities.Constants;
import com.example.admin.wobeassignment.utilities.SharedPreferenceManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

/**
 * Created by shyam on 28/10/17.
 */

public class UserManager {
    Activity context;

    public UserManager (Activity context) {
        this.context = context;

    }
    public void addSharedPreferenceListener(String email) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();
        DatabaseReference usersRef = ref.child("users");
        usersRef.child(AeSimpleSHA1.SHA1(email)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                UserModel userModel = snapshot.getValue(UserModel.class);
                updateSharedPreference(userModel);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println("error");
            }
        });
    }

    private void updateSharedPreference(UserModel userModel) {
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
        SharedPreferenceManager.getInstance(context).
                saveTransactionList(Constants.TRANS_LIST, userModel.getTransactions());
        pushProfileToCleverTap(userModel.getEmail(), userModel.getFirstName(), userModel.getLastName(), userModel.getCredits());
    }

    private void pushProfileToCleverTap(String email, String firstName, String lastName, float credits) {
        CleverTapAPI cleverTap;
        try {
            cleverTap = CleverTapAPI.getInstance(context.getApplicationContext());
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
}
