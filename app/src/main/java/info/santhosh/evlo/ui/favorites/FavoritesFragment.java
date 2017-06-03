package info.santhosh.evlo.ui.favorites;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import info.santhosh.evlo.R;
import info.santhosh.evlo.common.Utils;
import info.santhosh.evlo.data.CommodityContract;
import info.santhosh.evlo.data.FavoriteAddorRemoveAsyncTask;
import info.santhosh.evlo.data.dbModels.Commodity;
import info.santhosh.evlo.widget.EmptyRecyclerView;

import static info.santhosh.evlo.data.dbModels.Commodity.COMMODITY_DETAIL_COLUMNS;

/**
 * Created by santhoshvai on 04/02/2017.
 */

public class FavoritesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    EmptyRecyclerView mRecyclerView;

    private static final int COMMODITY_FAV_LOADER = 1;
    CommodityDetailAdapter commodityFavAdapter;

    private static final String BUNDLE_RECYCLER_LAYOUT = "FavoritesFragment.recycler.layout";

    public static FavoritesFragment newInstance() {
        FavoritesFragment f = new FavoritesFragment();
        Bundle args = new Bundle();
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.favorites_list, container, false);
        commodityFavAdapter = new CommodityDetailAdapter();
        getLoaderManager().initLoader(COMMODITY_FAV_LOADER, null, this);
        mRecyclerView = (EmptyRecyclerView) rootView.findViewById(R.id.fav_rv);
        mRecyclerView.setProgressView(rootView.findViewById(R.id.progressBar));
        mRecyclerView.setEmptyView(rootView.findViewById(R.id.bookmark_empty));
        mRecyclerView.setAdapter(commodityFavAdapter);
        mRecyclerView.setShouldAnimate(true);
        // set item decoration
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                linearLayoutManager.getOrientation());
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.divider_grey));
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        return rootView;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState != null) {
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
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // sort by last added
        String sortOrder = CommodityContract.CommodityFavEntry.TABLE_NAME +
                "." + CommodityContract.CommodityFavEntry._ID + " DESC";

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
        new CursorToListAsyncTask(data, this).execute();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        commodityFavAdapter.setList(null);
    }

    private static class CommodityDetailAdapter extends RecyclerView.Adapter<CommodityDetailAdapter.ViewHolder> {

        private List<Commodity> mCommodityList = null;

        /**
         * Cache of the children views for a commodity list item.
         */
        public class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mVariety;
            final TextView mModalPrice;
            final TextView mState;
            final TextView mMarket;
            final TextView mFav;
            final TextView mShare;

            ViewHolder(View view) {
                super(view);
                this.mVariety = (TextView) view.findViewById(R.id.text_variety);
                this.mModalPrice = (TextView) view.findViewById(R.id.text_modal_price);
                this.mState = (TextView) view.findViewById(R.id.text_state_name);
                this.mMarket = (TextView) view.findViewById(R.id.text_market_district);
                this.mFav = (TextView) view.findViewById(R.id.favorite_icon);
                this.mShare = (TextView) view.findViewById(R.id.share_icon);
            }
        }

        @Override
        public CommodityDetailAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.favorites_card_row, parent, false);
            final CommodityDetailAdapter.ViewHolder vh = new ViewHolder(view);
            vh.mFav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int pos = vh.getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        // user removed from favs
                        final Commodity commodity = mCommodityList.get(pos);
                        if (commodity.isFavorite()) {
                            commodity.setFavorite(false);
                            vh.mFav.setSelected(false);

                            Snackbar.make(v, R.string.fav_removed, Snackbar.LENGTH_LONG)
                                    .setAction(R.string.undo, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            commodity.setFavorite(true);
                                            vh.mFav.setSelected(true);
                                        }
                                    })
                                    .addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                                        @Override
                                        public void onDismissed(Snackbar transientBottomBar, int event) {
                                            super.onDismissed(transientBottomBar, event);
                                            if (!commodity.isFavorite()) {
                                                new FavoriteAddorRemoveAsyncTask(
                                                        transientBottomBar.getContext(),
                                                        false)
                                                        .execute(commodity.getId());
                                            }
                                        }
                                    })
                                    .show();
                        }
                    }
                }
            });
            vh.mShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: share an image if you can
                    final Resources res = vh.itemView.getContext().getResources();
                    int pos = vh.getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        Commodity commodity = mCommodityList.get(pos);
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        final String commodityName = commodity.getCommodity();
                        final String modalPrice = commodity.getModal_Price();
                        final String district = commodity.getDistrict();
                        final String market = commodity.getMarket();
                        final String state = commodity.getState();
                        final String variety = commodity.getVariety();
                        String share = v.getContext().getResources().getString(R.string.share_data,
                                commodityName, variety, modalPrice, market, district, state);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, share);
                        sendIntent.setType("text/plain");
                        vh.itemView.getContext().startActivity(Intent.createChooser(sendIntent,
                                res.getString(R.string.share_heading)));
                    }
                }
            });
            return vh;
        }

        private void animateFavoriteSelect(final CommodityDetailAdapter.ViewHolder holder, boolean shouldSelect) {
            // TODO: animate the icon fill instead of the zoom animation below
            holder.mFav.setSelected(shouldSelect);

            if(shouldSelect && Utils.isAPI21Plus()) { // not animating in api <21 due to cost of no render thread
                holder.mFav.animate().scaleX(1.3f).scaleY(1.3f).setDuration(100).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        holder.mFav.animate().scaleX(1f).scaleY(1f).setDuration(100);
                    }
                });
            }
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
            if (payloads.isEmpty()) {
                onBindViewHolder(holder, position);
            } else {
                Commodity commodity = mCommodityList.get(position);
                if (payloads.contains(CommodityDiffCallback.FAVORITE_PAYLOAD)) {
                    animateFavoriteSelect(holder, commodity.isFavorite());
                }
            }
        }

        @Override
        public void onBindViewHolder(final CommodityDetailAdapter.ViewHolder holder, int position) {
            Commodity commodity = mCommodityList.get(position);
            final Resources res = holder.itemView.getContext().getResources();

            final String commodityName = commodity.getCommodity();
            final String modalPrice = commodity.getModal_Price();
            final String district = commodity.getDistrict();
            final String market = commodity.getMarket();
            final String state = commodity.getState();
            // variety name given is same as commodityName, then replace it as Normal
            final String variety = commodity.getVariety();

            holder.mFav.setSelected(commodity.isFavorite());

            String modal_price_text = res.getString(R.string.rupee_price, modalPrice);
            String market_text = res.getString(R.string.market, market, district);
            holder.mModalPrice.setText(Html.fromHtml(modal_price_text));
            holder.mMarket.setText(market_text);
            holder.mState.setText(state);

            // TODO: (More Button to show details) https://stackoverflow.com/questions/41464629/expand-collapse-animation-in-cardview
            holder.mVariety.setText(res.getString(R.string.commodity_name_and_variety, commodityName, variety));
        }

        @Override
        public int getItemCount() {
            if ( null == mCommodityList ) return 0;
            return mCommodityList.size();
        }

        @Override
        public void setHasStableIds(boolean hasStableIds) {
            super.setHasStableIds(true);
        }

        @Override
        public long getItemId(int position) {
            return mCommodityList.get(position).getId();
        }

        public void setList(List<Commodity> commodityList) {
            if (mCommodityList != null && commodityList != null) {
                mCommodityList.clear();
                mCommodityList.addAll(commodityList);
            }
            else {
                mCommodityList = commodityList;
            }
        }

        List<Commodity> getList() {
            return mCommodityList;
        }

    }

    private static class CommodityDiffCallback extends DiffUtil.Callback {
        private List<Commodity> mOld;
        private List<Commodity> mNew;
        static final int FAVORITE_PAYLOAD = 1;

        CommodityDiffCallback(List<Commodity> oldList, List<Commodity> newList) {
            mOld = oldList;
            mNew = newList;
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
            Commodity oldItem = mOld.get(oldItemPosition);
            Commodity newItem = mNew.get(newItemPosition);
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition){
            Commodity oldItem = mOld.get(oldItemPosition);
            Commodity newItem = mNew.get(newItemPosition);
            return oldItem.equals(newItem);
        }

        @Nullable
        @Override
        public Object getChangePayload(int oldItemPosition, int newItemPosition) {
            // TODO: add arrival date, price to equals and show arrival date in the UI (and use payload to update the arrival data and price)
            Commodity oldItem = mOld.get(oldItemPosition);
            Commodity newItem = mNew.get(newItemPosition);
            if(oldItem.isFavorite() != newItem.isFavorite()) {
                return FAVORITE_PAYLOAD;
            }
            // other changes must completely rebind the view, as many values will change (this must be due to new data from the api)
            return null;
        }
    }

    private static class CursorToListAsyncTask extends AsyncTask<String, Void, Pair<DiffUtil.DiffResult, ArrayList<Commodity>>> {
        Cursor mCursor;
        WeakReference<FavoritesFragment> favoritesFragmentWeakReference;

        CursorToListAsyncTask(Cursor cursor, FavoritesFragment favoritesFragment) {
            mCursor = cursor;
            favoritesFragmentWeakReference = new WeakReference<>(favoritesFragment);
        }

        @Override
        protected Pair<DiffUtil.DiffResult, ArrayList<Commodity>> doInBackground(String... params) {
            FavoritesFragment favoritesFragment = favoritesFragmentWeakReference.get();
            if(favoritesFragment == null) return null;

            final List<Commodity> oldCommodityList = favoritesFragment.commodityFavAdapter.getList();
            ArrayList<Commodity> newCommodityList = new ArrayList<>(mCursor.getCount());

            mCursor.moveToFirst();
            while(!mCursor.isAfterLast()) {
                newCommodityList.add(Commodity.fromCursor(mCursor));
                mCursor.moveToNext();
            }
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new CommodityDiffCallback(oldCommodityList, newCommodityList), false);
            return new Pair<>(diffResult, newCommodityList);
            // cursor close is handled by the cursor loader
        }

        @Override
        protected void onPostExecute(Pair<DiffUtil.DiffResult, ArrayList<Commodity>> pair) {
            FavoritesFragment favoritesFragment = favoritesFragmentWeakReference.get();
            if(favoritesFragment == null) return;

            favoritesFragment.mRecyclerView.hideProgressView();
            favoritesFragment.commodityFavAdapter.setList(pair.second);
            pair.first.dispatchUpdatesTo(favoritesFragment.commodityFavAdapter);
        }
    }
}
