package info.santhosh.evlo.ui.intro;

import android.os.AsyncTask;

import com.evernote.android.job.Job;

import java.lang.ref.WeakReference;

import info.santhosh.evlo.common.WriteDb;
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
                // used 60 here, remaining 40 is for sql storage
                final int percent = Math.round((60 * bytesRead) / contentLength);
                final IntroActivity introActivity = introActivityWeakReference.get();
                if (introActivity == null) return;
                if (done) introActivity.onProgressUpdate(60);
                else introActivity.onProgressUpdate(percent);
            }
        };

        WriteDb.WriteDbProgressListener writeDbProgressListener = new WriteDb.WriteDbProgressListener() {
            @Override
            public void update(int doneSoFar, int length, boolean done) {
                final int percent = 60 + Math.round((40 * doneSoFar) / length);
                final IntroActivity introActivity = introActivityWeakReference.get();
                if (introActivity == null) return;
                if (done) introActivity.onProgressUpdate(100);
                else introActivity.onProgressUpdate(percent);
            }
        };
        final IntroActivity introActivity = introActivityWeakReference.get();
        if (introActivity == null) return null;
        return synchronousProgressiveProtoRequest(introActivity.getApplicationContext(), progressListener, writeDbProgressListener);
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
