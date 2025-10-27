package com.example.appecig.util;

import android.content.Context;
import android.widget.Toast;

public class Toasty {
    public static void message(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
