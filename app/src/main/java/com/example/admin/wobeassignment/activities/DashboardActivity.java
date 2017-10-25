package com.example.admin.wobeassignment.activities;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.admin.wobeassignment.ApplicationLoader;
import com.example.admin.wobeassignment.R;
import com.example.admin.wobeassignment.adapters.TransactionAdapter;
import com.example.admin.wobeassignment.model.DashboardModel;
import com.example.admin.wobeassignment.model.TransactionModel;
import com.example.admin.wobeassignment.model.UserModel;
import com.example.admin.wobeassignment.utilities.AeSimpleSHA1;
import com.example.admin.wobeassignment.utilities.CommonUtils;
import com.example.admin.wobeassignment.utilities.Constants;
import com.example.admin.wobeassignment.utilities.FontManager;
import com.example.admin.wobeassignment.utilities.SharedPreferenceManager;
import com.example.admin.wobeassignment.utilities.WobeAlarm;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private RecyclerView recyclerView;
    private TextView tvAdded, tvSent, tvReceived;
    private TransactionAdapter adapter;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialiseViews();
        if (CommonUtils.isConnectingToInternet(DashboardActivity.this)) {
            makeApiCall(SharedPreferenceManager.getInstance(this).getString(Constants.EMAIL));
        } else {
            Toast.makeText(this, getResources().getString(R.string.check_internet_connection), Toast.LENGTH_SHORT).show();
        }
        scheduleNotification(getNotification("5 second delay"), 5000);
        initialiseNavigationDrawer();
    }
    private Notification getNotification(String content) {
        Notification.Builder builder = new Notification.Builder(this);
        System.out.println("again notification " + content);
        builder.setContentTitle("You can send credits now");
        builder.setContentText(content);
        builder.setSmallIcon(R.drawable.facebook_sign_in_button);
        return builder.build();
    }
    private void scheduleNotification(Notification notification, int delay) {

        Intent notificationIntent = new Intent(this, WobeAlarm.class);
        notificationIntent.putExtra(WobeAlarm.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(WobeAlarm.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC,  System.currentTimeMillis(), delay, pendingIntent);
    }

    //method to initialise the navigation drawer(hamburger menu)
    private void initialiseNavigationDrawer() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        setEmailAndNameInNavHeader(navigationView, drawer);
    }

    //method to set details in the navigation drawer(hamburger menu)
    private void setEmailAndNameInNavHeader(NavigationView nameInNavHeader, final DrawerLayout drawer) {
        View header = nameInNavHeader.getHeaderView(0);

        TextView tvUsername = (TextView) header.findViewById(R.id.tvUsername);
        TextView tvEmail = (TextView) header.findViewById(R.id.tvEmail);

        //Retrieving name and email from Shared Preference to show in menu
        if (SharedPreferenceManager.getInstance(this).getString(Constants.FIRST_NAME) != null) {
            tvUsername.setText(SharedPreferenceManager.getInstance(this).getString(Constants.FIRST_NAME));
        }
        if (SharedPreferenceManager.getInstance(this).getString(Constants.EMAIL) != null) {
            tvEmail.setText(SharedPreferenceManager.getInstance(this).getString(Constants.EMAIL));
        }
    }


    private void makeApiCall(String email) {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference();
        DatabaseReference usersRef = ref.child("users");
        usersRef.child(AeSimpleSHA1.SHA1(email)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                TextView tvName = (TextView) findViewById(R.id.tvName);
                TextView tvBalance = (TextView) findViewById(R.id.tvBalance);
                UserModel userModel = snapshot.getValue(UserModel.class);

                tvName.setText(userModel.getFirstName().trim());
                SharedPreferenceManager.getInstance(DashboardActivity.this).
                        saveData(Constants.FIRST_NAME, userModel.getFirstName());
                SharedPreferenceManager.getInstance(DashboardActivity.this).
                        saveData(Constants.LAST_NAME, userModel.getLastName());
                tvBalance.setText(getResources().getString(R.string.balance) + userModel.getCredits());
                tvAdded.setText(Float.toString(userModel.getAdded()));
                tvSent.setText(Float.toString(userModel.getSent()));
                tvReceived.setText(Float.toString(userModel.getReceived()));
                SharedPreferenceManager.getInstance(DashboardActivity.this).
                        saveData(Constants.CREDITS, Float.toString(userModel.getCredits()));
                adapter.setDataInAdapter(userModel.getTransactions());
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println("error");
            }
        });

    }

    private void initialiseViews() {
        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
        TextView iconAdded = (TextView) findViewById(R.id.iconAdded);
        iconAdded.setTypeface(iconFont);
        TextView iconSent = (TextView) findViewById(R.id.iconSent);
        iconSent.setTypeface(iconFont);
        TextView iconReceived = (TextView) findViewById(R.id.iconReceived);
        iconReceived.setTypeface(iconFont);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Button btnSendCredits = (Button) findViewById(R.id.btnSendCredits);
        btnSendCredits.setOnClickListener(this);
        recyclerView = (RecyclerView)
                findViewById(R.id.rvRecentTransactions);
        adapter = new TransactionAdapter(this);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        tvAdded = (TextView) findViewById(R.id.tvAdded);
        tvSent = (TextView) findViewById(R.id.tvSent);
        tvReceived = (TextView) findViewById(R.id.tvReceived);
    }

    /*
      Method to handle back press
    */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    /*
      Method to handle click events for items in the navigation drawer
    */
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_send_credits) {
            goToNextActivity(SendCreditsActivity.class);
        } else if (id == R.id.nav_profile) {
            goToNextActivity(ProfileActivity.class);
        } else if (id == R.id.nav_logout) {
            showLogoutPopUp();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /*
      Method to show the logout pop-up
    */
    private void showLogoutPopUp() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater inflater = (LayoutInflater) this.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.delete_custom_dialog, null);
        Button yes = (Button) view.findViewById(R.id.btnDeleteYes);
        Button no = (Button) view.findViewById(R.id.btnDeleteNo);
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferenceManager.getInstance(getApplicationContext()).clearData();
                FirebaseAuth.getInstance().signOut();
                finish();
                goToNextActivity(OnBoardingViewPagerActivity.class);
                dialog.dismiss();
            }
        });
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.setContentView(view);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

    }

    /*
       Method to handle click listeners of views
    */
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.btnSendCredits:
                goToNextActivity(SendCreditsActivity.class);
                break;
        }
    }

    /*
      Method to go to the next activity
    */
    protected void goToNextActivity(Class nextActivity) {
        Intent intent = new Intent();
        intent.setClass(this, nextActivity);
        startActivity(intent);
    }


    /*
       Dashboard API call done
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (CommonUtils.isConnectingToInternet(DashboardActivity.this)) {
            makeApiCall(SharedPreferenceManager.getInstance(this).getString(Constants.EMAIL));
        } else {
            Toast.makeText(this, getResources().getString(R.string.check_internet_connection), Toast.LENGTH_SHORT).show();
        }
    }

    /*
       When activity is the foreground, brought back from background, Passcode screen is shown again.
       This can be achieved through this lifecycle method of the activity
    */
    @Override
    protected void onStart() {
        super.onStart();
        if (ApplicationLoader.wasInBackground) {
            Intent intent = new Intent();
            intent.setClass(this, PasscodeActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(Constants.KEY_PASSCODE_ACTIVITY_BUNDLE, Constants.VALUE_SPLASH_SCREEN_ACTIVITY);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
            ApplicationLoader.wasInBackground = false;
        }
    }

}
