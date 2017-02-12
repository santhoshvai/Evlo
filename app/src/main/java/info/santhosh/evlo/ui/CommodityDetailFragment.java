package info.santhosh.evlo.ui;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import info.santhosh.evlo.R;
import info.santhosh.evlo.common.RecyclerViewUtil;
import info.santhosh.evlo.data.CommodityContract;

import static info.santhosh.evlo.data.dbModels.Commodity.COMMODITY_DETAIL_COLUMNS;

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

    private String mCommodityName;
    private RecyclerViewUtil.CommodityDetailAdapter mCommodityDetailAdapter;

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
                 activity.setTitle(mCommodityName);
             }
             mCommodityDetailAdapter = new RecyclerViewUtil.CommodityDetailAdapter(getActivity(), RecyclerViewUtil.COMMODITY_VARIETY_DETAIL);
             getLoaderManager().initLoader(COMMODITY_DETAIL_LOADER, null, this);
         }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.commodity_detail, container, false);
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.commodity_detail_list);
        recyclerView.setAdapter(mCommodityDetailAdapter);
        // set item decoration
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                linearLayoutManager.getOrientation());
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(recyclerView.getContext(), R.drawable.divider_grey));
        recyclerView.addItemDecoration(dividerItemDecoration);

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
        new RecyclerViewUtil.CursorToListAsyncTask(data, mCommodityDetailAdapter).execute();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
