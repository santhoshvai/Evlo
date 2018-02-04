package info.santhosh.evlo.ui.search;


import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import info.santhosh.evlo.R;
import info.santhosh.evlo.common.ColorUtil;
import info.santhosh.evlo.common.Utils;
import info.santhosh.evlo.data.CommodityContract;
import info.santhosh.evlo.ui.detail.CommodityDetailActivity;
import info.santhosh.evlo.widget.EmptyRecyclerView;
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;

import static info.santhosh.evlo.ui.search.SearchActivity.CommodityAdapter.SEARCH_HIGHLIGHT_PAYLOAD;
import static info.santhosh.evlo.ui.search.SearchActivity.CommodityName.COMMODITY_NAME_COLUMNS;

/**
 * Created by santhoshvai on 20/05/17.
 */

public class SearchActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, Searchbar.OnTextChangedCallback, EmptyRecyclerView.SetEmptyViewCallback {

    private static final String ARGS_NAME_KEY = "name";
    private Searchbar mSearchBar;
    private CommodityAdapter mCommodityAdapter;
    private EmptyRecyclerView mRecyclerView;

    private static final int COMMODITY_NAME_LOADER = 0;
    private static final String BUNDLE_RECYCLER_LAYOUT = "CommodityListActivity.recycler.layout";

    private String mFilterSearch = null;
    private FirebaseAnalytics mFirebaseAnalytics;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mSearchBar = (Searchbar) findViewById(R.id.search_toolbar);
        mSearchBar.setTextChangedListener(this);
        setSupportActionBar(mSearchBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupRecyclerView();
        getSupportLoaderManager().initLoader(COMMODITY_NAME_LOADER, null, this);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mSearchBar.setTextChangedListener(this);
        mRecyclerView.setEmptyViewCallback(this);
    }

    @Override
    protected void onStop() {
        mSearchBar.setTextChangedListener(null);
        mRecyclerView.setEmptyViewCallback(null);
        super.onStop();
    }

    @Override
    public void onSearchChange(CharSequence s) {
        if (mRecyclerView.getItemAnimator() instanceof SlideInUpAnimator) {
            // keep slide up animation just on first load
            // slide up animator causes crash when we notify item changes
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        }
        String query = s.toString().trim();
        Bundle args = null;
        if(!TextUtils.isEmpty(s)) {
            args = new Bundle();
            args.putString(ARGS_NAME_KEY, query);
        }
        mFilterSearch = query;
        mRecyclerView.scrollToPosition(0);
        if (mCommodityAdapter.getItemCount() == 0) {
            // none of the data may change in the adapter
            mRecyclerView.updateEmptyView();
        }
        getSupportLoaderManager().restartLoader(COMMODITY_NAME_LOADER, args, SearchActivity.this);
    }

    @Override
    public void setEmptyView(View emptyView) {
        if(!TextUtils.isEmpty(mFilterSearch)) { // search did not yield results
            ((TextView) emptyView).setText(getString(R.string.no_search_results, mFilterSearch));
        }
    }

