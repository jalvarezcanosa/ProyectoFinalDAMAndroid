package com.example.contapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "ContadoresAppPrefs";
    private static final String KEY_USER_TOKEN = "user_token";

    private SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveAuthToken(String token) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USER_TOKEN, token);
        editor.apply();
    }

    public String fetchAuthToken() {
        return prefs.getString(KEY_USER_TOKEN, null);
    }

    public boolean isLoggedIn() {
        return fetchAuthToken() != null;
    }

    public void clearSession() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_USER_TOKEN);
        editor.apply();
    }
}
