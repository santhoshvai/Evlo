package info.santhosh.evlo;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by santhoshvai on 28/11/2016.
 */

public class EvloApplication extends Application {

    public void onCreate() {
        super.onCreate();

        if(BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this);
        }
    }
}
