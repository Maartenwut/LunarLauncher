package com.maartendekkers.launcher;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Keystore {
    private static Keystore store;
    private SharedPreferences SP;

    private Keystore(Context context) {
        SP = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static Keystore getInstance(Context context) {
        if (store == null) {
            Log.v("Keystore","NEW STORE");
            store = new Keystore(context);
        }
        return store;
    }

    public void put(String key, String value) {//Log.v("Keystore","PUT "+key+" "+value);
        Editor editor = SP.edit();
        editor.putString(key, value);
        editor.commit(); // Stop everything and do an immediate save!
        // editor.apply();//Keep going and save when you are not busy - Available only in APIs 9 and above.  This is the preferred way of saving.
    }

    public String get(String key) {//Log.v("Keystore","GET from "+key);
        return SP.getString(key, null);

    }

    public String getString(String key, String def) {//Log.v("Keystore","GET from "+key);
        return SP.getString(key, def);

    }

    public int getInt(String key) {//Log.v("Keystore","GET INT from "+key);
        return SP.getInt(key, 1);
    }
    public void putInt(String key, int num) {//Log.v("Keystore","PUT INT "+key+" "+String.valueOf(num));
        Editor editor = SP.edit();

        editor.putInt(key, num);
        editor.commit();
    }

    public Set<String> getStringSet(String key) {//Log.v("Keystore","GET INT from "+key);
        Set<String> s = new HashSet<String>(SP.getStringSet(key, new HashSet<String>()));
        return s;
    }
    public void putStringSet(String key, Set<String> set) {//Log.v("Keystore","PUT INT "+key+" "+String.valueOf(num));
        Editor editor = SP.edit();

        editor.putStringSet(key, set);
        editor.commit();
    }

    public void saveOrderedCollection(Collection collection, String key){
        JSONArray jsonArray = new JSONArray(collection);
        SharedPreferences.Editor editor = SP.edit();
        editor.putString(key, jsonArray.toString());
        editor.commit();
    }

    public Collection loadOrderedCollection(String key){
        ArrayList arrayList = new ArrayList();
        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(this.getString(key, "[]"));
            for (int i = 0; i < jsonArray.length(); i++) {
                arrayList.add(jsonArray.get(i));
            }
            return arrayList;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return arrayList;
    }

    public boolean getBoolean(String key) {//Log.v("Keystore","GET INT from "+key);
        return SP.getBoolean(key, false);
    }
    public void putBoolean(String key, boolean num) {
        Editor editor = SP.edit();

        editor.putBoolean(key, num);
        editor.commit();
    }


    public void clear(){ // Delete all shared preferences
        Editor editor = SP.edit();

        editor.clear();
        editor.commit();
    }

}