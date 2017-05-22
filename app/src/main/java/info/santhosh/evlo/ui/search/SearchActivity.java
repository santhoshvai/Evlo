package info.santhosh.evlo.ui.search;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import info.santhosh.evlo.R;
import info.santhosh.evlo.common.ColorUtil;
import info.santhosh.evlo.common.Utils;
import info.santhosh.evlo.data.CommodityContract;
import info.santhosh.evlo.ui.CommodityDetailActivity;
import info.santhosh.evlo.ui.CommodityDetailFragment;

import static info.santhosh.evlo.ui.search.SearchActivity.CommodityAdapter.SEARCH_HIGHLIGHT_PAYLOAD;
import static info.santhosh.evlo.ui.search.SearchActivity.CommodityName.COMMODITY_NAME_COLUMNS;

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

    private String mFilterSearch = null;
    private int mSelectedItem = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mSearchBar = (Searchbar) findViewById(R.id.search_toolbar);
        mSearchBar.setTextChanged(new Searchbar.onTextChanged() {
            @Override
            public void onSearchChange(CharSequence s) {
                String query = s.toString().trim();
//                mCommodityAdapter.setmFilterSearch(query);
                Bundle args = new Bundle();
                args.putString("name", query);
                getSupportLoaderManager().restartLoader(COMMODITY_NAME_LOADER, args, SearchActivity.this);
                mFilterSearch = query;
            }
        });
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
        getSupportLoaderManager().initLoader(COMMODITY_NAME_LOADER, null, this);
    }

    private void setupRecyclerView() {
        mCommodityAdapter = new CommodityAdapter(this, "");
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
        new CursorToListAsyncTask(data, mCommodityAdapter, mFilterSearch).execute();
    }

    // your chance to clean up any references to the now reset Loader data
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCommodityAdapter.setList(null);
    }

    /*
    The adapter used for recycler view
     */
    class CommodityAdapter extends RecyclerView.Adapter<CommodityAdapter.ViewHolder> {

        private List<CommodityName> mCommodityList = new ArrayList<>();
        final private Context mContext;
        final int mSearchHighlightColor;
        private String mFilterSearch;
        private int mSelectedPos = -1;
        private final ItemClickListener mItemClickListener;

        static final int SEARCH_HIGHLIGHT_PAYLOAD = 1;

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
                        final CommodityName commodity = mCommodityList.get(position);
                        mItemClickListener.onClick(commodity.getName());
                    }
                }
            });
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
            if (payloads.isEmpty()) {
                onBindViewHolder(holder, position);
            } else {
                final CommodityName commodity = mCommodityList.get(position);
                final String commodityName = commodity.getName();
                for (Object data : payloads) {
                    if ((int) data == SEARCH_HIGHLIGHT_PAYLOAD) {
                        setHighlightedTextIfNeeded(commodityName, holder.mCommodityNameView);
                    }
                }
            }
        }

        @Override
        public void onBindViewHolder(final CommodityAdapter.ViewHolder holder, int position) {
            final CommodityName commodity = mCommodityList.get(position);
            final String commodityName = commodity.getName();

            setHighlightedTextIfNeeded(commodityName, holder.mCommodityNameView);
            if (mTwoPane && (mSelectedPos == position)) { // on tablet rotation, we must retain the clicked color
                // set the greyish background indicating that this was clicked
                ColorUtil.setListRowSelectionBackgroundColor(mContext, holder.mLinearLayout);
            } else {
                ColorUtil.setTransparentBackgroundColor(holder.mLinearLayout);
            }
        }

        private void setHighlightedTextIfNeeded(String commodityName, TextView commodityNameView) {
            if(!TextUtils.isEmpty(mFilterSearch)) { // this is while searching
                // highlight searched text - http://stackoverflow.com/a/23967561/3394023
                int startPos = commodityName.toLowerCase().indexOf(mFilterSearch.toLowerCase());
                int endPos = startPos + mFilterSearch.length();

                if (startPos != -1) { // This should always be true, just a sanity check
                    Spannable spannable = new SpannableString(commodityName);
                    spannable.setSpan(new ForegroundColorSpan(mSearchHighlightColor), startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    commodityNameView.setText(spannable);
                }
            } else {
                commodityNameView.setText(commodityName);
            }
        }

        @Override
        public int getItemCount() {
            if ( null == mCommodityList ) return 0;
            return mCommodityList.size();
        }

        void setmFilterSearch(String query) {
            mFilterSearch = query;
        }

        String getFilterSearch() {
            return mFilterSearch;
        }

        @Override
        public void setHasStableIds(boolean hasStableIds) {
            super.setHasStableIds(true);
        }

        @Override
        public long getItemId(int position) {
            final CommodityName commodityName = mCommodityList.get(position);
            return commodityName.getId();
        }

        void setList(List<CommodityName> commodityList) {
            if (mCommodityList != null && commodityList != null) {
                mCommodityList.clear();
                mCommodityList.addAll(commodityList);
            } else {
                mCommodityList = commodityList;
            }
        }

        List<CommodityName> getList() {
            if (mCommodityList != null) {
                return mCommodityList;
            } else {
                return new ArrayList<>(0);
            }
        }
    }

    static class CommodityName {
        // Specify the columns we need.
        static final String[] COMMODITY_NAME_COLUMNS = {
                CommodityContract.CommodityDataEntry.TABLE_NAME + "." + CommodityContract.CommodityDataEntry._ID,
                CommodityContract.CommodityDataEntry.COLUMN_VARIETY,
                CommodityContract.CommodityDataEntry.COLUMN_COMMODITY_NAME
        };


        // These indices are tied to COMMODITY_NAME_COLUMNS.  If COMMODITY_NAME_COLUMNS change, these must change.
        static final int COL_COMMODITY_ID = 0;
        static final int COL_VARIETY = 1;
        static final int COL_COMMODITY_NAME = 2;

        private int id;
        private String name;
        private String variety;

        private CommodityName(int id, String name, String variety) {
            this.id = id;
            this.name = name;
            this.variety = variety;
        }

        private CommodityName(int id, String name) {
            this.id = id;
            this.name = name;
            this.variety = null;
        }

        static CommodityName fromCursor(Cursor cursor) {
            // Read from cursor
            return new CommodityName(cursor.getInt(COL_COMMODITY_ID), cursor.getString(COL_COMMODITY_NAME));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CommodityName that = (CommodityName) o;

            if (id != that.id) return false;
            if (!name.equals(that.name)) return false;
            return variety != null ? variety.equals(that.variety) : that.variety == null;

        }

        @Override
        public int hashCode() {
            int result = id;
            result = 31 * result + name.hashCode();
            result = 31 * result + (variety != null ? variety.hashCode() : 0);
            return result;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    private static class CommodityDiffCallback extends DiffUtil.Callback {
        private List<CommodityName> mOld;
        private List<CommodityName> mNew;
        private String mOldFilterSearch;
        private String mNewFilterSearch;

        CommodityDiffCallback(List<CommodityName> oldList, List<CommodityName> newList,
                              String oldFilterSearch, String newFilterSearch) {
            mOld = oldList;
            mNew = newList;
            mOldFilterSearch = oldFilterSearch;
            mNewFilterSearch = newFilterSearch;
        }
        @Override
        public int getOldListSize() {
            if ( null == mOld ) return 0;
            return mOld.size();
        }

        @Override
        public int getNewListSize() {
            if ( null == mNew ) return 0;
            return mNew.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            CommodityName oldItem = mOld.get(oldItemPosition);
            CommodityName newItem = mNew.get(newItemPosition);
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition){
            // due to new search-filter we must change the highlight
            return TextUtils.equals(mOldFilterSearch, mNewFilterSearch);
        }

        @Nullable
        @Override
        public Object getChangePayload(int oldItemPosition, int newItemPosition) {
            // since we have only search highlight change, send that as payload
            return SEARCH_HIGHLIGHT_PAYLOAD;
        }
    }

    private static class CursorToListAsyncTask extends AsyncTask<String, Void, Pair<DiffUtil.DiffResult, ArrayList<CommodityName>>> {

        Cursor mCursor;
        WeakReference<CommodityAdapter> commodityAdapterWeakReference;
        String mFilterSearch;

        public CursorToListAsyncTask(Cursor cursor, CommodityAdapter commodityAdapter, String filterSearch) {
            mCursor = cursor;
            commodityAdapterWeakReference = new WeakReference<>(commodityAdapter);
            mFilterSearch = filterSearch;
        }

        @Override
        protected Pair<DiffUtil.DiffResult, ArrayList<CommodityName>> doInBackground(String... params) {
            ArrayList<CommodityName> commodities = new ArrayList<>(mCursor.getCount());
            while (mCursor.moveToNext()) {
                commodities.add(SearchActivity.CommodityName.fromCursor(mCursor));
            }
            CommodityAdapter commodityAdapter = commodityAdapterWeakReference.get();
            if(commodityAdapter == null) return null;
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                    new CommodityDiffCallback(commodityAdapter.getList(), commodities,
                            commodityAdapter.getFilterSearch(), mFilterSearch), false);
            commodityAdapter.setList(commodities);
            return new Pair<>(diffResult, commodities);
            // cursor close is handled by the cursor loader
        }

        @Override
        protected void onPostExecute(Pair<DiffUtil.DiffResult, ArrayList<CommodityName>> pair) {
            CommodityAdapter commodityAdapter = commodityAdapterWeakReference.get();
            if(commodityAdapter == null) return;
            commodityAdapter.setList(pair.second);
            commodityAdapter.setmFilterSearch(mFilterSearch);
            pair.first.dispatchUpdatesTo(commodityAdapter);
        }
    }

    /**
     * The click listener for the recycler view items
     */
    private class ItemClickListener {
        void onClick(String commodityName) {
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
                Intent intent = new Intent(SearchActivity.this, CommodityDetailActivity.class);
                intent.putExtra(CommodityDetailFragment.COMMODITY_NAME, commodityName);
                startActivity(intent);
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
