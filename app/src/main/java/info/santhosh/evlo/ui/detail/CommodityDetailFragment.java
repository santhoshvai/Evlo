package info.santhosh.evlo.ui.detail;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
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
import android.view.ViewParent;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.lang.ref.WeakReference;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import ca.barrenechea.widget.recyclerview.decoration.StickyHeaderAdapter;
import ca.barrenechea.widget.recyclerview.decoration.StickyHeaderDecoration;
import co.mobiwise.materialintro.prefs.PreferencesManager;
import co.mobiwise.materialintro.shape.Focus;
import co.mobiwise.materialintro.shape.FocusGravity;
import co.mobiwise.materialintro.shape.ShapeType;
import co.mobiwise.materialintro.view.MaterialIntroView;
import info.santhosh.evlo.R;
import info.santhosh.evlo.common.ShareDialogFragment;
import info.santhosh.evlo.common.Utils;
import info.santhosh.evlo.data.CommodityContract;
import info.santhosh.evlo.data.FavoriteAddorRemoveAsyncTask;
import info.santhosh.evlo.data.dbModels.Commodity;

import static info.santhosh.evlo.data.dbModels.Commodity.COMMODITY_DETAIL_COLUMNS;

public class CommodityDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "CommodityDetailFragment";

    static final String COMMODITY_NAME = "commodity_name";

    private static final int COMMODITY_DETAIL_LOADER = 1;

    private String mCommodityName;
    private CommodityDetailAdapter mCommodityDetailAdapter;
    RecyclerView mRecyclerView;
    private AdView mAdView;
    boolean dataNotEmptyLoaded = false;
    boolean adLoaded = false;

    private static final String BUNDLE_RECYCLER_LAYOUT = "CommodityDetailFragment.recycler.layout";

    public static CommodityDetailFragment newInstance(String commodityName) {
        Bundle arguments = new Bundle();
        arguments.putString(COMMODITY_NAME, commodityName);
        CommodityDetailFragment fragment = new CommodityDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (getArguments().containsKey(COMMODITY_NAME)) {
            mCommodityName = getArguments().getString(COMMODITY_NAME);
            Activity activity = this.getActivity();

            TextView title = (TextView) activity.findViewById(R.id.toolbar_title);
            if (title != null) {
                title.setText(mCommodityName);
            }
            mCommodityDetailAdapter = new CommodityDetailAdapter();
            getLoaderManager().initLoader(COMMODITY_DETAIL_LOADER, null, this);
        }

        View rootView = inflater.inflate(R.layout.fragment_commodity_detail, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.commodity_detail_list);
        // set item decoration
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(container.getContext(),
                LinearLayoutManager.VERTICAL);
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(container.getContext(), R.drawable.divider_grey));
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        mRecyclerView.setAdapter(mCommodityDetailAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new StickyHeaderDecoration(mCommodityDetailAdapter), 1);

        mAdView = rootView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                adLoaded = true;
                if (dataNotEmptyLoaded) {
                    mAdView.setVisibility(View.VISIBLE);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onDestroyView() {
        if (mAdView != null) {
            ViewParent parent = mAdView.getParent();
            if (parent != null && parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(mAdView);
            }
            mAdView.destroy();
            mAdView = null;
        }
        super.onDestroyView();
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
        mRecyclerView = (RecyclerView) getView().findViewById(R.id.commodity_detail_list);
        outState.putParcelable(BUNDLE_RECYCLER_LAYOUT, mRecyclerView.getLayoutManager().onSaveInstanceState());
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
        new CursorToListAsyncTask(data, mCommodityDetailAdapter).execute();
        // show ads only when we have data
        dataNotEmptyLoaded = data.getCount() > 0;
        if (dataNotEmptyLoaded && adLoaded) {
            mAdView.setVisibility(View.VISIBLE);
        } else {
            mAdView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCommodityDetailAdapter.setList(null);
    }

    private class CommodityDetailAdapter extends RecyclerView.Adapter<CommodityDetailAdapter.ViewHolder> implements
            StickyHeaderAdapter<CommodityDetailAdapter.HeaderHolder> {

        private List<Commodity> mCommodityList = null;
        private Map<String, Integer> mVarietyToHeaderId;
        private int mIsExpandedPosition = -1;

        // dirty workaround for support library bug https://github.com/googlesamples/android-ConstraintLayoutExamples/issues/6
        private boolean shouldShowMorebutton = true;

        private final AnimatedVectorDrawableCompat avd_from_down_arrow;
        private final AnimatedVectorDrawableCompat avd_from_up_arrow;
        final Queue<List<Commodity>> pendingDiffUtilUpdates = new ArrayDeque<>();

        CommodityDetailAdapter() {
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

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView mModalPrice;
            final TextView mState;
            final TextView mMarket;
            final TextView mDistrict;
            final TextView mFav;
            final TextView mShare;
            final TextView mDetail;
            final TextView mArrivalDate;
            final TextView mMaxPrice;
            final TextView mMinPrice;

            ViewHolder(View view) {
                super(view);
                this.mModalPrice = (TextView) view.findViewById(R.id.text_modal_price);
                this.mState = (TextView) view.findViewById(R.id.text_state_name);
                this.mMarket = (TextView) view.findViewById(R.id.text_variety_and_or_market);
                this.mDistrict = (TextView) view.findViewById(R.id.text_market_and_or_district);
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

        class HeaderHolder extends RecyclerView.ViewHolder {
            final TextView mVariety;

            HeaderHolder(View itemView) {
                super(itemView);
                mVariety = (TextView) itemView;
            }
        }

        @Override
        public long getHeaderId(int position) {
            return (position > 0) ? mVarietyToHeaderId.get(mCommodityList.get(position).getVariety()) : 0;
        }

        @Override
        public HeaderHolder onCreateHeaderViewHolder(ViewGroup parent) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.commodity_detail_card_header_variety, parent, false);
            return new HeaderHolder(view);
        }

        @Override
        public void onBindHeaderViewHolder(HeaderHolder headerHolder, int position) {
            Commodity commodity = mCommodityList.get(position);
            final String variety = commodity.getVariety();
            headerHolder.mVariety.setText(
                    headerHolder.itemView.getContext().getString(R.string.variety, variety));
        }

        @Override
        public CommodityDetailAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.commodity_card_row, parent, false);
            mConstraintSetNormal.clone((ConstraintLayout) view);
            try {
                mConstraintSetBig.load(parent.getContext(), R.layout.commodity_card_row_more);
            } catch (Exception e) {
                Crashlytics.logException(e);
                shouldShowMorebutton = false;
            }
            final CommodityDetailAdapter.ViewHolder vh = new ViewHolder(view);
            vh.mFav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int pos = vh.getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        final Commodity commodity = mCommodityList.get(pos);
                        final boolean isFavorite = commodity.isFavorite();
                        final Uri uri = CommodityContract.CommodityDataEntry.buildCommodityNameDetailUri(mCommodityName);
                        final int id = commodity.getId();
                        mRecyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                animateFavoriteSelect(vh, !isFavorite);
                                new FavoriteAddorRemoveAsyncTask(
                                        getContext(),
                                        !isFavorite, uri)
                                        .execute(id);
                            }
                        });

                        Snackbar.make(v,
                                isFavorite ? R.string.fav_removed: R.string.fav_added,
                                Snackbar.LENGTH_LONG)
                                .setAction(R.string.undo, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        new FavoriteAddorRemoveAsyncTask(
                                                getContext(),
                                                isFavorite, uri)
                                                .execute(id);
                                    }
                                })
                                .show();
                    }
                }
            });
            vh.mShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
                    Log.d("test", "payload");
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
            holder.mMarket.setText(res.getString(R.string.market_only, market));
            holder.mDistrict.setText(res.getString(R.string.district_only, district));
            holder.mState.setText(state);

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

            if (position == 0) {
                final Activity activity = getActivity();
                if (activity != null) {
                //  new PreferencesManager(activity).reset("bookmark");

                    mRecyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            new MaterialIntroView.Builder(activity)
                                    .enableIcon(true)
                                    .setFocusGravity(FocusGravity.CENTER)
                                    .setFocusType(Focus.NORMAL)
                                    .setDelayMillis(500)
                                    .enableFadeAnimation(true)
                                    .performClick(true)
                                    .setInfoText(getString(R.string.bookmark_intro_helper))
                                    .setShape(ShapeType.RECTANGLE)
                                    .setTarget(holder.mFav)
                                    .setUsageId("bookmark") //THIS SHOULD BE UNIQUE ID
                                    .show();
                        }
                    });
                }
            }
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

            // build the header id map
            if (mCommodityList != null) {
                mVarietyToHeaderId = new HashMap<>();
                for (int i = 0; i < mCommodityList.size(); i++) {
                    String variety = mCommodityList.get(i).getVariety();
                    if (!mVarietyToHeaderId.containsKey(variety)) {
                        mVarietyToHeaderId.put(variety, i);
                    }
                }
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

    private static class CursorToListAsyncTask extends AsyncTask<String, Void, Pair<DiffUtil.DiffResult, List<Commodity>>> {

        Cursor mCursor;
        WeakReference<CommodityDetailAdapter> commodityAdapterWeakReference;
        List<Commodity> pendingUpdateFromDiff;

        CursorToListAsyncTask(Cursor cursor, CommodityDetailAdapter commodityDetailAdapter) {
            mCursor = cursor;
            commodityAdapterWeakReference = new WeakReference<>(commodityDetailAdapter);
            pendingUpdateFromDiff = null;
        }

        CursorToListAsyncTask(List<Commodity> data, CommodityDetailAdapter commodityDetailAdapter) {
            mCursor = null;
            commodityAdapterWeakReference = new WeakReference<>(commodityDetailAdapter);
            pendingUpdateFromDiff = data;
        }


        @Override
        protected Pair<DiffUtil.DiffResult, List<Commodity>> doInBackground(String... params) {
            CommodityDetailAdapter commodityDetailAdapter = commodityAdapterWeakReference.get();
            if(commodityDetailAdapter == null) return null;

            final List<Commodity> oldCommodityList = commodityDetailAdapter.getList();
            List<Commodity> newCommodityList;

            if (mCursor != null) {
                newCommodityList = new ArrayList<>(mCursor.getCount());
                mCursor.moveToFirst();
                while(!mCursor.isAfterLast()) {
                    newCommodityList.add(Commodity.fromCursor(mCursor));
                    mCursor.moveToNext();
                }
            } else {
                newCommodityList = this.pendingUpdateFromDiff;
            }
            commodityDetailAdapter.pendingDiffUtilUpdates.add(newCommodityList);
            if (commodityDetailAdapter.pendingDiffUtilUpdates.size() > 1) {
                return null;
            }
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new CommodityDiffCallback(oldCommodityList, newCommodityList), false);
            return new Pair<>(diffResult, newCommodityList);
            // cursor close is handled by the cursor loader
        }

        @Override
        protected void onPostExecute(Pair<DiffUtil.DiffResult, List<Commodity>> pair) {
            CommodityDetailAdapter commodityDetailAdapter = commodityAdapterWeakReference.get();
            if(commodityDetailAdapter == null) return;
            if (pair == null) return;

            commodityDetailAdapter.pendingDiffUtilUpdates.remove();
            commodityDetailAdapter.setList(pair.second);
            pair.first.dispatchUpdatesTo(commodityDetailAdapter);
            if (commodityDetailAdapter.pendingDiffUtilUpdates.size() > 0) {
                new CursorToListAsyncTask(commodityDetailAdapter.pendingDiffUtilUpdates.peek(), commodityDetailAdapter).execute();
            }
        }
    }
}
