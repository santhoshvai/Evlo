package info.santhosh.evlo.common;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

/**
 * Created by santhoshvai on 09/05/16.
 */
public class Utils {

    private Utils() {}

    /**
     * hides the keyboard
     * */
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View currentFocus = activity.getCurrentFocus();
        if(currentFocus != null) {
            inputMethodManager.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
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
            mEmptyView.setText("Data is being loaded");

            if(! isNetworkAvailable(context)) {
                mEmptyView.setText("Please connect to the internet");
                return;
            }
        } else { // search did not yield results
            mEmptyView.setText("No search results for " + searchFilter);
        }
    }

}
