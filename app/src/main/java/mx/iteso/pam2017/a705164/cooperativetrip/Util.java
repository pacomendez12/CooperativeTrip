package mx.iteso.pam2017.a705164.cooperativetrip;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by pacomendez on 5/1/17.
 */

public class Util {
    private static ConnectivityManager conMgr;

    public static boolean isConnected(Activity activity) {
        if (conMgr == null) {
            conMgr = (ConnectivityManager) activity.getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
        }

        NetworkInfo i = conMgr.getActiveNetworkInfo();
        if (i == null)
            return false;
        if (!i.isConnected())
            return false;
        if (!i.isAvailable())
            return false;
        return true;
    }
}
