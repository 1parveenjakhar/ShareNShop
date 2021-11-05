package com.puteffort.sharenshop.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.DisplayMetrics;

import com.puteffort.sharenshop.R;

import java.util.Locale;

public class StaticData {
    private static Bitmap defaultImageBitmap;

    public static Bitmap getDefaultImageBitmap(Context context) {
        if (defaultImageBitmap == null) {
            defaultImageBitmap = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.default_user_image);
        }
        return defaultImageBitmap;
    }

    public static void changeLanguage(String language, Context context) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.setLocale(locale);
        context.getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());

//        Resources resources = context.getResources();
//        Configuration configuration = resources.getConfiguration();
//        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
//        configuration.setLocale(locale);
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N){
//            context.createConfigurationContext(configuration);
//        } else {
//            resources.updateConfiguration(configuration,displayMetrics);
//        }
    }

    public static Locale getCurrentLocale(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.getResources().getConfiguration().getLocales().get(0);
        } else {
            //noinspection deprecation
            return context.getResources().getConfiguration().locale;
        }
    }
}
