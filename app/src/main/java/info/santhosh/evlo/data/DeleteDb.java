package info.santhosh.evlo.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import info.santhosh.evlo.common.EvloPrefs;
import info.santhosh.evlo.data.CommodityContract;
import info.santhosh.evlo.data.CommodityDbHelper;

import static info.santhosh.evlo.common.Constants.SEVEN_DAYS_IN_MILLIS;

/**
 * Created by santhoshvai on 21/01/2018.
 */

public class DeleteDb {
    static final String TAG = "DeleteDb";

    // Delete old entries: a week old && not a fav
    /*  SELECT commodity_data._id
        FROM commodity_data LEFT JOIN commodity_fav ON commodity_fav.fav_id = commodity_data._id
        WHERE commodity_fav.fav_id IS NULL AND commodity_data.arrival_date < 1500501600000
     */
    public static void deleteOldCommodities(Context context) {
        final long lastDeletionDateTimeStamp = EvloPrefs.getLastDeletionDateTimeStamp(context);
        if (System.currentTimeMillis() - lastDeletionDateTimeStamp < SEVEN_DAYS_IN_MILLIS) {
            // if we had performed a delete only 7 days ago, skip doing that
            Log.d(TAG, "Deletion skipped. ");
            return;
        }
        Log.d(TAG, "Deletion start. ");
        List<String> oldCommodityIds = new ArrayList<>();
        final long lastArrivalTimeStamp = EvloPrefs.getLastArrivalDateTimeStamp(context);
        final long dateBelowToDelete = lastArrivalTimeStamp - SEVEN_DAYS_IN_MILLIS;
        CommodityDbHelper dbHelper = new CommodityDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            String query = "SELECT " +  CommodityContract.CommodityDataEntry.TABLE_NAME +
                    "." + CommodityContract.CommodityDataEntry._ID +
                    " FROM " + CommodityContract.CommodityDataEntry.TABLE_NAME + " LEFT JOIN " +
                    CommodityContract.CommodityFavEntry.TABLE_NAME +
                    " ON " + CommodityContract.CommodityFavEntry.TABLE_NAME +
                    "." + CommodityContract.CommodityFavEntry.COLUMN_FAV_ID +
                    " = " + CommodityContract.CommodityDataEntry.TABLE_NAME +
                    "." + CommodityContract.CommodityDataEntry._ID +
                    " WHERE " + CommodityContract.CommodityFavEntry.TABLE_NAME +
                    "." + CommodityContract.CommodityFavEntry.COLUMN_FAV_ID + " IS NULL AND " +
                    CommodityContract.CommodityDataEntry.COLUMN_ARRIVAL_DATE + " < ?";

            Cursor cursor = db.rawQuery(query, new String[] {Long.toString(dateBelowToDelete)});

            while (cursor.moveToNext()) {
                oldCommodityIds.add( Long.toString(cursor.getLong(0)) );
            }
            cursor.close();
            // check with SELECT COUNT(commodity_data._id) FROM commodity_data LEFT JOIN commodity_fav ON commodity_fav.fav_id = commodity_data._id
            String args = TextUtils.join(", ", oldCommodityIds);

            // https://stackoverflow.com/a/14358049
            db.execSQL(String.format("DELETE FROM " + CommodityContract.CommodityDataEntry.TABLE_NAME +
                    " WHERE " + CommodityContract.CommodityDataEntry.TABLE_NAME +
                    "." + CommodityContract.CommodityDataEntry._ID + " IN (%s);", args));
            Log.d(TAG, "Deletion Complete. ");
            EvloPrefs.setLastDeletionDateTimeStamp(context, System.currentTimeMillis());
        } catch(Exception e){
            // here you can catch all the exceptions
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

}
