package info.santhosh.evlo.data.scheduleJobs;

import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static info.santhosh.evlo.data.util.ProtoRequestAndStore.synchronousProtoRequest;

/**
 * Created by santhoshvai on 28/05/17.
 */

public class CommodityJob extends Job {

    static final String TAG = "CommodityJob";
    static final String TAG_NOT_CHARGING = "CommodityJob_NOT_CHARGING";
    static final String TAG_IMMEDIATE = "CommodityJob_IMMEDIATE";
    private static final String PROTO_URL = "https://www.dropbox.com/s/y80ip1cj3k0lds2/commodities_test?dl=1";

    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        return synchronousProtoRequest(getContext());
    }

    /**
     * Every 60 minutes, when charging
     * @return id of the job created
     */
    public static int scheduleJobWhenCharging() {
        Set<JobRequest> jobRequests = JobManager.instance().getAllJobRequestsForTag(TAG);
        if (!jobRequests.isEmpty()) {
            return jobRequests.iterator().next().getJobId();
        }

        return new JobRequest.Builder(TAG)
                .setPeriodic(TimeUnit.MINUTES.toMillis(60), TimeUnit.MINUTES.toMillis(5))
                .setRequiresCharging(true)
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                .setRequirementsEnforced(true)
                .build()
                .schedule();
    }

    /**
     * Every 4 hours, when not charging, on Wifi
     * @return id of the job created
     */
    public static int scheduleJobWhenNotChargingWiFiOnly() {
        // avoid 100 jobs can only be scheduled limit
        // https://github.com/evernote/android-job/issues/91
        // https://github.com/vRallev/job-sample/blob/master/app/src/main/java/com/evernote/android/job/sample/sync/SyncJob.java#L24
        Set<JobRequest> jobRequests = JobManager.instance().getAllJobRequestsForTag(TAG_NOT_CHARGING);
        if (!jobRequests.isEmpty()) {
            return jobRequests.iterator().next().getJobId();
        }

        // every 4 hours when not charging
        return new JobRequest.Builder(TAG_NOT_CHARGING)
                .setPeriodic(TimeUnit.HOURS.toMillis(4), TimeUnit.MINUTES.toMillis(5))
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
        return new JobRequest.Builder(TAG_IMMEDIATE)
                .startNow()
                .setUpdateCurrent(true)
                .build()
                .schedule();
    }

}
