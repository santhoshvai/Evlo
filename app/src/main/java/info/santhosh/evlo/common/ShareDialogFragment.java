package info.santhosh.evlo.common;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import info.santhosh.evlo.R;
import info.santhosh.evlo.data.dbModels.Commodity;

/**
 * Created by santhoshvai on 24/01/2018.
 */

public class ShareDialogFragment extends DialogFragment {

    public static final String COMMODITY = "COMMODITY";

    public static ShareDialogFragment newInstance(Commodity commodity) {
        Bundle args = new Bundle();
        args.putParcelable(COMMODITY, commodity);
        ShareDialogFragment fragment = new ShareDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        final Commodity commodity = bundle.getParcelable(COMMODITY);

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View view = inflater.inflate(R.layout.share_card, null);

        final String commodityName = commodity.getCommodity();
        final String modalPrice = commodity.getModal_Price();
        final String district = commodity.getDistrict();
        final String market = commodity.getMarket();
        final String state = commodity.getState();
        // variety name given is same as commodityName, then replace it as Normal
        final String variety = commodity.getVariety();

        TextView varietyTv = view.findViewById(R.id.text_variety_and_or_market);
        TextView modalPriceTv = view.findViewById(R.id.text_modal_price);
        TextView stateTv = view.findViewById(R.id.text_state_name);
        TextView marketTv = view.findViewById(R.id.text_market_and_or_district);
        TextView arrivalDateTv = view.findViewById(R.id.text_arrival_date);
        TextView maxPriceTv = view.findViewById(R.id.text_max_price);
        TextView minPriceTv = view.findViewById(R.id.text_min_price);

        String market_text = getActivity().getString(R.string.market, market, district);
        marketTv.setText(market_text);
        stateTv.setText(state);
        varietyTv.setText(getActivity().getString(R.string.commodity_name_and_variety, commodityName, variety));

        final String maxPrice = getActivity().getString(R.string.rupee_price_with_unit, commodity.getMax_Price());
        final String minPrice = getActivity().getString(R.string.rupee_price_with_unit, commodity.getMin_Price());
        final String modal_price_text = getActivity().getString(R.string.rupee_price_with_unit, modalPrice);
        final String arrivalDate = Utils.getArrivalDateString(commodity.getArrival_Date());
        arrivalDateTv.setText(arrivalDate);
        maxPriceTv.setText(maxPrice);
        minPriceTv.setText(minPrice);

        modalPriceTv.setText(Html.fromHtml(modal_price_text));

        String share_content = getActivity().getString(R.string.share_content);
        String no_thanks = getActivity().getString(R.string.no_thanks);

        // show dialog to user
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton(share_content, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // actual sharing
                shareBitmap(Utils.getBitmapFromView(view), getActivity(), commodity);
            }
        }).setNegativeButton(no_thanks, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.setView(view);
        return builder.create();
    }

    private static void shareBitmap(Bitmap bitmap, Context context, Commodity commodity) {
        Uri bmpUri = getLocalBitmapUri(bitmap, context, commodity);
        Intent shareIntent = new Intent();
        if (bmpUri != null) {
            // Construct a ShareIntent with link to image
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
            shareIntent.setType("image/*");
        } else {
            // sharing image failed, handle error by sharing text instead
            shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            final String commodityName = commodity.getCommodity();
            final String modalPrice = commodity.getModal_Price();
            final String district = commodity.getDistrict();
            final String market = commodity.getMarket();
            final String state = commodity.getState();
            final String variety = commodity.getVariety();
            String share = context.getString(R.string.share_data,
                    commodityName, variety, modalPrice, market, district, state);
            shareIntent.putExtra(Intent.EXTRA_TEXT, share);
            shareIntent.setType("text/plain");
        }
        // launch sharing dialog
        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_heading)));
    }

    @Nullable
    private static Uri getLocalBitmapUri(Bitmap bmp, Context context, Commodity commodity) {
        // Store image to default external storage directory
        Uri bmpUri = null;
        String fileName  = commodity.getCommodity() + "_" + System.currentTimeMillis() + ".png";
        try {
            // Use methods on Context to access package-specific directories on external storage.
            // This way, you don't need to request external read/write permission.
            // See https://youtu.be/5xVh-7ywKpE?t=25m25s
            // getExternalFilesDir() + "/Pictures" should match the declaration in fileprovider.xml paths
            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            if (Utils.isAPI24Plus()) {
                // wrap File object into a content provider. NOTE: authority here should match authority in manifest declaration
                bmpUri = FileProvider.getUriForFile(context, context.getString(R.string.file_authority), file);
            } else {
                // This will fail for API >= 24
                bmpUri = Uri.fromFile(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    public static void startShare(final FragmentActivity activity, final Commodity commodity) {
        FragmentManager fm = activity.getSupportFragmentManager();
        ShareDialogFragment fragment = ShareDialogFragment.newInstance(commodity);
        fragment.show(fm, "ShareDialogFragment");
    }

}
