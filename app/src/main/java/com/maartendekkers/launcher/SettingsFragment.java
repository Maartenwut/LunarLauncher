package com.maartendekkers.launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v14.preference.MultiSelectListPreference;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.View;
import android.widget.Switch;

import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


public class SettingsFragment extends PreferenceFragmentCompat {
    private Keystore store;//Holds our key pairs

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);
        store = Keystore.getInstance(getActivity());//Creates or Gets our key pairs.  You MUST have access to current context!
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().registerReceiver(updateHiddenAppsPreference, new IntentFilter("hiddenApps"));
        view.setBackgroundColor(getResources().getColor(android.R.color.background_light));

        new addApps().execute();

        Preference backgroundColor = findPreference("backgroundColor");
        backgroundColor.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                int color = getResources().getColor(R.color.defaultBackgroundColor);
                if (store.getInt("backgroundColor") != 1) {
                    color = store.getInt("backgroundColor");
                }
                ColorPickerDialog colorpicker = ColorPickerDialog.newBuilder()
                        .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                        .setAllowPresets(false)
                        .setDialogId(1337)
                        .setColor(color)
                        .setShowAlphaSlider(true)
                        .setShowColorShades(true)
                        .setDialogTitle(R.string.colorPickerDialog_title)
                        .create();
                colorpicker.setColorPickerDialogListener(new ColorPickerDialogListener() {
                    @Override public void onColorSelected(int dialogId, int color) {
                        store.putInt("backgroundColor",color);
                        Intent intent = new Intent("background");
                        getActivity().sendBroadcast(intent);
                    }

                    @Override public void onDialogDismissed(int dialogId) {

                    }
                });
                colorpicker.show(getActivity().getFragmentManager(), "ColorPicker");

                return true;
            }
        });

        Preference textColor = findPreference("textColor");
        textColor.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                int color = 0xffffffff;
                if (store.getInt("textColor") != 1) {
                    color = store.getInt("textColor");
                }
                ColorPickerDialog colorpicker = ColorPickerDialog.newBuilder()
                        .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                        .setAllowPresets(false)
                        .setDialogId(1338)
                        .setColor(color)
                        .setShowAlphaSlider(true)
                        .setShowColorShades(true)
                        .setDialogTitle(R.string.colorPickerDialog_title)
                        .create();
                colorpicker.setColorPickerDialogListener(new ColorPickerDialogListener() {
                    @Override public void onColorSelected(int dialogId, int color) {
                        store.putInt("textColor",color);
                        Intent intent = new Intent("allApps");
                        getActivity().sendBroadcast(intent);
                        intent = new Intent("favorites");
                        getActivity().sendBroadcast(intent);
                    }

                    @Override public void onDialogDismissed(int dialogId) {

                    }
                });
                colorpicker.show(getActivity().getFragmentManager(), "ColorPicker");

                return true;
            }
        });

        Preference setWallpaper = findPreference("setWallpaper");
        setWallpaper.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Intent.ACTION_SET_WALLPAPER);
                startActivity(intent);
                return true;
            }
        });

        final ListPreference iconOptions = (ListPreference) findPreference("iconOptions");
        iconOptions.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                Intent intent = new Intent("allApps");
                getActivity().sendBroadcast(intent);
                intent = new Intent("favorites");
                getActivity().sendBroadcast(intent);
                return true;
            }
        });

        final SwitchPreference fastScrolling = (SwitchPreference) findPreference("fastScrolling");
        fastScrolling.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                Intent intent = new Intent("allApps");
                getActivity().sendBroadcast(intent);
                return true;
            }
        });

        final ListPreference favoriteAppsOrder = (ListPreference) findPreference("favoriteAppsOrder");
        favoriteAppsOrder.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                Intent intent = new Intent("favorites");
                getActivity().sendBroadcast(intent);
                return true;
            }
        });
    }

    public static SettingsFragment newInstance() {

        return new SettingsFragment();
    }

    public class addApps extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... Params) {
            MultiSelectListPreference hideApps = (MultiSelectListPreference) findPreference("hideApps");
            hideApps.setDialogTitle(R.string.hideAppsDialog_title);
            Intent d = new Intent(Intent.ACTION_MAIN, null);
            d.addCategory(Intent.CATEGORY_LAUNCHER);
            d.removeCategory(Intent.CATEGORY_HOME);

            PackageManager pm = getActivity().getPackageManager();

            List<String> entries = new ArrayList<String>();
            List<String> entryValues = new ArrayList<String>();

            Set<String> hiddenApps = store.getStringSet("hideApps");
            List<ResolveInfo> packs = pm.queryIntentActivities(d, 0);
            Collections.sort(packs, new ResolveInfo.DisplayNameComparator(pm));
            for (int i = 0; i < packs.size(); i++) {
                ResolveInfo p = packs.get(i);
                if (p.activityInfo.enabled && !p.activityInfo.packageName.equals(getActivity().getApplicationContext().getPackageName())) {
                    entries.add(p.activityInfo.loadLabel(pm).toString());
                    entryValues.add(p.activityInfo.packageName);
                }
            }

            hideApps.setEntries(entries.toArray(new CharSequence[entries.size()]));
            hideApps.setEntryValues(entryValues.toArray(new CharSequence[entryValues.size()]));
            hideApps.setValues(hiddenApps);
            hideApps.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {

                    Intent intent = new Intent("allApps");
                    getActivity().sendBroadcast(intent);
                    return true;
                }
            });
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
        }

    }
    private BroadcastReceiver updateHiddenAppsPreference = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            new addApps().execute();
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(updateHiddenAppsPreference);
    }

}
