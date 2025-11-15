package com.example.flightapp.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {
    private static final String PREF = "flight_pref";
    private static final String KEY_TOKEN = "token";
    private SharedPreferences sp;

    public PrefManager(Context ctx) {
        sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        sp.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return sp.getString(KEY_TOKEN, null);
    }

    public void clear() {
        sp.edit().clear().apply();
    }
}
