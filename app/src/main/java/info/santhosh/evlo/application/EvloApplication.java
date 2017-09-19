package info.santhosh.evlo.application;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.evernote.android.job.JobManager;

import info.santhosh.evlo.common.DebugUtils;
import info.santhosh.evlo.data.scheduleJobs.CommodityJob;
import info.santhosh.evlo.data.scheduleJobs.CommodityJobCreator;
import io.fabric.sdk.android.Fabric;

/**
 * Created by santhoshvai on 28/11/2016.
 */

public class EvloApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DebugUtils.init(this);
        Fabric.with(this, new Crashlytics());
        JobManager.create(this).addJobCreator(new CommodityJobCreator());
        CommodityJob.scheduleJobWhenConnectedImmediately();
//        startService(new Intent(this, GetProtoDataService.class));
    }

}
