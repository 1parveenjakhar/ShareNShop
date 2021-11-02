package com.puteffort.sharenshop.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

public class StaticData {

    public static void changeLocale(String language, Context context) {
        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(new Locale(language));
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }
}
