package com.example.aquasys.system;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class SharedPreferencesHelper {

    public static <T> List<T> getListFromSharedPreferences(Context context, String key, TypeToken<List<T>> typeToken) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String json = sharedPreferences.getString(key, "");

        if (!json.isEmpty()) {
            return new Gson().fromJson(json, typeToken.getType());
        }

        return new ArrayList<>(); // Return an empty list if no data is found
    }

    public static <T> void saveListToSharedPreferences(Context context, List<T> dataList, String key) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = new Gson().toJson(dataList);
        editor.putString(key, json);
        editor.apply();
    }
}
