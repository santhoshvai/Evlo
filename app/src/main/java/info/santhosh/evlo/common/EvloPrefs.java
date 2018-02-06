package info.santhosh.evlo.common;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;

/**
 * Created by santhoshvai on 28/05/17.
 */

public class EvloPrefs {

    private static final String GENERAL_PREFERENCES_KEY = "general_preferences_key";
    private static final String LAST_ROW_ORDER = "LAST_ROW_ORDER";
    private static final String LAST_ARRIVAL_DATE = "LAST_ARRIVAL_DATE";
    private static final String LAST_ARRIVAL_DATE_TIMESTAMP = "LAST_ARRIVAL_DATE_TIMESTAMP";
    private static final String LAST_DELETION_DATE_TIMESTAMP = "LAST_DELETION_DATE_TIMESTAMP";
    private static final String IS_FIRST_RUN = "IS_FIRST_RUN";
    private static final String DATA_HAS_LOADED_ATLEAST_ONCE = "DATA_HAS_LOADED_ATLEAST_ONCE";
    private static final String UNNOTIFIED_BOOKMARK_UPDATES = "UNNOTIFIED_BOOKMARK_UPDATES";
    private static final String APP_OPEN_COUNT = "APP_OPEN_COUNT";
    private static final String APP_OPEN_DATE_MILLIS = "APP_OPEN_DATE_MILLIS";
    private static final String APP_RATING_OPENED = "APP_RATING_OPENED";
    private static final String APP_RATING_CHECK_MULTIPLIER_COUNT = "APP_RATING_CHECK_MULTIPLIER_COUNT";

    private static final String TAG = "EvloPrefs";

    private EvloPrefs() {}

    private static SharedPreferences generalPreferences(Context context) {
        return context.getSharedPreferences(GENERAL_PREFERENCES_KEY, Context.MODE_PRIVATE);
    }

    public static int getLastRowOrder(Context context) {
        // <Table diffgr:id="Table43" msdata:rowOrder="42">
        // (roworder starts from 0, but id starts from 1)
        return generalPreferences(context).getInt(LAST_ROW_ORDER, -1);
    }

    public static void setLastRowOrder(Context context, int rowOrder) {
        final SharedPreferences.Editor editor = generalPreferences(context).edit();
        editor.putInt(LAST_ROW_ORDER, rowOrder);
        editor.apply();
    }

    public static Date getLastArrivalDate(Context context) {
        // <Arrival_Date>28/05/2017</Arrival_Date>
        String s = generalPreferences(context).getString(LAST_ARRIVAL_DATE, null);
        return Utils.convertArrivalDate(s);
    }

    public static void setLastArrivalDate(Context context, String arrivalDate) {
        final SharedPreferences.Editor editor = generalPreferences(context).edit();
        editor.putString(LAST_ARRIVAL_DATE, arrivalDate);
        editor.apply();
    }

    public static long getLastArrivalDateTimeStamp(Context context) {
        final long date = generalPreferences(context).getLong(LAST_ARRIVAL_DATE_TIMESTAMP, -1);
        return date;
    }

    public static void setLastArrivalDateTimeStamp(Context context, long arrivalDate) {
        final SharedPreferences.Editor editor = generalPreferences(context).edit();
        editor.putLong(LAST_ARRIVAL_DATE_TIMESTAMP, arrivalDate);
        editor.apply();
    }

    public static long getLastDeletionDateTimeStamp(Context context) {
        final long date = generalPreferences(context).getLong(LAST_DELETION_DATE_TIMESTAMP, -1);
        return date;
    }

    public static void setLastDeletionDateTimeStamp(Context context, long arrivalDate) {
        final SharedPreferences.Editor editor = generalPreferences(context).edit();
        editor.putLong(LAST_DELETION_DATE_TIMESTAMP, arrivalDate);
        editor.apply();
    }

    public static boolean getIsFirstRun(Context context) {
        return generalPreferences(context).getBoolean(IS_FIRST_RUN, true);
    }

    public static void setIsFirstRun(Context context, boolean isFirst) {
        final SharedPreferences.Editor editor = generalPreferences(context).edit();
        editor.putBoolean(IS_FIRST_RUN, isFirst);
        editor.apply();
    }

    public static boolean getDataHasLoadedAtleastOnce(Context context) {
        return generalPreferences(context).getBoolean(DATA_HAS_LOADED_ATLEAST_ONCE, false);
    }

    public static void setDataHasLoadedAtleastOnce(Context context, boolean value) {
        final SharedPreferences.Editor editor = generalPreferences(context).edit();
        editor.putBoolean(DATA_HAS_LOADED_ATLEAST_ONCE, value);
        editor.apply();
    }

    public static boolean getAppRatingOpened(Context context) {
        return generalPreferences(context).getBoolean(APP_RATING_OPENED, false);
    }

    public static void setAppRatingOpened(Context context, boolean value) {
        final SharedPreferences.Editor editor = generalPreferences(context).edit();
        editor.putBoolean(APP_RATING_OPENED, value);
        editor.apply();
    }

    public static int getAppOpenCount(Context context) {
        return generalPreferences(context).getInt(APP_OPEN_COUNT, 0);
    }

    public static void setAppOpenCount(Context context, int value) {
        final SharedPreferences.Editor editor = generalPreferences(context).edit();
        editor.putInt(APP_OPEN_COUNT, value);
        editor.apply();
    }

    public static int getAppCheckMultiplierCount(Context context) {
        return generalPreferences(context).getInt(APP_RATING_CHECK_MULTIPLIER_COUNT, 1);
    }

    public static void setAppCheckMultiplierCount(Context context, int value) {
        final SharedPreferences.Editor editor = generalPreferences(context).edit();
        editor.putInt(APP_RATING_CHECK_MULTIPLIER_COUNT, value);
        editor.apply();
    }

    public static long getAppOpenDateMillis(Context context) {
        return generalPreferences(context).getLong(APP_OPEN_DATE_MILLIS, 0);
    }

    public static void setAppOpenDateMillis(Context context, long value) {
        final SharedPreferences.Editor editor = generalPreferences(context).edit();
        editor.putLong(APP_OPEN_DATE_MILLIS, value);
        editor.apply();
    }

    public static int getUnnotifiedBookmarkUpdates(Context context) {
        return generalPreferences(context).getInt(UNNOTIFIED_BOOKMARK_UPDATES, 0);
    }

    /**
     *
     * @param context
     * @param newUpdatesCount how many bookmarks have been updated?
     * @return total number of unnotified updates
     */
    public static int updateUnnotifiedBookmarkUodates(Context context, int newUpdatesCount) {
        int count = getUnnotifiedBookmarkUpdates(context) + newUpdatesCount;
        final SharedPreferences.Editor editor = generalPreferences(context).edit();
        editor.putInt(UNNOTIFIED_BOOKMARK_UPDATES, count);
        editor.apply();
        return count;
    }

    public static void resetUnnotifiedBookmarkUpdates(Context context) {
        final SharedPreferences.Editor editor = generalPreferences(context).edit();
        editor.putInt(UNNOTIFIED_BOOKMARK_UPDATES, 0);
        editor.apply();
    }
}
