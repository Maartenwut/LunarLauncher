package com.maartendekkers.launcher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class AppAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private List<AppList> listStorage;
    private Context mContext; //instance variable
    private FragmentActivity mActivity; //instance variable
    private Keystore store;//Holds our key pairs
    private Boolean showIcons;

    public AppAdapter(FragmentActivity activity, Context context, List<AppList> customizedListView, String fragment) {

        store = Keystore.getInstance(context);//Creates or Gets our key pairs.  You MUST have access to current context!
        layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        listStorage = customizedListView;
        this.mContext = context;
        this.mActivity = activity;
        if(store.getString("iconOptions", "d").equals(fragment) || store.getString("iconOptions", "d").equals("showIcons")) {
            showIcons = true;
        } else {
            showIcons = false;
        }
    }

    @Override
    public int getCount() {
        return listStorage.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder listViewHolder;
        if(convertView == null) {
            listViewHolder = new ViewHolder();
            if(showIcons == false) {
                convertView = layoutInflater.inflate(R.layout.app_list_noicon, parent, false);
            } else {
                convertView = layoutInflater.inflate(R.layout.app_list, parent, false);
                listViewHolder.imageInListView = convertView.findViewById(R.id.app_icon);
            }

            listViewHolder.textInListView = convertView.findViewById(R.id.list_app_name);
            convertView.setTag(listViewHolder);
        } else {
            listViewHolder = (ViewHolder)convertView.getTag();
        }
        convertView.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent i = v.getContext().getPackageManager().getLaunchIntentForPackage(listStorage.get(position).getPackage());
                if (null != i) {
                    v.getContext().startActivity(i);
                    mActivity.overridePendingTransition(R.anim.task_enter, R.anim.fade_out);
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ViewPagerCustomDuration pager = mActivity.findViewById(R.id.viewPager);
                            pager.setScrollDurationFactor(0.1);
                            pager.setCurrentItem(1);
                            pager.setScrollDurationFactor(1);
                        }
                    }, 500);
                } else {
                    Toast.makeText(v.getContext(), "Couldn't launch app", Toast.LENGTH_LONG).show();
                }


            }
        });
        convertView.setLongClickable(true);
        listViewHolder.textInListView.setText(listStorage.get(position).getName());
        if (store.getInt("textColor") != 1) {
            listViewHolder.textInListView.setTextColor(store.getInt("textColor"));
        }
        if(showIcons == true) {
            listViewHolder.imageInListView.setImageDrawable(listStorage.get(position).getIcon());
        }


        return convertView;
    }

    static class ViewHolder{

        TextView textInListView;
        ImageView imageInListView;
    }
}
