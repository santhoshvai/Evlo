package info.santhosh.evlo.common;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import java.util.List;

import info.santhosh.evlo.R;

import static info.santhosh.evlo.common.Constants.SEVEN_DAYS_IN_MILLIS;

/**
 * Created by santhoshvai on 06/02/2018.
 */

public class RateAskDialogFragment extends DialogFragment {

    public static RateAskDialogFragment newInstance() {
        return new RateAskDialogFragment();
    }

    @Override @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.rate_app_dialog, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setPositiveButton(R.string.rate_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EvloPrefs.setAppRatingOpened(getContext(), true);
                        openAppRating(getActivity());
                    }
                })
                .setNeutralButton(R.string.rate_neutral, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // dimessed, ask him later
                        final int new_multiplier = EvloPrefs.getAppCheckMultiplierCount(getContext()) + 1;
                        EvloPrefs.setAppCheckMultiplierCount(getContext(), new_multiplier);
                        dismiss();
                    }
                })
                .setNegativeButton(R.string.rate_negative, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // user doesnt want to review, but sends feedback, so ask him much later :(
                        final int new_multiplier = EvloPrefs.getAppCheckMultiplierCount(getContext()) + 2;
                        EvloPrefs.setAppCheckMultiplierCount(getContext(), new_multiplier);
                        dismiss();
                        Utils.composeEmail(getActivity(), getString(R.string.feedback_email), getString(R.string.contact_us_subject));
                    }
                });
            return builder.create();

    }

    public static boolean shouldShow(@NonNull Context context) {
        // already rating was opened
        if (EvloPrefs.getAppRatingOpened(context)) return false;

        // App opened atleast 4 times && App used atleast for 7 days
        int count_app_open = EvloPrefs.getAppOpenCount(context) + 1;
        long lastOpenTime = EvloPrefs.getAppOpenDateMillis(context);
        boolean usedForSevenMultipliedDays = false;
        if (lastOpenTime != 0) {
            final int multiplier = EvloPrefs.getAppCheckMultiplierCount(context);
            usedForSevenMultipliedDays = (System.currentTimeMillis() - lastOpenTime) >= (multiplier * SEVEN_DAYS_IN_MILLIS);
        }
        if (count_app_open >= 4 && usedForSevenMultipliedDays) return true;
        EvloPrefs.setAppOpenCount(context, count_app_open);
        EvloPrefs.setAppOpenDateMillis(context, System.currentTimeMillis());
        return false;
    }


    static void openAppRating(Context context) {
        // you can also use BuildConfig.APPLICATION_ID
        String appId = context.getPackageName();
        Intent rateIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("market://details?id=" + appId));
        boolean marketFound = false;

        // find all applications able to handle our rateIntent
        final List<ResolveInfo> otherApps = context.getPackageManager()
                .queryIntentActivities(rateIntent, 0);
        for (ResolveInfo otherApp: otherApps) {
            // look for Google Play application
            if (otherApp.activityInfo.applicationInfo.packageName
                    .equals("com.android.vending")) {

                ActivityInfo otherAppActivity = otherApp.activityInfo;
                ComponentName componentName = new ComponentName(
                        otherAppActivity.applicationInfo.packageName,
                        otherAppActivity.name
                );
                // make sure it does NOT open in the stack of your activity
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                // task reparenting if needed
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                // if the Google Play was already open in a search result
                //  this make sure it still go to the app page you requested
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                // this make sure only the Google Play app is allowed to
                // intercept the intent
                rateIntent.setComponent(componentName);
                context.startActivity(rateIntent);
                marketFound = true;
                break;

            }
        }

        // if GP not present on device, open web browser
        if (!marketFound) {
            Intent webIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id="+appId));
            context.startActivity(webIntent);
        }
    }

    public static void start(final FragmentActivity activity) {
        FragmentManager fm = activity.getSupportFragmentManager();
        RateAskDialogFragment fragment = RateAskDialogFragment.newInstance();
        fragment.show(fm, "RateAskDialogFragment");
    }
}