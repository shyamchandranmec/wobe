package com.example.admin.wobeassignment.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Admin on 19-09-2017.
 */

public class CommonUtils {

    /**
     * Check internet is connected or not
     *
     * @param context activity context
     * @return boolean
     */
    public static boolean isConnectingToInternet(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

}
