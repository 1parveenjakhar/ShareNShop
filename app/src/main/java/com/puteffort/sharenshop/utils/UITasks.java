package com.puteffort.sharenshop.utils;

import android.content.Context;
import android.widget.Toast;

public class UITasks {
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
