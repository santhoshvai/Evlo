package info.santhosh.evlo;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;

import info.santhosh.evlo.Services.GetXmlService;
import info.santhosh.evlo.data.CommodityContract;

/**
 * An activity representing a list of Commodities. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link CommodityDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class CommodityListActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private CommodityAdapter mCommodityAdapter;

    private static final int COMMODITY_NAME_LOADER = 0;
    private static final String LOG_TAG = "Main_Activity";
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

    /**
     * Whether the service has already started
     */
    private boolean mServiceStarted = false;

    private String mSearchQuery = "";
    private boolean mSearchViewExpanded = false;
    private int mSelectedItem = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commodity_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        final String apiKey = BuildConfig.DATA_GOV_IN_API_KEY;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "ApiKey: " + apiKey, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mCommodityAdapter = new CommodityAdapter(this, mSearchQuery);
        getSupportLoaderManager().initLoader(COMMODITY_NAME_LOADER, null, this);
        getSupportLoaderManager().enableDebugLogging(true);

        View recyclerView = findViewById(R.id.commodity_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        if (findViewById(R.id.commodity_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        // we set the background for viewgroup in xml, no need for window background
        getWindow().setBackgroundDrawable(null);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        // Save whether the service has already been started
        outState.putBoolean("mServiceStarted", mServiceStarted);
        outState.putString("mSearchQuery", mSearchQuery);
        outState.putBoolean("mSearchViewExpanded", mSearchViewExpanded);
        outState.putInt("mSelectedItem", mSelectedItem);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.commodity_list);
        outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, recyclerView.getLayoutManager().onSaveInstanceState());

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(outState);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);

        mServiceStarted = savedInstanceState.getBoolean("mServiceStarted");
        launchXmlService();

        mSearchQuery = savedInstanceState.getString("mSearchQuery","");
        mSearchViewExpanded = savedInstanceState.getBoolean("mSearchViewExpanded");
        Parcelable savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.commodity_list);
        recyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);

        mSelectedItem = savedInstanceState.getInt("mSelectedItem", -1);
        mCommodityAdapter.mSelectedPos = mSelectedItem;
        recyclerView.getLayoutManager().scrollToPosition(mSelectedItem);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(mCommodityAdapter);
    }

    // to startup the service
    private void launchXmlService() {
        if(!mServiceStarted) {
            // Construct our Intent specifying the Service
            Intent i = new Intent(this, GetXmlService.class);
            // Add extras to the bundle

            // Start the service
            startService(i);
            mServiceStarted = true;
        }
    }

    // need to use cursor loader: https://github.com/santhoshvai/Android_Notes/blob/master/SQLITE_USAGE/5_Loaders.md#using-cursor-adapter
    // here’s where you construct the actual Loader instance
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Sort order:  Ascending, by name.
        // TODO: sort by recently used
        String sortOrder = CommodityContract.CommodityDataEntry.COLUMN_COMMODITY_NAME + " ASC";

        Uri commodityNameUri = CommodityContract.CommodityDataEntry.buildAllCommodityNames();

        if(args != null) {
            String name = args.getString("name","");
            Log.d(LOG_TAG, "onCreateLoader: "+ name);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // http://developer.android.com/training/search/setup.html
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        MenuItem searchItem = menu.findItem(R.id.search_commodities);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE); // occupy full width
        // The call to getSearchableInfo() obtains a SearchableInfo object that is created from the searchable configuration XML file.
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        // This sets the cursor blink to be White
        final EditText searchTextView = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        try {
            Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            mCursorDrawableRes.setAccessible(true);
            mCursorDrawableRes.set(searchTextView, 0);
        } catch (Exception e) {}

        // if previous searchQuery is present - due to configuration changes
        if (mSearchViewExpanded) {
            searchItem.expandActionView();
            searchView.setQuery(mSearchQuery, false);
            ((FloatingActionButton) findViewById(R.id.fab)).setVisibility(View.GONE); // hide fab
            if(mSearchQuery.length() >0) {
                // preserve the highlight
                mCommodityAdapter.setmFilterSearch(mSearchQuery);
                mCommodityAdapter.notifyDataSetChanged();
            }
            // searchView.clearFocus(); // hide keyboard
        }

        searchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextChange(String query) {
                        Bundle args = new Bundle();
                        query = query.trim();
                        args.putString("name", query);
                        mSearchQuery = query;
                        getSupportLoaderManager().restartLoader(COMMODITY_NAME_LOADER, args, CommodityListActivity.this);
                        mCommodityAdapter.setmFilterSearch(query);
                        return false;
                    }
                    @Override
                    public boolean onQueryTextSubmit(String query) { return false; }
                }
        );

        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.search_commodities),
                new MenuItemCompat.OnActionExpandListener() {
                    @Override
                    public boolean onMenuItemActionExpand(MenuItem item) {
                        // hide FAB
                        ((FloatingActionButton) findViewById(R.id.fab)).setVisibility(View.GONE);
                        mSearchViewExpanded = true;
                        return true; //true if item should expand
                    }

                    @Override
                    public boolean onMenuItemActionCollapse(MenuItem item) {
                        // FAB should re-appear
                        ((FloatingActionButton) findViewById(R.id.fab)).setVisibility(View.VISIBLE);
                        mSearchQuery = "";
                        mSearchViewExpanded = false;
                        return true; //true if item should collapse
                    }
                }
        );

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            mServiceStarted = false;
            launchXmlService();
            Toast.makeText(getApplicationContext(), "Data refresh started..", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class CommodityAdapter extends RecyclerView.Adapter<CommodityAdapter.ViewHolder> {

        private Cursor mCursor;
        final private Context mContext;
        private String mFilterSearch;
        private int mSelectedPos = -1;

        /**
         * Cache of the children views for a commodity list item.
         */
        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mCommodityNameView;
            public final LinearLayout mLinearLayout;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mCommodityNameView = (TextView) view.findViewById(R.id.commodity_name);
                mLinearLayout = (LinearLayout) view.findViewById(R.id.commodity_row);

                // Handle item click and set the selection
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
            }

        }

        public CommodityAdapter(Context context, String filter) {
            mContext = context;
            mFilterSearch = filter;
        }

        @Override
        public CommodityAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if ( parent instanceof RecyclerView ) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.commodity_list_content, parent, false);
                return new ViewHolder(view);
            } else {
                throw new RuntimeException("Not bound to RecyclerViewSelection");
            }
        }

        @Override
        public void onBindViewHolder(final CommodityAdapter.ViewHolder holder, final int position) {
            mCursor.moveToPosition(position);
            // Read from cursor

            final String commodityName = mCursor.getString(CommodityListActivity.COL_COMMODITY_NAME);

            if(mFilterSearch.length() > 0 ) {
                // highlight searched text - http://stackoverflow.com/a/23967561/3394023
                int startPos = commodityName.toLowerCase().indexOf(mFilterSearch.toLowerCase());
                int endPos = startPos + mFilterSearch.length();

                if (startPos != -1) // This should always be true, just a sanity check
                {
                    Spannable spannable = new SpannableString(commodityName);
                    // #4fc3f7 Color
                    spannable.setSpan(new ForegroundColorSpan(Color.rgb(79,195,247)), startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    holder.mCommodityNameView.setText(spannable);
                }

            } else {
                holder.mCommodityNameView.setText(commodityName);
            }

            // highlight selected item of the recyclerview for tablets
            if(mTwoPane && (mSelectedPos == position)) {
                mSelectedItem = position; // to remember upon rotation
                holder.mLinearLayout.setBackgroundColor(
                        ContextCompat.getColor(mContext, R.color.colorListRowSelect));
            } else {
                holder.mLinearLayout.setBackgroundColor(Color.TRANSPARENT);
            }
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        // Redraw the old selection and the new
                        notifyItemChanged(mSelectedPos);
                        mSelectedPos = position;
                        notifyItemChanged(mSelectedPos);

                        Bundle arguments = new Bundle();
                        arguments.putString(CommodityDetailFragment.COMMODITY_NAME, commodityName);
                        CommodityDetailFragment fragment = new CommodityDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.commodity_detail_container, fragment)
                                .commit();
                        Utils.hideSoftKeyboard(CommodityListActivity.this);
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, CommodityDetailActivity.class);
                        intent.putExtra(CommodityDetailFragment.COMMODITY_NAME, commodityName);

                        context.startActivity(intent);
                    }
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

        public void setmFilterSearch(String query) {
            mFilterSearch = query;
        }
    }

}
