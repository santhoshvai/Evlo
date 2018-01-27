package info.santhosh.evlo.ui.main;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.Spanned;

import info.santhosh.evlo.R;
import info.santhosh.evlo.common.ShareDialogFragment;
import info.santhosh.evlo.data.dbModels.Commodity;

/**
 * Created by santhoshvai on 27/01/2018.
 */

public class DisclaimerDialog extends DialogFragment {
    public static DisclaimerDialog newInstance() {
        DisclaimerDialog fragment = new DisclaimerDialog();
        return fragment;
    }

    @Override @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.disclaimer_description)
                .setTitle(R.string.disclaimer)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dismiss();
                    }
                });
        return builder.create();
    }

    public static void showDisclaimer(final FragmentActivity activity) {
        FragmentManager fm = activity.getSupportFragmentManager();
        DisclaimerDialog fragment = DisclaimerDialog.newInstance();
        fragment.show(fm, "DisclaimerDialog");
    }
}
