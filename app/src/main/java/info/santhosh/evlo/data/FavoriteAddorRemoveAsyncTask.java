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

    Context mContext;
    boolean mIsAdd;
    Uri mUri = null; // uri for which change should be notified

    public FavoriteAddorRemoveAsyncTask(Context context, boolean isAdd) {
        mContext = context.getApplicationContext();
        mIsAdd = isAdd;
    }

    public FavoriteAddorRemoveAsyncTask(Context context, boolean isAdd, Uri uri) {
        mContext = context.getApplicationContext();
        mIsAdd = isAdd;
        mUri = uri;
    }

    @Override
    protected Boolean doInBackground(Integer... params) {
        WriteDb writeDb = new WriteDb(mContext);
        if(mIsAdd) {
            return ( writeDb.addUsingCommoditiesFavId(params[0]) != null );
        } else {
           return ( writeDb.removeUsingCommoditiesFavId(params[0]) != 0 );
        }
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        if(mUri != null) {
            ContentResolver contentResolver = mContext.getContentResolver();
            contentResolver.notifyChange(mUri, null);
        }
    }
}
