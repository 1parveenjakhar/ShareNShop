package com.puteffort.sharenshop.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.puteffort.sharenshop.R;

public class StaticData {
    private static Bitmap defaultImageBitmap;

    public static Bitmap getDefaultImageBitmap(Context context) {
        if (defaultImageBitmap == null) {
            defaultImageBitmap = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.default_user_image);
        }
        return defaultImageBitmap;
    }
}
