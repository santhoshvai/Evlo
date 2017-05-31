package info.santhosh.evlo.application;

import android.app.Application;
import android.content.Intent;

import com.evernote.android.job.JobManager;

import info.santhosh.evlo.common.DebugUtils;
import info.santhosh.evlo.data.scheduleJobs.CommodityJobCreator;
import info.santhosh.evlo.service.GetXmlService;

/**
 * Created by santhoshvai on 28/11/2016.
 */

public class EvloApplication extends Application {

    private static final EvloLifecycleCallbacks EVLO_LIFECYCLE_CALLBACKS = new EvloLifecycleCallbacks();

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(EVLO_LIFECYCLE_CALLBACKS);

        DebugUtils.init(this);

        JobManager.create(this).addJobCreator(new CommodityJobCreator());
        startService(new Intent(this, GetXmlService.class));
    }

    public static boolean isVisible() {
        return EVLO_LIFECYCLE_CALLBACKS.isVisible();
    }
}
