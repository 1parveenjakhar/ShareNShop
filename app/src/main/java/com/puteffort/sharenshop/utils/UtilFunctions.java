package com.puteffort.sharenshop.utils;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.gson.Gson;
import com.puteffort.sharenshop.R;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class UtilFunctions {
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    public static final OkHttpClient client = new OkHttpClient();
    public static final Gson gson = new Gson();

    public static final String SERVER_URL = "http://143.244.142.48:100/";
    public static final int ERROR_CODE = 401, SUCCESS_CODE = 200;

    public static final String INTENT_TAG = "Notification-Receiver";

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

    public static Set<Integer> getDefaultLastActivityChips() {
        return new HashSet<>(Arrays.asList(
                R.id.lessThan1Month, R.id.oneMonthTo6Months, R.id.sixMonthsTo1Year, R.id.greaterThan1Year));
    }

    public static Request getRequest(String json, String url) {
        RequestBody body = RequestBody.create(json, JSON);
        return new Request.Builder().url(url).post(body).build();
    }
}
