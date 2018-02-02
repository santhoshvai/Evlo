package info.santhosh.evlo.common;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.lang.ref.WeakReference;

/**
 * Created by santhoshvai on 02/02/2018.
 */

public class ConnectionStatusReceiver extends BroadcastReceiver {

    WeakReference<ConnectionReceivableActivity> activityWeakReference;

    public ConnectionStatusReceiver(ConnectionReceivableActivity activity) {
        activityWeakReference = new WeakReference<>(activity);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        ConnectionReceivableActivity activity = activityWeakReference.get();
        if (manager != null && activity != null) {
            NetworkInfo connection = manager.getActiveNetworkInfo();
            if (connection != null && connection.isConnectedOrConnecting()){
                activity.onOnline();
            } else {
                activity.onOffline();
            }
        }
    }

    public interface ConnectionReceivableActivity {
        void onOnline();
        void onOffline();
    }
}
