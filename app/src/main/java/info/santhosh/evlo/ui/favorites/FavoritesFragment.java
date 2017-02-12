package info.santhosh.evlo.ui.favorites;

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

import info.santhosh.evlo.R;
import info.santhosh.evlo.common.RecyclerViewUtil;
import info.santhosh.evlo.data.CommodityContract;

import static info.santhosh.evlo.data.dbModels.Commodity.COMMODITY_DETAIL_COLUMNS;

/**
 * Created by santhoshvai on 04/02/2017.
 */

public class FavoritesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    RecyclerView mRecyclerView;

    private static final int COMMODITY_FAV_LOADER = 1;
    RecyclerViewUtil.CommodityDetailAdapter commodityFavAdapter;

    private static final String BUNDLE_RECYCLER_LAYOUT = "FavoritesFragment.recycler.layout";

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
        if(savedInstanceState != null)
        {
            Parcelable savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_LAYOUT);
            mRecyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, mRecyclerView.getLayoutManager().onSaveInstanceState());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        commodityFavAdapter = new RecyclerViewUtil.CommodityDetailAdapter(getActivity(), RecyclerViewUtil.FAVORITES);
        getLoaderManager().initLoader(COMMODITY_FAV_LOADER, null, this);
        mRecyclerView = (RecyclerView) getView().findViewById(R.id.fav_rv);
        mRecyclerView.setAdapter(commodityFavAdapter);
        // set item decoration
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                linearLayoutManager.getOrientation());
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(mRecyclerView.getContext(), R.drawable.divider_grey));
        mRecyclerView.addItemDecoration(dividerItemDecoration);
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
                COMMODITY_DETAIL_COLUMNS,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        new RecyclerViewUtil.CursorToListAsyncTask(data, commodityFavAdapter).execute();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
