package info.santhosh.evlo;

import android.app.Application;

import info.santhosh.evlo.common.DebugUtils;

/**
 * Created by santhoshvai on 28/11/2016.
 */

public class EvloApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        DebugUtils.init(this);
    }
}
