package org.techtown.boda;

import android.app.Application;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

public class MyApp extends Application {
    private AppNetworkChangeReceiver networkChangeReceiver;

    @Override
    public void onCreate() {
        super.onCreate();

        networkChangeReceiver = new AppNetworkChangeReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, filter);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        unregisterReceiver(networkChangeReceiver);
    }
}
