package com.gali.apps.movielibrary;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;

/**
 * Created by 1 on 2/27/2017.
 */

public class InternetChecker {
    /*
    public static boolean isConnected() throws InterruptedException, IOException {
        String command = "ping -c 1 google.com";
        return (Runtime.getRuntime().exec (command).waitFor() == 0);
    }
    */
    public static boolean isConnected(Context context)  {
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean result = activeNetworkInfo != null && activeNetworkInfo.isConnected();
        return result;
    }
}
