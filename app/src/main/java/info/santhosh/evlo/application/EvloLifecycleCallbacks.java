package info.santhosh.evlo.application;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * Created by santhoshvai on 28/05/17.
 */

class EvloLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

    private static boolean isVisible;

    public boolean isVisible(){
        return isVisible;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        isVisible = true;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        isVisible = false;
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
