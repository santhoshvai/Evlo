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
    private static final String IS_FIRST_RUN = "IS_FIRST_RUN";

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
        // <Arrival_Date>28/05/2017</Arrival_Date>
        final long date = generalPreferences(context).getLong(LAST_ARRIVAL_DATE_TIMESTAMP, -1);
        return date;
    }

    public static void setLastArrivalDateTimeStamp(Context context, long arrivalDate) {
        final SharedPreferences.Editor editor = generalPreferences(context).edit();
        editor.putLong(LAST_ARRIVAL_DATE_TIMESTAMP, arrivalDate);
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
}
