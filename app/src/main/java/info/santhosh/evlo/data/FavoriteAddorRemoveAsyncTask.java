package info.santhosh.evlo.data;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import info.santhosh.evlo.common.WriteDb;

/**
 * Created by santhoshvai on 03/02/2017.
 */

public class FavoriteAddorRemoveAsyncTask extends AsyncTask<Integer, Void, Boolean> {

    Context mApplicationContext;
    boolean mIsAdd;
    Uri mUri; // uri for which change should be notified

    public FavoriteAddorRemoveAsyncTask(Context context, boolean isAdd) {
        mApplicationContext = context.getApplicationContext();
        mIsAdd = isAdd;
        mUri = null;
    }

    public FavoriteAddorRemoveAsyncTask(Context context, boolean isAdd, Uri uri) {
        mApplicationContext = context.getApplicationContext();
        mIsAdd = isAdd;
        mUri = uri;
    }

    @Override
    protected Boolean doInBackground(Integer... params) {
        WriteDb writeDb = new WriteDb(mApplicationContext);
        boolean success;
        if(mIsAdd) {
            success = writeDb.addUsingCommoditiesFavId(params[0]) != null;
        } else {
            success = writeDb.removeUsingCommoditiesFavId(params[0]) != 0;
        }

        if(mUri != null && success) {
            ContentResolver contentResolver = mApplicationContext.getContentResolver();
            contentResolver.notifyChange(mUri, null);
        }

        return success;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        // Do nothing
    }
}
