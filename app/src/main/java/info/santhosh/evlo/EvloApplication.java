package info.santhosh.evlo;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.squareup.leakcanary.LeakCanary;

/**
 * Created by santhoshvai on 28/11/2016.
 */

public class EvloApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
        if(BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this);
        }
    }
}
