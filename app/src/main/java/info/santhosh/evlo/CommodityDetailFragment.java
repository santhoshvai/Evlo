package info.santhosh.evlo;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import info.santhosh.evlo.data.CommodityContract;

/**
 * A fragment representing a single Commodity detail screen.
 * This fragment is either contained in a {@link CommodityListActivity}
 * in two-pane mode (on tablets) or a {@link CommodityDetailActivity}
 * on handsets.
 */
public class CommodityDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String COMMODITY_NAME = "commodity_name";

    private static final int COMMODITY_DETAIL_LOADER = 1;

    // Specify the columns we need.
    private static final String[] COMMODITY_DETAIL_COLUMNS = {
            CommodityContract.CommodityDataEntry.TABLE_NAME + "." + CommodityContract.CommodityDataEntry._ID,
            CommodityContract.CommodityDataEntry.COLUMN_ARRIVAL_DATE,
            CommodityContract.CommodityDataEntry.COLUMN_MAX_PRICE,
            CommodityContract.CommodityDataEntry.COLUMN_MIN_PRICE,
            CommodityContract.CommodityDataEntry.COLUMN_MODAL_PRICE,
            CommodityContract.CommodityDataEntry.COLUMN_COMMODITY_NAME,
            CommodityContract.CommodityDataEntry.COLUMN_VARIETY,
            CommodityContract.CommodityDataEntry.COLUMN_STATE_NAME,
            CommodityContract.CommodityDataEntry.COLUMN_DISTRICT_NAME,
            CommodityContract.CommodityDataEntry.COLUMN_MARKET_NAME
    };

    // These indices are tied to COMMODITY_DETAIL_COLUMNS.  If COMMODITY_DETAIL_COLUMNS change, these must change.
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

    private String mCommodityName;
    private CommodityDetailAdapter mCommodityDetailAdapter;

    public static final String LOG_TAG = "CommodityDetailFragment";
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
             CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
             if (appBarLayout != null) {
                 appBarLayout.setTitle(mCommodityName);
             }
//             mCommodityAdapter = new CommodityAdapter(this, mSearchQuery);
             mCommodityDetailAdapter = new CommodityDetailAdapter(getActivity());
             getLoaderManager().initLoader(COMMODITY_DETAIL_LOADER, null, this);
             getLoaderManager().enableDebugLogging(true);

         }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.commodity_detail, container, false);

        if (mCommodityName != null) {
            ((TextView) rootView.findViewById(R.id.commodity_detail_name)).setText(mCommodityName);
        }
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

    public class CommodityDetailAdapter extends RecyclerView.Adapter<CommodityDetailAdapter.ViewHolder> {

        private Cursor mCursor;
        final private Context mContext;

        /**
         * Cache of the children views for a commodity list item.
         */
        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mVariety;
            public final TextView mModalPrice;
            public final TextView mState;
            public final TextView mMarket;
            public final ImageView mFav;

            public ViewHolder(View view) {
                super(view);
                this.mView = view;
                this.mVariety = (TextView) view.findViewById(R.id.text_variety);
                this.mModalPrice = (TextView) view.findViewById(R.id.text_modal_price);
                this.mState = (TextView) view.findViewById(R.id.text_state_name);
                this.mMarket = (TextView) view.findViewById(R.id.text_market_district);
                this.mFav = (ImageView) view.findViewById(R.id.favorite_icon);
            }
        }

        public CommodityDetailAdapter(Context context) {
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
            Resources res = mContext.getResources();

            // Read from cursor

            final String commodityName = mCursor.getString(CommodityDetailFragment.COL_COMMODITY_NAME);
            final String modalPrice = mCursor.getString(CommodityDetailFragment.COL_MODAL_PRICE);
            final String date = mCursor.getString(CommodityDetailFragment.COL_ARRIVAL_DATE);
            final String maxPrice = mCursor.getString(CommodityDetailFragment.COL_MAX_PRICE);
            final String minPrice = mCursor.getString(CommodityDetailFragment.COL_MIN_PRICE);
            final String district = mCursor.getString(CommodityDetailFragment.COL_DISTRICT_NAME);
            final String market = mCursor.getString(CommodityDetailFragment.COL_MARKET_NAME);
            final String state = mCursor.getString(CommodityDetailFragment.COL_STATE_NAME);
            final String variety = mCursor.getString(CommodityDetailFragment.COL_VARIETY);

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