    private void setupRecyclerView() {
        mCommodityAdapter = new CommodityAdapter();
        mRecyclerView = (EmptyRecyclerView) findViewById(R.id.commodity_list);
        mRecyclerView.setProgressView(findViewById(R.id.progressBarSearch));
        mRecyclerView.setEmptyView(findViewById(R.id.empty_text_view));
        mRecyclerView.setEmptyViewCallback(new EmptyRecyclerView.SetEmptyViewCallback() {
            @Override
            public void setEmptyView(View emptyView) {
                if(!TextUtils.isEmpty(mFilterSearch)) { // search did not yield results
                    ((TextView) emptyView).setText(getString(R.string.no_search_results, mFilterSearch));
                }
            }
        });
        mRecyclerView.setAdapter(mCommodityAdapter);
        // set item decoration
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                linearLayoutManager.getOrientation());
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(mRecyclerView.getContext(), R.drawable.divider_grey));
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        if (Utils.isAPI21Plus()) { // performance was awful on lower apis
            SlideInUpAnimator animator = new SlideInUpAnimator(new OvershootInterpolator(1f));
            animator.setAddDuration(300);
            animator.setRemoveDuration(300);
            mRecyclerView.setItemAnimator(animator);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        mRecyclerView.showProgressView();
        // Sort order:  Ascending, by name.
        // TODO: sort by recently used
        String sortOrder = CommodityContract.CommodityDataEntry.COLUMN_COMMODITY_NAME + " ASC";

        Uri commodityNameUri = CommodityContract.CommodityDataEntry.buildAllCommodityNames();

        if(args != null) {
            //  search
            String name = args.getString(ARGS_NAME_KEY, "");
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

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        new CursorToListAsyncTask(data, this).execute();
    }

    // clean up any references to the now reset Loader data
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCommodityAdapter.setList(null);
    }

    class CommodityAdapter extends RecyclerView.Adapter<CommodityAdapter.ViewHolder> {

        private List<CommodityName> mCommodityList = new ArrayList<>();
        final int mSearchHighlightColor;
        private String mFilterSearch = "";
        private final ItemClickListener mItemClickListener;

        static final int SEARCH_HIGHLIGHT_PAYLOAD = 1;

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mCommodityNameView;
            final LinearLayout mLinearLayout;

            ViewHolder(View view) {
                super(view);
                mCommodityNameView = (TextView) view.findViewById(R.id.commodity_name);
                mLinearLayout = (LinearLayout) view.findViewById(R.id.commodity_row);
            }

        }

        CommodityAdapter() {
            mSearchHighlightColor = ContextCompat.getColor(SearchActivity.this, R.color.searchHighlight);
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
            ColorUtil.setTransparentBackgroundColor(holder.mLinearLayout);
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
            return mCommodityList.get(position).getId();
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
        WeakReference<SearchActivity> searchActivityWeakReference;
        String mFilterSearch;

        CursorToListAsyncTask(Cursor cursor, SearchActivity activity) {
            mCursor = cursor;
            searchActivityWeakReference = new WeakReference<>(activity);
            mFilterSearch = activity.mFilterSearch;
        }

        @Override
        protected Pair<DiffUtil.DiffResult, ArrayList<CommodityName>> doInBackground(String... params) {
            SearchActivity searchActivity = searchActivityWeakReference.get();
            if(searchActivity == null) return null;

            final List<CommodityName> oldCommodities = searchActivity.mCommodityAdapter.getList();
            ArrayList<CommodityName> newCommodities = new ArrayList<>(mCursor.getCount());
            mCursor.moveToFirst();
            while(!mCursor.isAfterLast()) {
                newCommodities.add(SearchActivity.CommodityName.fromCursor(mCursor));
                mCursor.moveToNext();
            }
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                    new CommodityDiffCallback(oldCommodities, newCommodities,
                            searchActivity.mCommodityAdapter.getFilterSearch(), mFilterSearch), false);
            return new Pair<>(diffResult, newCommodities);
            // cursor close is handled by the cursor loader
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Pair<DiffUtil.DiffResult, ArrayList<CommodityName>> pair) {
            SearchActivity searchActivity = searchActivityWeakReference.get();
            if(searchActivity == null) return;

            // TODO: the empty check is to not hide the bar on the first ever search fragment call, what if the user had no internet and nothing is displayed?
            searchActivity.mCommodityAdapter.setList(pair.second);
            searchActivity.mCommodityAdapter.setmFilterSearch(mFilterSearch);
            pair.first.dispatchUpdatesTo(searchActivity.mCommodityAdapter);
            searchActivity.mRecyclerView.hideProgressView();
        }
    }

    /**
     * The click listener for the recycler view items
     */
    private class ItemClickListener {
        void onClick(String commodityName) {
            // move to the detail activity/fragment
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, commodityName);
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "search item");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
            startActivity(CommodityDetailActivity.getIntent(SearchActivity.this, commodityName));
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            overridePendingTransition(0, 0);
            return true;
        } else if (item.getItemId() == R.id.action_clear) {
            mSearchBar.clearText();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
