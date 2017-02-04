package info.santhosh.evlo.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
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

import static info.santhosh.evlo.common.DetailCommodityColumnConstants.COL_ARRIVAL_DATE;
import static info.santhosh.evlo.common.DetailCommodityColumnConstants.COL_COMMODITY_DETAIL_ID;
import static info.santhosh.evlo.common.DetailCommodityColumnConstants.COL_COMMODITY_NAME;
import static info.santhosh.evlo.common.DetailCommodityColumnConstants.COL_DISTRICT_NAME;
import static info.santhosh.evlo.common.DetailCommodityColumnConstants.COL_MARKET_NAME;
import static info.santhosh.evlo.common.DetailCommodityColumnConstants.COL_MAX_PRICE;
import static info.santhosh.evlo.common.DetailCommodityColumnConstants.COL_MIN_PRICE;
import static info.santhosh.evlo.common.DetailCommodityColumnConstants.COL_MODAL_PRICE;
import static info.santhosh.evlo.common.DetailCommodityColumnConstants.COL_STATE_NAME;
import static info.santhosh.evlo.common.DetailCommodityColumnConstants.COL_VARIETY;
import static info.santhosh.evlo.common.DetailCommodityColumnConstants.COMMODITY_DETAIL_COLUMNS;

/**
 * A fragment representing a single Commodity detail screen.
 * This fragment is either contained in a {@link CommodityListActivity}
 * in two-pane mode (on tablets) or a {@link CommodityDetailActivity}
 * on handsets.
 */
public class CommodityDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "CommodityDetailFragment";

    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String COMMODITY_NAME = "commodity_name";

    private static final int COMMODITY_DETAIL_LOADER = 1;
    private static final DecimalFormat IndianCurrencyFormat = new DecimalFormat("##,##,###");


    private String mCommodityName;
    private CommodityDetailAdapter mCommodityDetailAdapter;

    private static final String BUNDLE_RECYCLER_LAYOUT = "CommodityDetailFragment.recycler.layout";

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CommodityDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

         if (getArguments().containsKey(COMMODITY_NAME)) {
             mCommodityName = getArguments().getString(COMMODITY_NAME);
             Activity activity = this.getActivity();

             TextView title = (TextView) activity.findViewById(R.id.toolbar_title);
             if (title != null) {
//                 title.setText(mCommodityName);
                 activity.setTitle(mCommodityName);
             }
             mCommodityDetailAdapter = new CommodityDetailAdapter(getActivity());
             getLoaderManager().initLoader(COMMODITY_DETAIL_LOADER, null, this);
//             getLoaderManager().enableDebugLogging(true);

         }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.commodity_detail, container, false);

//        if (mCommodityName != null) {
//            ((TextView) rootView.findViewById(R.id.commodity_detail_name)).setText(mCommodityName);
//        }
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.commodity_detail_list);
        recyclerView.setAdapter(mCommodityDetailAdapter);

        return rootView;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.commodity_detail_list);
        if(savedInstanceState != null)
        {
            Parcelable savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
            recyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        RecyclerView recyclerView = (RecyclerView) getView().findViewById(R.id.commodity_detail_list);
        outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, recyclerView.getLayoutManager().onSaveInstanceState());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String sortOrder = CommodityContract.CommodityDataEntry.COLUMN_VARIETY + " ASC";

        Uri commodityDataUri = CommodityContract.CommodityDataEntry.buildCommodityNameDetailUri(mCommodityName);


        return new CursorLoader(
                getActivity(),
                commodityDataUri,
                COMMODITY_DETAIL_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCommodityDetailAdapter.swapCursor(data);
//        Log.d(LOG_TAG, DatabaseUtils.dumpCursorToString(data));

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCommodityDetailAdapter.swapCursor(null);
    }

    class CommodityDetailAdapter extends RecyclerView.Adapter<CommodityDetailAdapter.ViewHolder> {

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

        CommodityDetailAdapter(Context context) {
            mContext = context;
        }

        @Override
        public CommodityDetailAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if ( parent instanceof RecyclerView ) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.commodity_detail_card_row, parent, false);
                return new ViewHolder(view);
            } else {
                throw new RuntimeException("Not bound to RecyclerViewSelection");
            }
        }

        @Override
        public void onBindViewHolder(final CommodityDetailAdapter.ViewHolder holder, final int position) {
            mCursor.moveToPosition(position);
            final Resources res = mContext.getResources();

            // Read from cursor
            // TODO: read if an entry is already stored as a favorite from cursor, cursor needs to give this info

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

            String modal_price_text = String.format(res.getString(R.string.modal_price), modalPrice);
            String market_text = String.format(res.getString(R.string.market), market, district);
            holder.mModalPrice.setText(Html.fromHtml(modal_price_text));
            holder.mMarket.setText(market_text);
            holder.mState.setText(state);
            holder.mVariety.setText(variety);


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
                    String share = String.format(res.getString(R.string.share_data),
                            commodityName, variety, modalPrice, market, district, state);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, share);
                    sendIntent.setType("text/plain");
                    startActivity(Intent.createChooser(sendIntent,
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
