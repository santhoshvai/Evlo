package info.santhosh.evlo.data.scheduleJobs;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

/**
 * Created by santhoshvai on 28/05/17.
 */

public class CommodityJobCreator implements JobCreator {
    @Override
    public Job create(String tag) {
        switch (tag) {
            case CommodityJob.TAG:
                return new CommodityJob();
            default:
                return null;
            // TODO: a job to monitor favorites price update and trigger a notification
        }
    }
}
