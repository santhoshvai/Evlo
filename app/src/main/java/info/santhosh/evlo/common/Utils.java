package info.santhosh.evlo.common;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import info.santhosh.evlo.R;

/**
 * Created by santhoshvai on 09/05/16.
 */
public class Utils {

    private static final String TAG = "Utils";

    private static int screenWidth = 0;
    private static int screenHeight = 0;

    private Utils() {}

    /**
     * hides the keyboard
     * */
    public static void hideSoftKeyboard(@NonNull Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View currentFocus = activity.getCurrentFocus();
        if(currentFocus != null) {
            inputMethodManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }

    /**
     * shows the keyboard
     * */
    public static void showKeyboard(@NonNull Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
    }

    /**
     * Returns true if the network is available or about to become available.
     */
    public static boolean isNetworkAvailable(Context c) {
        ConnectivityManager cm =
                (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    /**
     * Helper method to set the correct empty text
     * empty view text must be changed in various scenarios, as below
     * <ul>
     *      <li>No internet</li>
     *      <li>Data being loaded</li>
     *      <li>Search not found </li>
     *</ul>
     * @param context to check if internet is there
     * @param mEmptyView the empty textview
     * @param searchFilter the search filter used
     */
    public static void setEmptyViewText(Context context, TextView mEmptyView, String searchFilter) {
        if(searchFilter.isEmpty()) { // not a search thing ( this will occur only once during app lifetime )
            mEmptyView.setText(R.string.loading);

            if(! isNetworkAvailable(context)) {
                mEmptyView.setText(R.string.Connect_internet);
            }
        } else { // search did not yield results
            mEmptyView.setText(context.getString(R.string.no_search_results, searchFilter));
        }
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int getScreenHeight(Context c) {
        if (screenHeight == 0) {
            WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screenHeight = size.y;
        }

        return screenHeight;
    }

    public static int getScreenWidth(Context c) {
        if (screenWidth == 0) {
            WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            screenWidth = size.x;
        }

        return screenWidth;
    }

    public static boolean isAPI21Plus() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static Date convertArrivalDate(String s) {
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        try {
            return df.parse(s);
        } catch(ParseException e) {
            Log.e(TAG, "cannot parse date in the prefs: " + s, e);
        }
        return null;
    }

}
