package com.maartendekkers.launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class HomeFragment extends Fragment {
    private Keystore store;//Holds our key pairs
    private List<AppList> favoriteAppsList;
    private View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.home_fragment, container, false);

        store = Keystore.getInstance(v.getContext());//Creates or Gets our key pairs.  You MUST have access to current context!
        new addFavorites().execute();
        getActivity().registerReceiver(updateFavoriteApps, new IntentFilter("favorites"));

        return v;
    }
    public static HomeFragment newInstance() {

        return new HomeFragment();
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.favorite_menu, menu);
    }
    @Override
    public boolean onContextItemSelected(MenuItem item){
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int number = info.position;
        switch (item.getItemId()){
            case R.id.remove_from_favorites:

                Collection favoriteApps = store.loadOrderedCollection("favoriteApps");
                favoriteApps.remove(favoriteAppsList.get(number).getPackage());
                store.saveOrderedCollection(favoriteApps, "favoriteApps");

                new addFavorites().execute();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public class addFavorites extends AsyncTask<Void, Void, List<AppList>> {

        @Override
        protected List<AppList> doInBackground(Void... Params) {

            Collection favorites = store.loadOrderedCollection("favoriteApps");
            List<String> favoriteApps = new ArrayList<String>(favorites);

            String order = store.getString("favoriteAppsOrder", "default");

            Intent d = new Intent(Intent.ACTION_MAIN, null);
            d.addCategory(Intent.CATEGORY_LAUNCHER);
            d.removeCategory(Intent.CATEGORY_HOME);

            PackageManager pm = getActivity().getPackageManager();
            List<AppList> res = new ArrayList<AppList>();

            /*************/
            /** Sorting **/
            /*************/

            if (order.equals("defaultReversed")) {
                Collections.reverse(favoriteApps);
            } else if (order.equals("alphabetical") || order.equals("alphabeticalReversed")) {
                List<String> appsNames = new ArrayList<String>();

                for (int i = 0; i < favoriteApps.size(); i++) {
                    ApplicationInfo p;
                    try {
                        p = getActivity().getPackageManager().getApplicationInfo(favoriteApps.get(i), 0);
                        String appName = pm.getApplicationLabel(p).toString();
                        appsNames.add(i, appName);
                    } catch (PackageManager.NameNotFoundException e) {
                    }
                }

                List<String> sortedAppsNames = new ArrayList<String>(appsNames);
                List<String> sortedApps = new ArrayList<String>();
                Collections.sort(sortedAppsNames);

                for (int i = 0; i < sortedAppsNames.size(); i++) {
                    sortedApps.add(i, favoriteApps.get(appsNames.indexOf(sortedAppsNames.get(i))));
                }
                if (order.equals("alphabeticalReversed")) {
                    Collections.reverse(sortedApps);
                }
                favoriteApps = sortedApps;
            }

            for (int i = 0; i < favoriteApps.size(); i++) {
                try {
                    ApplicationInfo p = getActivity().getPackageManager().getApplicationInfo(favoriteApps.get(i), 0);
                    String appName = pm.getApplicationLabel(p).toString();
                    String appPkg = favoriteApps.get(i);

                    Drawable icon = pm.getApplicationIcon(p);;
                    res.add(new AppList(appName, icon, appPkg));
                } catch (PackageManager.NameNotFoundException e) {
                }
            }
            return res;
        }

        @Override
        protected void onPostExecute(List<AppList> result) {
            super.onPostExecute(result);
            ListView favoriteApps = v.findViewById(R.id.homeListview);
            TextView noFavorites = v.findViewById(R.id.textViewNoFavorites);
            if (result.isEmpty()) {
                noFavorites.setVisibility(View.VISIBLE);
                if (store.getInt("textColor") != 1) {
                    noFavorites.setTextColor(store.getInt("textColor"));
                }
                favoriteApps.setVisibility(View.INVISIBLE);
            } else {
                noFavorites.setVisibility(View.INVISIBLE);
                favoriteApps.setVisibility(View.VISIBLE);
                favoriteAppsList = result;
                AppAdapter favoriteAppAdapter = new AppAdapter(getActivity(), v.getContext(), result, "favoriteIcons");
                favoriteApps.setAdapter(favoriteAppAdapter);
                registerForContextMenu(favoriteApps);
            }
        }

    }
    private BroadcastReceiver updateFavoriteApps = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            new addFavorites().execute();
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(updateFavoriteApps);
    }
}
