package info.santhosh.evlo;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

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
public class CommodityListActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private CommodityAdapter mCommodityAdapter;

    private static final int COMMODITY_NAME_LOADER = 0;

    // Specify the columns we need.
    private static final String[] COMMODITY_NAME_COLUMNS = {
            CommodityContract.CommodityNameEntry.TABLE_NAME + "." + CommodityContract.CommodityNameEntry._ID,
            CommodityContract.CommodityNameEntry.COLUMN_VARIETY,
            CommodityContract.CommodityNameEntry.COLUMN_COMMODITY_NAME
    };

    // These indices are tied to COMMODITY_NAME_COLUMNS.  If COMMODITY_NAME_COLUMNS change, these must change.
    static final int COL_COMMODITY_ID = 0;
    static final int COL_VARIETY = 1;
    static final int COL_COMMODITY_NAME = 2;

    /**
     * Whether the service has already started
     */
    private boolean mServiceStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commodity_list);
        launchXmlService();

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

        mCommodityAdapter = new CommodityAdapter(this);
        getSupportLoaderManager().initLoader(COMMODITY_NAME_LOADER, null, this);

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
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        savedInstanceState.putBoolean("mServiceStarted", mServiceStarted);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);

        mServiceStarted = savedInstanceState.getBoolean("mServiceStarted");
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
        String sortOrder = CommodityContract.CommodityNameEntry.COLUMN_COMMODITY_NAME + " ASC";

        Uri commodityNameUri = CommodityContract.CommodityNameEntry.CONTENT_URI;

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
        SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        // The call to getSearchableInfo() obtains a SearchableInfo object that is created from the searchable configuration XML file.
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

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
            Toast.makeText(getApplicationContext(), "Refresh clicked", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
