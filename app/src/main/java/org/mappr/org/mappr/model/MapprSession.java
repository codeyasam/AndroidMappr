package org.mappr.org.mappr.model;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by codeyasam on 6/8/16.
 */
public class MapprSession {

    public static boolean isLoggedIn = false;

    public static final String REGISTERORIGIN = "registerFrom";
    public static final String RATEREGISTER = "rateregister101";
    public static final String LOGGED_USER_ID = "logged_user_id";

    public static void logUser(Activity activity, String userId) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(LOGGED_USER_ID, userId);
        editor.commit();
        isLoggedIn = true;
    }


}
