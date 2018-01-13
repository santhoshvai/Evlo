package info.santhosh.evlo.ui.intro;

import android.os.AsyncTask;

import com.evernote.android.job.Job;

import java.lang.ref.WeakReference;

import info.santhosh.evlo.data.util.Progress;

import static info.santhosh.evlo.data.util.ProtoRequestAndStore.synchronousProgressiveProtoRequest;

/**
 * Created by santhoshvai on 27/11/17.
 */

public class IntroLoadProgress extends AsyncTask<Void, Integer, Job.Result> {

    WeakReference<IntroActivity> introActivityWeakReference;

    public IntroLoadProgress(IntroActivity introActivity) {
        introActivityWeakReference = new WeakReference<>(introActivity);
    }

    @Override
    protected Job.Result doInBackground(Void... voids) {
        final Progress.ProgressListener progressListener = new Progress.ProgressListener() {
            @Override public void update(long bytesRead, long contentLength, boolean done) {
                // used 80 here, remaining 20 is for sql storage
                final int percent = Math.round((80 * bytesRead) / contentLength);
                final IntroActivity introActivity = introActivityWeakReference.get();
                if (introActivity == null) return;
                introActivity.onProgressUpdate(percent);
            }
        };
        final IntroActivity introActivity = introActivityWeakReference.get();
        if (introActivity == null) return null;
        return synchronousProgressiveProtoRequest(introActivity.getApplicationContext(), progressListener);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        final IntroActivity introActivity = introActivityWeakReference.get();
        if (introActivity == null) return;
        introActivity.onProgressUpdate(values[0]);
    }

    @Override
    protected void onPostExecute(Job.Result result) {
        final IntroActivity introActivity = introActivityWeakReference.get();
        if (introActivity == null) return;
        if (result == Job.Result.SUCCESS) {
            introActivity.onProgressUpdate(100);
            introActivity.onLoadDone();
        }
    }
}
