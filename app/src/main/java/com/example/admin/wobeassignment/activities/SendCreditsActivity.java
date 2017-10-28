package com.example.admin.wobeassignment.activities;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.admin.wobeassignment.R;
import com.example.admin.wobeassignment.managers.UserManager;
import com.example.admin.wobeassignment.model.BaseModel;
import com.example.admin.wobeassignment.model.TransactionModel;
import com.example.admin.wobeassignment.model.UserModel;
import com.example.admin.wobeassignment.model.VerifyUserModel;
import com.example.admin.wobeassignment.utilities.AeSimpleSHA1;
import com.example.admin.wobeassignment.utilities.CommonUtils;
import com.example.admin.wobeassignment.utilities.Constants;
import com.example.admin.wobeassignment.utilities.FontManager;
import com.example.admin.wobeassignment.utilities.SharedPreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Admin on 21-09-2017.
 */

public class SendCreditsActivity extends AppCompatActivity implements View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener{
    private EditText etCredits, etDescription, etEmail;
    private Button btnSendCredits, tvVerify;
    private String toCustomerId, fromCustomerId, toFirstName, toLastName, fromFirstName, fromLastName;
    private TextView tvName, tvBalance;
    private UserModel toUserModel, fromUserModel;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private UserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_credits);
        initialiseToolbar();
        initialiseViews();
        fromCustomerId = SharedPreferenceManager.getInstance(this).getString(Constants.CUSTOMER_ID);
        userManager = new UserManager(this);
        userManager.addSharedPreferenceListener(SharedPreferenceManager.getInstance(this).getString(Constants.EMAIL));
        updateView();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        updateView();
    }

    private  void updateView() {
        if (SharedPreferenceManager.getInstance(SendCreditsActivity.this).getString(Constants.FIRST_NAME) != null) {
            tvName.setText(SharedPreferenceManager.getInstance(SendCreditsActivity.this).getString(Constants.FIRST_NAME));
        }

        if (SharedPreferenceManager.getInstance(SendCreditsActivity.this).getString(Constants.CREDITS) != null) {
            tvBalance.setText(getResources().getString(R.string.balance) + SharedPreferenceManager.
                    getInstance(SendCreditsActivity.this).getString(Constants.CREDITS));
        }
    }
    private void initialiseViews() {
        etEmail = (EditText) findViewById(R.id.etEmail);
        tvVerify = (Button) findViewById(R.id.tvVerify);
        tvVerify.setOnClickListener(this);
        etCredits = (EditText) findViewById(R.id.etCredits);
        etDescription = (EditText) findViewById(R.id.etDescription);
        btnSendCredits = (Button) findViewById(R.id.btnSend);
        btnSendCredits.setOnClickListener(this);
        tvName = (TextView) findViewById(R.id.tvName);
        tvBalance = (TextView) findViewById(R.id.tvBalance);
    }

    /*
      Method to initialise toolbar and set title
    */
    private void initialiseToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getText(R.string.send_credits));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /*
      Method to handle back arrow click from toolbar
    */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    /*
      Method to handle click listeners of views
    */
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.tvVerify:
                String email = etEmail.getText().toString().trim();
                if (email != null && !email.isEmpty()) {
                    if (email.equalsIgnoreCase(SharedPreferenceManager.getInstance(SendCreditsActivity.this).
                            getString(Constants.EMAIL))) {
                        Toast.makeText(this, getString(R.string.cannot_send_credits), Toast.LENGTH_SHORT).show();
                    } else {
                        if (CommonUtils.isConnectingToInternet(SendCreditsActivity.this)) {
                            verifyUserApiCall(email);
                        } else {
                            Toast.makeText(this, getResources().getString(R.string.check_internet_connection),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(this, getResources().getString(R.string.enter_email_to_verify), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnSend:
                sendCreditsValidation();
                break;
        }
    }

    /*
      Method for User validation API call.
      Request parameters - email.
      Successful response - toCustomerId, toFirstName, toLastname
    */
    private void verifyUserApiCall(final String email) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();
        final DatabaseReference usersRef = ref.child("users");
        String emailHash = AeSimpleSHA1.SHA1(email);
        usersRef.child(emailHash).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.hasChild("email")) {
                    UserModel userModel = snapshot.getValue(UserModel.class);
                    toUserModel = userModel;
                    toCustomerId = userModel.getEmail();
                    tvVerify.setVisibility(View.GONE);
                    etEmail.setEnabled(false);
                    etCredits.setEnabled(true);
                    etCredits.setHintTextColor(getResources().getColor(R.color.colorPrimary));
                    etDescription.setEnabled(true);
                    etDescription.setHintTextColor(getResources().getColor(R.color.colorPrimary));
                    btnSendCredits.setEnabled(true);
                    etEmail.setTextColor(getResources().getColor(R.color.edit_text_disable_color));
                    toFirstName = userModel.getFirstName();
                    toLastName = userModel.getLastName();
                } else {
                    Toast.makeText(SendCreditsActivity.this, getResources().
                                    getString(R.string.invalid_user),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println("error");
            }
        });
        usersRef.child(AeSimpleSHA1.SHA1(SharedPreferenceManager.getInstance(this).getString(Constants.EMAIL))).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                UserModel userModel = snapshot.getValue(UserModel.class);
                fromUserModel = userModel;
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println("error");
            }
        });
    }


    /*
      Method for credits entered and description entered validation
    */
    private void sendCreditsValidation() {
        if (!(etCredits.getText().toString().trim().length() > 0)) {
            Toast.makeText(this, getResources().getText(R.string.enter_cedits), Toast.LENGTH_SHORT).show();
        } else if (!(etDescription.getText().toString().trim().length() > 0)) {
            Toast.makeText(this, getResources().getText(R.string.enter_description), Toast.LENGTH_SHORT).show();
        } else {
            checkCreditBalance(etCredits.getText().toString().trim());
        }
    }

    /*
      Method to check credit balance with the entered credits.
      If entered credit <= the credit balance, then Send Credits API call is done.
      else, customer is indicated that the balance is insufficient
     */
    private void checkCreditBalance(String credits) {
        BigInteger toCredits = BigInteger.valueOf(Long.valueOf(credits));
        String x = SharedPreferenceManager.
                getInstance(this).getString(Constants.CREDITS);
        BigInteger userCredits = new BigDecimal(Float.valueOf(x)).toBigInteger();
        int availableCredits = userCredits.compareTo(toCredits);
        if (availableCredits == 0) {
            if (CommonUtils.isConnectingToInternet(SendCreditsActivity.this)) {
                btnSendCredits.setEnabled(false);
                sendCreditsApiCall(etCredits.getText().toString().trim(), etDescription.getText().toString());
            } else {
                Toast.makeText(this, getResources().getString(R.string.check_internet_connection),
                        Toast.LENGTH_SHORT).show();
            }
        } else if (availableCredits == 1) {
            if (CommonUtils.isConnectingToInternet(SendCreditsActivity.this)) {
                btnSendCredits.setEnabled(false);
                sendCreditsApiCall(etCredits.getText().toString().trim(), etDescription.getText().toString());
            } else {
                Toast.makeText(this, getResources().getString(R.string.check_internet_connection),
                        Toast.LENGTH_SHORT).show();
            }
        } else if (availableCredits == -1) {
            Toast.makeText(this, getResources().getString(R.string.insufficient_credits), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        userManager.addCustomEvent("Visited Send Credits Activity");
    }


    /*
       Method to make Send Credits API call.

       Request parameters - fromCustomerId, fromFirstname, fromLastname,
       toCustomerId, toFirstname, toLastname, credits, description, noteToSelf

       Successful response - Success Message
    */
    private void sendCreditsApiCall(String credits, String description) {
        float amount = Float.parseFloat(credits);
        if (SharedPreferenceManager.getInstance(this).getString(Constants.FIRST_NAME) != null &&
                SharedPreferenceManager.getInstance(this).getString(Constants.LAST_NAME) != null) {
            fromFirstName = SharedPreferenceManager.getInstance(this).getString(Constants.FIRST_NAME);
            fromLastName = SharedPreferenceManager.getInstance(this).getString(Constants.LAST_NAME);
        }
        if (SharedPreferenceManager.getInstance(this).getString(Constants.LAST_NAME) != null) {
            fromLastName = SharedPreferenceManager.getInstance(this).getString(Constants.LAST_NAME);
        }

        String url = String.format(Constants.SEND_CREDITS_URL, fromCustomerId, fromFirstName, fromLastName,
                toCustomerId, toFirstName, toLastName, credits, description, null);
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();
        final DatabaseReference usersRef = ref.child("users");
        TransactionModel fromTransactionModel = new TransactionModel();
        TransactionModel toTransactionModel = new TransactionModel();
        String tid = fromUserModel.getUserId() + "--" + fromUserModel.getTransactions().size();
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd--HH:mm").format(Calendar.getInstance().getTime());

        fromTransactionModel.setTransactionId(tid);
        fromTransactionModel.setFromCustomerID(fromUserModel.getEmail());
        fromTransactionModel.setFromFirstName(fromUserModel.getFirstName());
        fromTransactionModel.setFromLastName(fromUserModel.getLastName());
        fromTransactionModel.setToCustomerID(toUserModel.getEmail());
        fromTransactionModel.setToFirstName(toUserModel.getFirstName());
        fromTransactionModel.setToLastName(toUserModel.getLastName());
        fromTransactionModel.setTransactionDate(timeStamp);
        fromTransactionModel.setCredits(amount);

        toTransactionModel.setTransactionId(tid);
        toTransactionModel.setFromCustomerID(fromUserModel.getEmail());
        toTransactionModel.setFromFirstName(fromUserModel.getFirstName());
        toTransactionModel.setFromLastName(fromUserModel.getLastName());
        toTransactionModel.setToCustomerID(toUserModel.getEmail());
        toTransactionModel.setToFirstName(toUserModel.getFirstName());
        toTransactionModel.setToLastName(toUserModel.getLastName());
        toTransactionModel.setTransactionDate(timeStamp);
        toTransactionModel.setCredits(amount);
        Map<String, Object> update = new HashMap<>();
        toUserModel.setCredits(toUserModel.getCredits() + amount);
        toUserModel.setReceived(toUserModel.getReceived() + amount);
        toUserModel.getTransactions().add(toTransactionModel);
        fromUserModel.setCredits(fromUserModel.getCredits() - amount);
        fromUserModel.setSent(fromUserModel.getSent() + amount);
        fromUserModel.getTransactions().add(fromTransactionModel);
        update.put(AeSimpleSHA1.SHA1(toCustomerId), toUserModel);
        update.put(AeSimpleSHA1.SHA1(SharedPreferenceManager.getInstance(this).getString(Constants.EMAIL)), fromUserModel);
        usersRef.updateChildren(update).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    userManager.creditsSent();
                    showSuccessDialog();
                } else {
                    btnSendCredits.setEnabled(true);
                }
            }
        });
    }

    /*
       Method to show success dialog
    */
    private void showSuccessDialog() {
        SharedPreferenceManager.getInstance(SendCreditsActivity.this).
                saveData(Constants.LAST_TRANS_TIME, new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date()));
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater inflater = (LayoutInflater) this.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.custom_dialog, null);
        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
        TextView success = (TextView) view.findViewById(R.id.success);
        success.setTypeface(iconFont);
        Button ok = (Button) view.findViewById(R.id.btnOk);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                finish();
            }
        });
        dialog.setContentView(view);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

}
