package com.maartendekkers.launcher;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.widget.LinearLayout;

public class MainActivity extends AppCompatActivity {
    private Keystore store;//Holds our key pairs
    private boolean settingsPage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        store = Keystore.getInstance(this);//Creates or Gets our key pairs.  You MUST have access to current context!

        setContentView(R.layout.activity_main);

        ViewPagerCustomDuration pager = findViewById(R.id.viewPager);
        pager.setAdapter(new PagerAdapter(getSupportFragmentManager()));
        pager.setCurrentItem(1);
        pager.setOffscreenPageLimit(2);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {}
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            public void onPageSelected(int position) {
                if (position == 0 && !settingsPage) {
                    animateColorChange(getColor(), 0xff000000);
                    settingsPage = true;
                } else if (settingsPage) {
                    animateColorChange(0xff000000, getColor());
                    settingsPage = false;
                }
            }
        });

        setBackground();
        registerReceiver(setBackground, new IntentFilter("background"));

    }

    public class PagerAdapter extends FragmentPagerAdapter {

        public PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int pos) {
            switch(pos) {

                case 0: return SettingsFragment.newInstance();
                case 1: return HomeFragment.newInstance();
                case 2: return AppsFragment.newInstance();
                default: return HomeFragment.newInstance();
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (Intent.ACTION_MAIN.equals(intent.getAction())) {
            ViewPagerCustomDuration pager = findViewById(R.id.viewPager);
            pager.setScrollDurationFactor(2);
            pager.setCurrentItem(1);
            pager.setScrollDurationFactor(1);
        }
    }

    @Override
    public void onResume() {
        overridePendingTransition(R.anim.fade_in,R.anim.task_exit);
        super.onResume();
    }

    public void setBackground() {
        int color = getColor();
        LinearLayout overlay = findViewById(R.id.mainLayoutOverlay);
        overlay.setBackgroundColor(color);

        if (!settingsPage) {
        setSystemBarColors(color);
        }
    }
    @Override
    public void onBackPressed(){
        // do something here and don't write super.onBackPressed()
    }
    private BroadcastReceiver setBackground = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setBackground();
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(setBackground);
    }
    public void setSystemBarColors(int color) {
        ViewPagerCustomDuration pager = findViewById(R.id.viewPager);
            Window window = getWindow();
            window.setNavigationBarColor(color);
            window.setStatusBarColor(color);
    }
    public int getColor() {
        int color;
        color = getResources().getColor(R.color.defaultBackgroundColor);
        if (store.getInt("backgroundColor") != 1) {
            color = store.getInt("backgroundColor");
        }
        return color;
    }

    public void animateColorChange(int colorFrom, int colorTo) {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(250); // milliseconds
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                setSystemBarColors((int) animator.getAnimatedValue());
            }

        });
        colorAnimation.start();
    }
}
