package com.puteffort.sharenshop.utils;

import android.content.Context;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UtilFunctions {
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static boolean isEmailValid(String emailId) {
        //Validating email ID using Regex
        final Pattern VALID_EMAIL_ADDRESS_REGEX =
                Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailId);
        return matcher.find();
    }

    public static String getFormattedTime(int years, int months, int days) {
        StringBuilder time = new StringBuilder();
        if (years != 0) time.append(years).append("Y ");
        if (months != 0) time.append(months).append("M ");
        if (days != 0) time.append(days).append("D ");
        return time.toString().trim();
    }
}