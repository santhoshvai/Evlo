package info.santhosh.evlo;

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
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import info.santhosh.evlo.Services.GetXmlService;
import info.santhosh.evlo.data.CommodityContract;
import info.santhosh.evlo.dummy.DummyContent;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        launchXmlService();
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

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(DummyContent.ITEMS));
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        private final List<DummyContent.DummyItem> mValues;

        public SimpleItemRecyclerViewAdapter(List<DummyContent.DummyItem> items) {
            mValues = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.commodity_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mValues.get(position);
            holder.mContentView.setText(mValues.get(position).content);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(CommodityDetailFragment.ARG_ITEM_ID, holder.mItem.id);
                        CommodityDetailFragment fragment = new CommodityDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.commodity_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, CommodityDetailActivity.class);
                        intent.putExtra(CommodityDetailFragment.ARG_ITEM_ID, holder.mItem.id);

                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mContentView;
            public DummyContent.DummyItem mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mContentView = (TextView) view.findViewById(R.id.commodity_name);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }

    // to startup the service
    public void launchXmlService() {
        // Construct our Intent specifying the Service
        Intent i = new Intent(this, GetXmlService.class);
        // Add extras to the bundle

        // Start the service
        startService(i);
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

    }

    // your chance to clean up any references to the now reset Loader data
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}
