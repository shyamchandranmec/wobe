package com.example.admin.wobeassignment.utilities;

/**
 * Created by Admin on 19-09-2017.
 */

public class Constants {

    public static final String USERNAME = "username";
    public static final String FIRST_NAME = "firstname";
    public static final String LAST_NAME = "lastname";
    public static final String EMAIL = "email";
    public static final String CUSTOMER_ID = "customerId";
    public static final String CREDITS = "credits";
    public static final String LAST_TRANS_TIME = "lastTransactionTime";
    public static final String TRANS_LIST = "transactionList";
    public static final String PASSCODE = "passcode";


    public static final String KEY_PASSCODE_ACTIVITY_BUNDLE = "PasscodeActivity";

    public static final String VALUE_SPLASH_SCREEN_ACTIVITY = "1";
    public static final String VALUE_LOGIN_ACTIVITY = "2";
    public static final String VALUE_REGISTER_ACTIVITY = "3";
    public static final String VALUE_FOR_GOOGLE_SIGN = "4";


    private static String BASE_URL = "https://www.axisshared.com:8443/MainPage?AXIS_API_KEY=i50c988cb_8895_44e7_ad6c_a74189f202a6&AXIS_API=";

    public static String Register_URL = BASE_URL + "WobeCustomerAdd&" +
            "firstName=%s" + "&lastName=%s" + "&emailAddress=%s" + "&userPassword=%s" + "&TOKEN_ID=%s";

    public static String LOGIN_URL = BASE_URL + "WobeSignIn&" + "emailAddress=%s" + "&userPassword=%s";

    public static String SOCIAL_LOGIN_URL = BASE_URL + "WobeCustomerAddSocial&" +
            "firstName=%s" + "&lastName=%s" + "&emailAddress=%s" + "&TOKEN_ID=%s";

    public static String DASHBOARD_URL = BASE_URL + "WobeDashboardDetails&" + "customerID=%s";

    public static String VERIFY_USER_URL = BASE_URL + "WobeVerifyCustomer&" + "email_address=%s";

    public static String SEND_CREDITS_URL = BASE_URL + "WobeSendCredits&" + "fromCustomerID=%s" +
            "&fromFirstName=%s" + "&fromLastName=%s" + "&toCustomerID=%s" + "&toFirstName=%s" +
            "&toLastName=%s" + "&credits=%s" + "&description=%s" + "&eToSelf=%s";
}
