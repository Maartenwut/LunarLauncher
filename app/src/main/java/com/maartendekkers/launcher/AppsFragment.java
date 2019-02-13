package com.maartendekkers.launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;


public class AppsFragment extends Fragment {
    private Keystore store;//Holds our key pairs
    private List<AppList> installedApps;
    private View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.apps_fragment, container, false);

        store = Keystore.getInstance(v.getContext());//Creates or Gets our key pairs.  You MUST have access to current context!
        new addApps().execute();
        getActivity().registerReceiver(updateAllApps, new IntentFilter("allApps"));

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addDataScheme("package");
        getActivity().registerReceiver(updateBoth, intentFilter);
        return v;
    }
    public static AppsFragment newInstance() {
        return new AppsFragment();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.menu_list, menu);
    }
    @Override
    public boolean onContextItemSelected(MenuItem item){
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int number = info.position;
        Uri packageUri;
        switch (item.getItemId()){
            case R.id.add_to_favourites:
                Collection favoriteApps = store.loadOrderedCollection("favoriteApps");
                if (!favoriteApps.contains(installedApps.get(number).getPackage())) {

                    favoriteApps.add(installedApps.get(number).getPackage());
                    store.saveOrderedCollection(favoriteApps, "favoriteApps");

                    Intent intent = new Intent("favorites");
                    getActivity().sendBroadcast(intent);
                }
                return true;
            case R.id.delete_app:
                packageUri = Uri.parse("package:" + installedApps.get(number).getPackage());
                Intent uninstallIntent =
                        new Intent(Intent.ACTION_DELETE, packageUri);
                startActivity(uninstallIntent);
                return true;
            case R.id.app_info:
                packageUri = Uri.parse("package:" + installedApps.get(number).getPackage());
                Intent infoIntent =
                        new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageUri);
                startActivity(infoIntent);
                return true;
            case R.id.hide_app:
                Set<String> hiddenApps = store.getStringSet("hideApps");
                hiddenApps.add(installedApps.get(number).getPackage());
                store.putStringSet("hideApps", hiddenApps);
                new addApps().execute();

                Intent intent = new Intent("hiddenApps");
                getActivity().sendBroadcast(intent);
                return true;
            default:
                return super.onContextItemSelected(item);

        }
    }
    public class addApps extends AsyncTask<Void, Void, List<AppList>> {

        @Override
        protected List<AppList> doInBackground(Void... Params) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView loadingText = v.findViewById(R.id.loadingAppsText);
                    loadingText.setVisibility(View.VISIBLE);
                }
            });

            Set<String> set = store.getStringSet("hideApps");
            List<String> hiddenApps = new ArrayList<String>(set);

            Intent d = new Intent(Intent.ACTION_MAIN, null);
            d.addCategory(Intent.CATEGORY_LAUNCHER);
            d.removeCategory(Intent.CATEGORY_HOME);

            PackageManager pm = getActivity().getPackageManager();
            List<AppList> res = new ArrayList<AppList>();

            List<ResolveInfo> packs = pm.queryIntentActivities(d, 0);
            Collections.sort(packs, new ResolveInfo.DisplayNameComparator(pm));
            for (int i = 0; i < packs.size(); i++) {
                ResolveInfo p = packs.get(i);
                if (    p.activityInfo.enabled && !hiddenApps.contains(p.activityInfo.packageName) &&
                        !p.activityInfo.packageName.equals(getActivity().getApplicationContext().getPackageName())) {
                    String appName = p.activityInfo.loadLabel(pm).toString();
                    String appPkg = p.activityInfo.packageName;
                    Drawable icon = p.activityInfo.loadIcon(pm);
                    res.add(new AppList(appName, icon, appPkg));
                }
            }
            return res;
        }

        @Override
        protected void onPostExecute(List<AppList> result) {
            super.onPostExecute(result);
            ListView userInstalledApps = v.findViewById(R.id.appListview);

            boolean value = store.getBoolean("fastScrolling");
            userInstalledApps.setFastScrollEnabled(value);
            userInstalledApps.setFastScrollAlwaysVisible(value);

            installedApps = result;
            AppAdapter installedAppAdapter = new AppAdapter(getActivity(), v.getContext(), installedApps, "drawerIcons");
            userInstalledApps.setAdapter(installedAppAdapter);
            registerForContextMenu(userInstalledApps);

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TextView loadingText = v.findViewById(R.id.loadingAppsText);
                    loadingText.setVisibility(View.INVISIBLE);
                }
            });
        }

    }
    private BroadcastReceiver updateAllApps = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            new addApps().execute();
        }
    };
    private BroadcastReceiver updateBoth = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            intent = new Intent("allApps");
            getActivity().sendBroadcast(intent);
            intent = new Intent("favorites");
            getActivity().sendBroadcast(intent);
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(updateAllApps);
        getActivity().unregisterReceiver(updateBoth);
    }

}

