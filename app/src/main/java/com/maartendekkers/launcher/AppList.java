package com.maartendekkers.launcher;

import android.graphics.drawable.Drawable;

public class AppList {

    private String name;
    private String pkg;
    Drawable icon;

    public AppList(String name, Drawable icon, String pkg) {
        this.name = name;
        this.icon = icon;
        this.pkg = pkg;
    }

    public String getName() {
        return name;
    }
    public String getPackage() { return pkg; }
    public Drawable getIcon() {
        return icon;
    }
}
