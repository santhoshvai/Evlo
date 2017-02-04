package info.santhosh.evlo.ui.favorites;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;

import info.santhosh.evlo.R;
import info.santhosh.evlo.data.CommodityContract;
import info.santhosh.evlo.data.FavoriteAddorRemoveAsyncTask;

/**
 * Created by santhoshvai on 04/02/2017.
 */

public class FavoritesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    RecyclerView mRecyclerView;

    private static final int COMMODITY_FAV_LOADER = 1;
    private static final DecimalFormat IndianCurrencyFormat = new DecimalFormat("##,##,###");
    CommodityFavAdapter commodityFavAdapter;

    // Specify the columns we need.
    private static final String[] COMMODITY_FAV_COLUMNS = {
            CommodityContract.CommodityDataEntry.TABLE_NAME + "." + CommodityContract.CommodityDataEntry._ID,
            CommodityContract.CommodityDataEntry.COLUMN_ARRIVAL_DATE,
            CommodityContract.CommodityDataEntry.COLUMN_MAX_PRICE,
            CommodityContract.CommodityDataEntry.COLUMN_MIN_PRICE,
            CommodityContract.CommodityDataEntry.COLUMN_MODAL_PRICE,
            CommodityContract.CommodityDataEntry.COLUMN_COMMODITY_NAME,
            CommodityContract.CommodityDataEntry.COLUMN_VARIETY,
            CommodityContract.CommodityDataEntry.COLUMN_STATE_NAME,
            CommodityContract.CommodityDataEntry.COLUMN_DISTRICT_NAME,
            CommodityContract.CommodityDataEntry.COLUMN_MARKET_NAME,
            CommodityContract.CommodityFavEntry.TABLE_NAME + "." + CommodityContract.CommodityFavEntry._ID
    };

    // These indices are tied to COMMODITY_FAV_COLUMNS.  If COMMODITY_DETAIL_COLUMNS change, these must change.
    static final int COL_COMMODITY_DETAIL_ID = 0;
    static final int COL_ARRIVAL_DATE = 1;
    static final int COL_MAX_PRICE = 2;
    static final int COL_MIN_PRICE = 3;
    static final int COL_MODAL_PRICE = 4;
    static final int COL_COMMODITY_NAME = 5;
    static final int COL_VARIETY = 6;
    static final int COL_STATE_NAME = 7;
    static final int COL_DISTRICT_NAME = 8;
    static final int COL_MARKET_NAME = 9;
    static final int COL_COMMODITY_FAV_ID = 10;

    public static FavoritesFragment newInstance() {
        FavoritesFragment f = new FavoritesFragment();
        Bundle args = new Bundle();
        f.setArguments(args);
        return f;
    }

    public FavoritesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.favorites_list, container, false);
        return rootView;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        commodityFavAdapter = new CommodityFavAdapter(getActivity());
        getLoaderManager().initLoader(COL_COMMODITY_FAV_ID, null, this);
        mRecyclerView = (RecyclerView) getView().findViewById(R.id.fav_rv);
        mRecyclerView.setAdapter(commodityFavAdapter);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // sort by last added
        String sortOrder = CommodityContract.CommodityFavEntry.TABLE_NAME +
                "." + CommodityContract.CommodityFavEntry.COLUMN_FAV_ID + " DESC";

        Uri commodityDataUri = CommodityContract.CommodityFavEntry.buildAllFavsCommodityDetails();


        return new CursorLoader(
                getActivity(),
                commodityDataUri,
                COMMODITY_FAV_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        commodityFavAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        commodityFavAdapter.swapCursor(null);
    }

    static class CommodityFavAdapter extends RecyclerView.Adapter<CommodityFavAdapter.ViewHolder> {

        private Cursor mCursor;
        final private Context mContext;

        /**
         * Cache of the children views for a commodity list item.
         */
        class ViewHolder extends RecyclerView.ViewHolder {
            final View mView;
            final TextView mVariety;
            final TextView mModalPrice;
            final TextView mState;
            final TextView mMarket;
            final ImageView mFav;
            final ImageView mShare;

            ViewHolder(View view) {
                super(view);
                this.mView = view;
                this.mVariety = (TextView) view.findViewById(R.id.text_variety);
                this.mModalPrice = (TextView) view.findViewById(R.id.text_modal_price);
                this.mState = (TextView) view.findViewById(R.id.text_state_name);
                this.mMarket = (TextView) view.findViewById(R.id.text_market_district);
                this.mFav = (ImageView) view.findViewById(R.id.favorite_icon);
                this.mShare = (ImageView) view.findViewById(R.id.share_icon);
            }
        }

        CommodityFavAdapter(Context context) {
            mContext = context;
        }

        @Override
        public CommodityFavAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if ( parent instanceof RecyclerView ) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.favorites_card_row, parent, false);
                return new ViewHolder(view);
            } else {
                throw new RuntimeException("Not bound to RecyclerViewSelection");
            }
        }

        @Override
        public void onBindViewHolder(final CommodityFavAdapter.ViewHolder holder, final int position) {
            mCursor.moveToPosition(position);
            final Resources res = mContext.getResources();

            // Read from cursor

            final int columnId = mCursor.getInt(COL_COMMODITY_DETAIL_ID);
            final String commodityName = mCursor.getString(COL_COMMODITY_NAME);
            Double modalPriceDouble = Double.valueOf(mCursor.getString(COL_MODAL_PRICE));

            final String modalPrice = IndianCurrencyFormat.format(modalPriceDouble);
            final String date = mCursor.getString(COL_ARRIVAL_DATE);
            final String maxPrice = mCursor.getString(COL_MAX_PRICE);
            final String minPrice = mCursor.getString(COL_MIN_PRICE);
            final String district = mCursor.getString(COL_DISTRICT_NAME);
            final String market = mCursor.getString(COL_MARKET_NAME);
            final String state = mCursor.getString(COL_STATE_NAME);
            // variety name given is same as commodityName, then replace it as Normal
            final String variety = mCursor.getString(COL_VARIETY)
                    .equalsIgnoreCase(commodityName)? "Normal": mCursor.getString(COL_VARIETY);
            final String nameAndVariety = res.getString(R.string.favorite_name_data, commodityName, variety);

            String modal_price_text = res.getString(R.string.modal_price, modalPrice);
            String market_text = String.format(res.getString(R.string.market), market, district);
            holder.mModalPrice.setText(Html.fromHtml(modal_price_text));
            holder.mMarket.setText(market_text);
            holder.mState.setText(state);
            holder.mVariety.setText(nameAndVariety);


            holder.mFav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setSelected(!v.isSelected());
                    new FavoriteAddorRemoveAsyncTask(v.getContext(), v.isSelected()).execute(columnId);
                }
            });

            holder.mShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: share an image if you can
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    String share = res.getString(R.string.share_data,
                            commodityName, variety, modalPrice, market, district, state);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, share);
                    sendIntent.setType("text/pl" +
                            "" +
                            "ain");
                    v.getContext().startActivity(Intent.createChooser(sendIntent,
                            res.getString(R.string.share_heading)));
                }
            });
        }

        @Override
        public int getItemCount() {
            if ( null == mCursor ) return 0;
            return mCursor.getCount();
        }


        public void swapCursor(Cursor newCursor) {
            mCursor = newCursor;
            notifyDataSetChanged();
        }

        public Cursor getCursor() {
            return mCursor;
        }

    }
}
