package com.example.admin.wobeassignment.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.admin.wobeassignment.R;
import com.example.admin.wobeassignment.utilities.Constants;
import com.example.admin.wobeassignment.utilities.SharedPreferenceManager;

/**
 * Created by Admin on 19-09-2017.
 */

public class SplashScreenActivity extends AppCompatActivity {
    Thread splashTread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        splashTread = new Thread() {
            @Override
            public void run() {
                try {
                    int waited = 0;
                    // Splash screen pause time
                    while (waited < 1500) {
                        sleep(100);
                        waited += 100;
                    }
                    goToNextActivity();
                } catch (InterruptedException e) {
                    // do nothing
                } finally {
                    SplashScreenActivity.this.finish();
                }

            }
        };
        splashTread.start();
    }

    /*
      Method to go to next activity.
      If customer has Registered/Logged in, Dashboard activity is shown
      else, Onboarding activity is shown.
     */
    protected void goToNextActivity() {
        boolean isFirstTime = SharedPreferenceManager.getInstance(this).isFirstTimeLaunch();
        Intent intent = new Intent();
        if (!isFirstTime) {
            intent.setClass(this, OnBoardingViewPagerActivity.class);
        } else {
            intent.setClass(this, PasscodeActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(Constants.KEY_PASSCODE_ACTIVITY_BUNDLE, Constants.VALUE_SPLASH_SCREEN_ACTIVITY);
            intent.putExtras(bundle);
        }
        startActivity(intent);
        finish();
    }

}