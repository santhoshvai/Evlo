package info.santhosh.evlo.common;

import android.os.StrictMode;

import com.facebook.stetho.Stetho;
import com.squareup.leakcanary.AndroidRefWatcherBuilder;
import com.squareup.leakcanary.LeakCanary;

import info.santhosh.evlo.application.EvloApplication;
import info.santhosh.evlo.service.CrashlyticsLeakLogService;

/**
 * Created by santhoshvai on 19/05/17.
 */

public class DebugUtils {

    private DebugUtils() {}

    public static void init(EvloApplication evloApplication) {
        // LEAK CANARY
        if (LeakCanary.isInAnalyzerProcess(evloApplication)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        AndroidRefWatcherBuilder refWatcher = LeakCanary.refWatcher(evloApplication).listenerServiceClass(CrashlyticsLeakLogService.class);
        refWatcher.buildAndInstall();
        //LeakCanary.install(evloApplication);

        // STETHO
        Stetho.initializeWithDefaults(evloApplication);

        // STRICT MODE
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());
    }
}
