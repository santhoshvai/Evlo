package info.santhosh.evlo.common;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by santhoshvai on 23/10/17.
 */

public final class DataFetchStatusProvider {
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STARTED, DONE, ERROR})
    public @interface DataFetchStatus {}
    public static final int STARTED = 0;
    public static final int DONE = 1;
    public static final int ERROR = 2;

    private static final Object mLock = new Object();
    private static DataFetchStatusProvider mInstance;

    private Context applnContext;
    private @DataFetchStatus int status = STARTED;
    @Nullable private WriteDb.WriteDbProgressListener writeDbProgressListener;

    public static DataFetchStatusProvider getInstance(Context context) {
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new DataFetchStatusProvider(context);
            }
            return mInstance;
        }
    }

    private DataFetchStatusProvider(Context context) {
        applnContext= context.getApplicationContext();
    }

    public @DataFetchStatus int getStatus() {
        return status;
    }

    public void setDataFetchStatus(@DataFetchStatus int status) {
        this.status = status;
        switch(status) {
            case STARTED :
                LocalBroadcastManager.getInstance(applnContext).sendBroadcast(new Intent(Constants.INTENT_DATA_FETCH_START));
                break;
            case DONE:
                LocalBroadcastManager.getInstance(applnContext).sendBroadcast(new Intent(Constants.INTENT_DATA_FETCH_DONE));
                break;
            case ERROR :
                LocalBroadcastManager.getInstance(applnContext).sendBroadcast(new Intent(Constants.INTENT_DATA_FETCH_ERROR));
                break;
            default :
                throw new IllegalArgumentException("Illegal Data fetch status: " + status);
        }
    }

    public void setWriteDbProgressListener(@Nullable WriteDb.WriteDbProgressListener writeDbProgressListener) {
        this.writeDbProgressListener = writeDbProgressListener;
    }

    public boolean shouldGiveBulkInsertUpdates() {
        return this.writeDbProgressListener != null;
    }

    public void updateWriteDbProgress(int inserted, int total, boolean done) {
        if (writeDbProgressListener != null) {
            // we give the second 50% weightage, so 0.5..1
            writeDbProgressListener.update(inserted + total, total * 2, done);
            if (done) this.writeDbProgressListener = null;
        }
    }
}
