package com.puteffort.sharenshop.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;

import com.puteffort.sharenshop.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class StaticData {
    private static String defaultImageString;
    private static Bitmap defaultImageBitmap;

    private static Bitmap getDefaultImageBitmap(Context context) {
        if (defaultImageBitmap == null) {
            defaultImageBitmap = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.default_user_image);
            AsyncTask.execute(() -> defaultImageString = getStringFromBitmap(defaultImageBitmap));
        }
        return defaultImageBitmap;
    }

    public static String getDefaultImageString() {
        return defaultImageString;
    }

    public static void changeLocale(String language, Context context) {
        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(new Locale(language));
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    public static Bitmap getBitmapFromURL(String url, Context context) {
        if (url == null || url.isEmpty() || url.equalsIgnoreCase("null")) {
            return getDefaultImageBitmap(context);
        }
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            return getDefaultImageBitmap(context);
        }
    }

    public static Bitmap getBitmapFromString(String src, Context context) {
        if (src == null || src.isEmpty() || src.equalsIgnoreCase("null")) {
            return getDefaultImageBitmap(context);
        }
        byte[] decodedString = Base64.decode(src, Base64.URL_SAFE );
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    public static String getStringFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] b = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }
}
