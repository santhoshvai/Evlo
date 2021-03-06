package info.santhosh.evlo.ui.main;

import android.app.Activity;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.graphics.drawable.AnimatedVectorDrawableCompat;
import android.support.transition.TransitionManager;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdView;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import info.santhosh.evlo.R;
import info.santhosh.evlo.common.AdRequestor;
import info.santhosh.evlo.common.ShareDialogFragment;
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

    static final String TAG = "FavoritesFragment";
    private static final int COMMODITY_FAV_LOADER = 1;
    CommodityFavAdapter commodityFavAdapter;
    private AdView mAdView;
    boolean dataNotEmptyLoaded = false;
    boolean adLoaded = false;
    private AdRequestor adRequestor;

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
        commodityFavAdapter = new CommodityFavAdapter();
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
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(container.getContext(), R.drawable.divider_grey));

        mRecyclerView.addItemDecoration(dividerItemDecoration);
        mAdView = rootView.findViewById(R.id.adView);
        adRequestor = new AdRequestor(mAdView, new AdListener() {
            @Override
            public void onAdLoaded() {
                adLoaded = true;
                if (dataNotEmptyLoaded) {
                    mAdView.setVisibility(View.VISIBLE);
                }
            }
        });
        adRequestor.start();
        return rootView;
    }

    public void changeAdSettings() {
        adRequestor.showConsentForm();
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

    @NonNull
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
    public void onLoadFinished(@NonNull Loader<Cursor> loader, final Cursor data) {
        new CursorToListAsyncTask(data, this).execute();
        // show ads only when we have data
        dataNotEmptyLoaded = data.getCount() > 0;
        if (dataNotEmptyLoaded && adLoaded) {
            mAdView.setVisibility(View.VISIBLE);
        } else {
            mAdView.setVisibility(View.GONE);
        }
        // cursor close will be done by the framework
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        commodityFavAdapter.setList(null);
    }

    private class CommodityFavAdapter extends RecyclerView.Adapter<CommodityFavAdapter.ViewHolder> {

        private List<Commodity> mCommodityList = null;

        private int mIsExpandedPosition = -1;
        private final AnimatedVectorDrawableCompat avd_from_down_arrow;
        private final AnimatedVectorDrawableCompat avd_from_up_arrow;
        final Queue<List<Commodity>> pendingDiffUtilUpdates = new ArrayDeque<>();

        // dirty workaround for support library bug https://github.com/googlesamples/android-ConstraintLayoutExamples/issues/6
        private boolean shouldShowMorebutton = true;

        CommodityFavAdapter() {
            avd_from_down_arrow = AnimatedVectorDrawableCompat.create(getContext(), R.drawable.avd_from_down_arrow);
            avd_from_up_arrow = AnimatedVectorDrawableCompat.create(getContext(), R.drawable.avd_from_up_arrow);
        }

        /**
         * The ConstraintSet to use for the normal initial state
         */
        private ConstraintSet mConstraintSetNormal = new ConstraintSet();
        /**
         * ConstraintSet to be applied on the normal ConstraintLayout to make the Image bigger.
         */
        private ConstraintSet mConstraintSetBig = new ConstraintSet();

        /**
         * Cache of the children views for a commodity list item.
         */
        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mVariety;
            final TextView mModalPrice;
            final TextView mState;
            final TextView mMarket;
            final TextView mFav;
            final TextView mShare;
            final TextView mDetail;
            final TextView mArrivalDate;
            final TextView mMaxPrice;
            final TextView mMinPrice;

            ViewHolder(View view) {
                super(view);
                this.mVariety = (TextView) view.findViewById(R.id.text_variety_and_or_market);
                this.mModalPrice = (TextView) view.findViewById(R.id.text_modal_price);
                this.mState = (TextView) view.findViewById(R.id.text_state_name);
                this.mMarket = (TextView) view.findViewById(R.id.text_market_and_or_district);
                this.mFav = (TextView) view.findViewById(R.id.favorite_icon);
                this.mShare = (TextView) view.findViewById(R.id.share_icon);
                this.mDetail = (TextView) view.findViewById(R.id.details_icon);
                this.mArrivalDate = (TextView) view.findViewById(R.id.text_arrival_date);
                this.mMaxPrice = (TextView) view.findViewById(R.id.text_max_price);
                this.mMinPrice = (TextView) view.findViewById(R.id.text_min_price);
            }
        }

        RecyclerView mRecyclerView;


        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
            mRecyclerView = recyclerView;
        }


        @Override
        public CommodityFavAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.commodity_card_row, parent, false);
            mConstraintSetNormal.clone((ConstraintLayout) view);
            try {
                mConstraintSetBig.load(parent.getContext(), R.layout.commodity_card_row_more);
            } catch(Exception e) {
                Crashlytics.logException(e);
                shouldShowMorebutton = false;
            }

            final CommodityFavAdapter.ViewHolder vh = new ViewHolder(view);
            vh.mFav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int pos = vh.getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        // user removed from favs
                        final Commodity commodity = mCommodityList.get(pos);
                        if (commodity.isFavorite()) {
                            final int id = commodity.getId();
                            // no need to send uri to constructor here,
                            // because we use the favorite uri in the async task and that is automatically notified when there is a change
                            new FavoriteAddorRemoveAsyncTask(getContext(), false)
                                    .execute(id);

                            Snackbar.make(v, R.string.fav_removed, Snackbar.LENGTH_LONG)
                                    .setAction(R.string.undo, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            new FavoriteAddorRemoveAsyncTask(getContext(), true)
                                                    .execute(id);
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
                    int pos = vh.getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        ShareDialogFragment.startShare(getActivity(), mCommodityList.get(pos));
                    }
                }
            });
            vh.mDetail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = vh.getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        mIsExpandedPosition = (pos == mIsExpandedPosition) ? -1 : pos;
                        TransitionManager.beginDelayedTransition(mRecyclerView);
                        notifyDataSetChanged();
                    }
                }
            });
            return vh;
        }

        private void animateFavoriteSelect(final CommodityFavAdapter.ViewHolder holder, boolean shouldSelect) {
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
        public void onBindViewHolder(final CommodityFavAdapter.ViewHolder holder, int position) {
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

            String market_text = res.getString(R.string.market, market, district);
            holder.mMarket.setText(market_text);
            holder.mState.setText(state);
            holder.mVariety.setText(res.getString(R.string.commodity_name_and_variety, commodityName, variety));

            // (More Button to show details) https://stackoverflow.com/questions/41464629/expand-collapse-animation-in-cardview

            String modal_price_text;
            boolean isExpanded = position == mIsExpandedPosition;

            if (isExpanded) {
                final String maxPrice = res.getString(R.string.rupee_price_with_unit, commodity.getMax_Price());
                final String minPrice = res.getString(R.string.rupee_price_with_unit, commodity.getMin_Price());
                modal_price_text = res.getString(R.string.rupee_price_with_unit, modalPrice);
                final String arrivalDate = Utils.getArrivalDateString(commodity.getArrival_Date());
                holder.mArrivalDate.setText(arrivalDate);
                holder.mMaxPrice.setText(maxPrice);
                holder.mMinPrice.setText(minPrice);

                holder.mDetail.setCompoundDrawablesWithIntrinsicBounds(avd_from_down_arrow, null, null, null);
                avd_from_down_arrow.start();

                mConstraintSetBig.applyTo((ConstraintLayout) holder.itemView);
            } else {
                modal_price_text = res.getString(R.string.rupee_price, modalPrice);
                holder.mDetail.setCompoundDrawablesWithIntrinsicBounds(avd_from_up_arrow, null, null, null);

                mConstraintSetNormal.applyTo((ConstraintLayout) holder.itemView);
            }

            holder.mModalPrice.setText(Html.fromHtml(modal_price_text));

            if (!shouldShowMorebutton) {
                holder.mDetail.setVisibility(View.GONE);
            }
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

    private static class CursorToListAsyncTask extends AsyncTask<String, Void, DiffUtil.DiffResult> {
        WeakReference<FavoritesFragment> favoritesFragmentWeakReference;
        List<Commodity> newCommodityList;

        CursorToListAsyncTask(Cursor cursor, FavoritesFragment favoritesFragment) {
            favoritesFragmentWeakReference = new WeakReference<>(favoritesFragment);
            if (cursor != null) {
                newCommodityList = new ArrayList<>(cursor.getCount());
                cursor.moveToFirst();
                while(!cursor.isAfterLast()) {
                    newCommodityList.add(Commodity.fromCursor(cursor));
                    cursor.moveToNext();
                }
            }
        }

        CursorToListAsyncTask(List<Commodity> data, FavoritesFragment favoritesFragment) {
            favoritesFragmentWeakReference = new WeakReference<>(favoritesFragment);
            newCommodityList = data;
        }

        @Override
        protected DiffUtil.DiffResult doInBackground(String... params) {
            FavoritesFragment favoritesFragment = favoritesFragmentWeakReference.get();
            if(favoritesFragment == null) return null;
            if (newCommodityList == null) return null;

            final List<Commodity> oldCommodityList = favoritesFragment.commodityFavAdapter.getList();

            Queue<List<Commodity>> pendingDiffUtilUpdates = favoritesFragment.commodityFavAdapter.pendingDiffUtilUpdates;
            pendingDiffUtilUpdates.add(newCommodityList);
            if (pendingDiffUtilUpdates.size() > 1) {
                return null;
            }
            return DiffUtil.calculateDiff(new CommodityDiffCallback(oldCommodityList, newCommodityList), false);
        }

        @Override
        protected void onPostExecute(DiffUtil.DiffResult diffResult) {
            FavoritesFragment favoritesFragment = favoritesFragmentWeakReference.get();
            if(favoritesFragment == null) return;
            if (diffResult == null) {
                favoritesFragment.mRecyclerView.hideProgressView();
                return;
            }

            Queue<List<Commodity>> pendingDiffUtilUpdates = favoritesFragment.commodityFavAdapter.pendingDiffUtilUpdates;
            pendingDiffUtilUpdates.remove();
            favoritesFragment.commodityFavAdapter.setList(newCommodityList);
            diffResult.dispatchUpdatesTo(favoritesFragment.commodityFavAdapter);
            favoritesFragment.mRecyclerView.hideProgressView();
            if (pendingDiffUtilUpdates.size() > 0) {
                new CursorToListAsyncTask(pendingDiffUtilUpdates.peek(), favoritesFragment).execute();
            }
        }
    }
}
