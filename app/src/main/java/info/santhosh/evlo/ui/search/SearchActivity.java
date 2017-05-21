package info.santhosh.evlo.ui.search;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import info.santhosh.evlo.R;
import info.santhosh.evlo.common.ColorUtil;
import info.santhosh.evlo.common.Utils;
import info.santhosh.evlo.data.CommodityContract;
import info.santhosh.evlo.ui.CommodityDetailActivity;
import info.santhosh.evlo.ui.CommodityDetailFragment;

/**
 * Created by santhoshvai on 20/05/17.
 */

public class SearchActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private Searchbar mSearchBar;
    private boolean mTwoPane;
    private CommodityAdapter mCommodityAdapter;
    private RecyclerView mRecyclerView;

    private static final int COMMODITY_NAME_LOADER = 0;
    private static final String BUNDLE_RECYCLER_LAYOUT = "CommodityListActivity.recycler.layout";

    // Specify the columns we need.
    private static final String[] COMMODITY_NAME_COLUMNS = {
            CommodityContract.CommodityDataEntry.TABLE_NAME + "." + CommodityContract.CommodityDataEntry._ID,
            CommodityContract.CommodityDataEntry.COLUMN_VARIETY,
            CommodityContract.CommodityDataEntry.COLUMN_COMMODITY_NAME
    };


    // These indices are tied to COMMODITY_NAME_COLUMNS.  If COMMODITY_NAME_COLUMNS change, these must change.
    static final int COL_COMMODITY_ID = 0;
    static final int COL_VARIETY = 1;
    static final int COL_COMMODITY_NAME = 2;

    private String mSearchQuery = "";
    private int mSelectedItem = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mSearchBar = (Searchbar) findViewById(R.id.search_toolbar);
        setSupportActionBar(mSearchBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (findViewById(R.id.commodity_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-sw600dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        setupRecyclerView();
    }

    private void setupRecyclerView() {
        mCommodityAdapter = new CommodityAdapter(this, mSearchQuery);
        mRecyclerView = (RecyclerView) findViewById(R.id.commodity_list);
        mRecyclerView.setAdapter(mCommodityAdapter);
        // set item decoration
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                linearLayoutManager.getOrientation());
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(mRecyclerView.getContext(), R.drawable.divider_grey));
        mRecyclerView.addItemDecoration(dividerItemDecoration);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Sort order:  Ascending, by name.
        // TODO: sort by recently used
        String sortOrder = CommodityContract.CommodityDataEntry.COLUMN_COMMODITY_NAME + " ASC";

        Uri commodityNameUri = CommodityContract.CommodityDataEntry.buildAllCommodityNames();

        if(args != null) {
            //  search
            String name = args.getString("name","");
            return new CursorLoader(this,
                    CommodityContract.CommodityDataEntry.buildCommodityNameSearchUri(name),
                    COMMODITY_NAME_COLUMNS,
                    null,
                    null,
                    sortOrder);
        }

        return new CursorLoader(this,
                commodityNameUri,
                COMMODITY_NAME_COLUMNS,
                null,
                null,
                sortOrder);
    }

    // this is where the results you deliver appear
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCommodityAdapter.swapCursor(data);
    }

    // your chance to clean up any references to the now reset Loader data
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCommodityAdapter.swapCursor(null);
    }

    /*
    The adapter used for recycler view
     */
    class CommodityAdapter extends RecyclerView.Adapter<CommodityAdapter.ViewHolder> {

        private Cursor mCursor;
        final private Context mContext;
        final int mSearchHighlightColor;
        private String mFilterSearch;
        private int mSelectedPos = -1;
        private final ItemClickListener mItemClickListener;

        /**
         * Cache of the children views for a commodity list item.
         */
        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mCommodityNameView;
            final LinearLayout mLinearLayout;

            ViewHolder(View view) {
                super(view);
                mCommodityNameView = (TextView) view.findViewById(R.id.commodity_name);
                mLinearLayout = (LinearLayout) view.findViewById(R.id.commodity_row);
            }

        }

        CommodityAdapter(Context context, String filter) {
            mContext = context;
            mFilterSearch = filter;
            mSearchHighlightColor = ContextCompat.getColor(context, R.color.searchHighlight);
            mItemClickListener = new ItemClickListener();
        }



        @Override
        public CommodityAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if ( parent instanceof RecyclerView ) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.commodity_list_content, parent, false);
                final CommodityAdapter.ViewHolder vh = new ViewHolder(view);
                vh.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int position = vh.getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            if (mTwoPane) {
                                // set the greyish background indicating that this was clicked
                                ColorUtil.setListRowSelectionBackgroundColor(mContext, vh.mLinearLayout);
                                // reset the old selected position background
                                notifyItemChanged(mSelectedPos);
                                // change the selected position
                                mSelectedPos = position;
                            }
                            mCursor.moveToPosition(position);
                            final String commodityName = mCursor.getString(SearchActivity.COL_COMMODITY_NAME);
                            mItemClickListener.onClick(commodityName, mContext);
                        }
                    }
                });
                return vh;
            } else {
                throw new RuntimeException("Not bound to RecyclerViewSelection");
            }
        }

        @Override
        public void onBindViewHolder(final CommodityAdapter.ViewHolder holder, int position) {
            mCursor.moveToPosition(position);
            // Read from cursor
            final String commodityName = mCursor.getString(SearchActivity.COL_COMMODITY_NAME);

            if(mFilterSearch.length() > 0 ) { // this is while searching
                // highlight searched text - http://stackoverflow.com/a/23967561/3394023
                int startPos = commodityName.toLowerCase().indexOf(mFilterSearch.toLowerCase());
                int endPos = startPos + mFilterSearch.length();

                if (startPos != -1) // This should always be true, just a sanity check
                {
                    Spannable spannable = new SpannableString(commodityName);
                    spannable.setSpan(new ForegroundColorSpan(mSearchHighlightColor), startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    holder.mCommodityNameView.setText(spannable);
                }

            } else {
                holder.mCommodityNameView.setText(commodityName);
            }
            if (mTwoPane && (mSelectedPos == position)) { // on tablet rotation, we must retain the clicked color
                // set the greyish background indicating that this was clicked
                ColorUtil.setListRowSelectionBackgroundColor(mContext, holder.mLinearLayout);
            } else {
                ColorUtil.setTransparentBackgroundColor(holder.mLinearLayout);
            }
        }

        @Override
        public int getItemCount() {
            if ( null == mCursor ) return 0;
            return mCursor.getCount();
        }


        void swapCursor(Cursor newCursor) {
            mCursor = newCursor;
            notifyDataSetChanged();
        }

        public Cursor getCursor() {
            return mCursor;
        }

        void setmFilterSearch(String query) {
            mFilterSearch = query;
        }

        public String getFilterSearch() {
            return mFilterSearch;
        }

        @Override
        public void setHasStableIds(boolean hasStableIds) {
            super.setHasStableIds(hasStableIds);
        }

        @Override
        public long getItemId(int position) {
            return mCursor.getInt(COL_COMMODITY_ID);
        }
    }

    /**
     * The click listener for the recycler view items
     */
    private class ItemClickListener {
        void onClick(String commodityName, Context context) {
            // move to the detail activity/fragment
            if (mTwoPane) {
                Bundle arguments = new Bundle();
                arguments.putString(CommodityDetailFragment.COMMODITY_NAME, commodityName);
                CommodityDetailFragment fragment = new CommodityDetailFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.commodity_detail_container, fragment)
                        .commit();
                Utils.hideSoftKeyboard(SearchActivity.this);
            } else {
                Intent intent = new Intent(context, CommodityDetailActivity.class);
                intent.putExtra(CommodityDetailFragment.COMMODITY_NAME, commodityName);
                context.startActivity(intent);
            }
        }
    }

    @Override
    public void finish() {
        // when the user tries to finish the activity we have to animate the exit
        // let's start by hiding the keyboard so that the exit seems smooth
        Utils.hideSoftKeyboard(this);
        super.finish();
        // override the system pending transition as we are handling ourselves
        overridePendingTransition(0, 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
//        else if (item.getItemId() == R.id.action_clear) {
//            searchbar.clearText();
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }
}
