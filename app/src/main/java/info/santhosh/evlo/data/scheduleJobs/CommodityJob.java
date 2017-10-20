package info.santhosh.evlo.data.scheduleJobs;

import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;

import java.util.concurrent.TimeUnit;

import info.santhosh.evlo.service.GetProtoDataService;

/**
 * Created by santhoshvai on 28/05/17.
 */

public class CommodityJob extends Job {

    static final String TAG = "CommodityJob";
    private static final String RUN_WHEN_APP_IS_OPEN = "run_when_app_is_open";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        PersistableBundleCompat extras = params.getExtras();
        Boolean runWhenAppOpen = extras.getBoolean(RUN_WHEN_APP_IS_OPEN, false);

//        if(!runWhenAppOpen) { // TODO: find a way to see if app is open? maybe shared prefs?
//            // do not run when app is visible to the user
//            // this creates stutter in the application
//            return Result.SUCCESS;
//        }
        return GetProtoDataService.synchronousProtoRequest(getContext());
    }

    @Override
    protected void onReschedule(int newJobId) {
        // the rescheduled job has a new ID
    }

    /**
     * Every 30 minutes, when charging
     * @return id of the job created
     */
    public static int scheduleJobWhenCharging() {
        return new JobRequest.Builder(TAG)
                .setPeriodic(TimeUnit.MINUTES.toMillis(30), TimeUnit.MINUTES.toMillis(5))
                .setRequiresCharging(true)
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                .setRequirementsEnforced(true)
                .build()
                .schedule();
    }

    /**
     * Every 2 hours, when not charging, on Wifi
     * @return id of the job created
     */
    public static int scheduleJobWhenNotChargingWiFiOnly() {
        // every 4 hours when not charging
        return new JobRequest.Builder(TAG)
                .setPeriodic(TimeUnit.HOURS.toMillis(3), TimeUnit.MINUTES.toMillis(5))
                .setRequiresCharging(false)
                .setRequiredNetworkType(JobRequest.NetworkType.UNMETERED)
                .setRequirementsEnforced(true)
                .build()
                .schedule();
    }

    /**
     * To be used when we try to access internet when user was offline,
     * This will immediately start after user gets online
     * @return id of the job created
     */
    public static int scheduleJobWhenConnectedImmediately() {

        return new JobRequest.Builder(TAG)
                .setExecutionWindow(1, TimeUnit.DAYS.toMillis(1)) // https://github.com/evernote/android-job/issues/228
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                .setRequirementsEnforced(true)
                .build()
                .schedule();
    }

    public static int scheduleJobImmediately() {

        return new JobRequest.Builder(TAG)
                .startNow()
                .build()
                .schedule();
    }


}
