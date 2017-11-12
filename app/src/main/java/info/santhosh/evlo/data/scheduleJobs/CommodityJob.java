package info.santhosh.evlo.data.scheduleJobs;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import info.santhosh.evlo.common.DataFetchStatusProvider;
import info.santhosh.evlo.common.EvloPrefs;
import info.santhosh.evlo.common.WriteDb;
import info.santhosh.evlo.model.CommodityProtos;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
     * Every 30 minutes, when charging
     * @return id of the job created
     */
    public static int scheduleJobWhenCharging() {
        Set<JobRequest> jobRequests = JobManager.instance().getAllJobRequestsForTag(TAG);
        if (!jobRequests.isEmpty()) {
            return jobRequests.iterator().next().getJobId();
        }

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
        // avoid 100 jobs can only be scheduled limit
        // https://github.com/evernote/android-job/issues/91
        // https://github.com/vRallev/job-sample/blob/master/app/src/main/java/com/evernote/android/job/sample/sync/SyncJob.java#L24
        Set<JobRequest> jobRequests = JobManager.instance().getAllJobRequestsForTag(TAG_NOT_CHARGING);
        if (!jobRequests.isEmpty()) {
            return jobRequests.iterator().next().getJobId();
        }

        // every 4 hours when not charging
        return new JobRequest.Builder(TAG_NOT_CHARGING)
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
        return new JobRequest.Builder(TAG_IMMEDIATE)
                .startNow()
                .setUpdateCurrent(true)
                .build()
                .schedule();
    }

    @WorkerThread
    private static Job.Result synchronousProtoRequest(Context context) {
        DataFetchStatusProvider.getInstance(context).setDataFetchStatus(DataFetchStatusProvider.STARTED);
        OkHttpClient client = new OkHttpClient();
        Request requestProto = new Request.Builder()
                .url( PROTO_URL )
                .build();
        Response response = null;

        try {
            response = client.newCall(requestProto).execute();
            if (!response.isSuccessful() || response.body() == null) {
                DataFetchStatusProvider.getInstance(context).setDataFetchStatus(DataFetchStatusProvider.ERROR);
                return Job.Result.RESCHEDULE;
            }
            final InputStream byteStream = response.body().byteStream();
            CommodityProtos.Commodities commoditiesProto = CommodityProtos.Commodities.parseFrom(byteStream);
            WriteDb.usingProtos(context, commoditiesProto);
            Log.d(TAG, "Received proto list size: " + commoditiesProto.getCommodityCount());

            if (commoditiesProto.getCommodityCount() > 0) {
                if (!EvloPrefs.getDataHasLoadedAtleastOnce(context)) {
                    EvloPrefs.setDataHasLoadedAtleastOnce(context, true);
                }
                DataFetchStatusProvider.getInstance(context).setDataFetchStatus(DataFetchStatusProvider.DONE);
            } else {
                DataFetchStatusProvider.getInstance(context).setDataFetchStatus(DataFetchStatusProvider.ERROR);
            }

            return Job.Result.SUCCESS;
        } catch (IOException e) {
            DataFetchStatusProvider.getInstance(context).setDataFetchStatus(DataFetchStatusProvider.ERROR);
            return Job.Result.RESCHEDULE;
        } catch(Exception e) {
            // TODO: log exception to firebase
            e.printStackTrace();
            DataFetchStatusProvider.getInstance(context).setDataFetchStatus(DataFetchStatusProvider.ERROR);
            return Job.Result.FAILURE;
        } finally {
            if (response != null && response.body() != null) {
                response.body().close();
            }
        }
    }


}
