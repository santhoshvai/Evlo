package info.santhosh.evlo.common;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.util.Pair;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import info.santhosh.evlo.R;
import info.santhosh.evlo.data.FavoriteAddorRemoveAsyncTask;
import info.santhosh.evlo.data.dbModels.Commodity;

import static android.support.v7.widget.RecyclerView.NO_POSITION;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by santhoshvai on 12/02/17.
 */

public class RecyclerViewUtil {

    public static final int COMMODITY_VARIETY_DETAIL = 0;
    public static final int FAVORITES = 1;

    @Retention(SOURCE)
    @IntDef({COMMODITY_VARIETY_DETAIL, FAVORITES})
    public @interface CardFragmentType {}

    public static class CommodityDetailAdapter extends RecyclerView.Adapter<CommodityDetailAdapter.ViewHolder> {

        private List<Commodity> mCommodityList = null;
        final private Context mContext;
        @CardFragmentType int mCardFragmentType = COMMODITY_VARIETY_DETAIL;

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

        public CommodityDetailAdapter(Context context, @CardFragmentType int cardFragmentType) {
            mContext = context;
            mCardFragmentType = cardFragmentType;
        }

        @Override
        public CommodityDetailAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            switch (mCardFragmentType){
                case COMMODITY_VARIETY_DETAIL:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.commodity_detail_card_row, parent, false);
                    break;
                case FAVORITES:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.favorites_card_row, parent, false);
                    break;
                default:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.commodity_detail_card_row, parent, false);
            }
            final CommodityDetailAdapter.ViewHolder vh = new ViewHolder(view);
            vh.mFav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int pos = vh.getAdapterPosition();
                    if (pos != NO_POSITION) {
                        final Commodity commodity = mCommodityList.get(pos);
                        final boolean shouldAdd = !v.isSelected();
//                        final Uri detailUri = CommodityContract.CommodityDataEntry.buildCommodityNameDetailUri(commodity.getCommodity());
                        new FavoriteAddorRemoveAsyncTask(v.getContext(), shouldAdd).execute(commodity.getId());
                        animateFavoriteSelect(vh, shouldAdd);
                        if (!shouldAdd) {
                            Snackbar.make(v, R.string.fav_removed, Snackbar.LENGTH_LONG)
                                    .setAction(R.string.undo, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            new FavoriteAddorRemoveAsyncTask(v.getContext(),
                                                    true)
                                                    .execute(commodity.getId());
                                            animateFavoriteSelect(vh, true);
                                        }
                                    }).show();
                        } else {
                            Snackbar.make(v, R.string.fav_added, Snackbar.LENGTH_SHORT)
                                    .setAction(R.string.undo, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            new FavoriteAddorRemoveAsyncTask(v.getContext(),
                                                    false)
                                                    .execute(commodity.getId());
                                            animateFavoriteSelect(vh, false);
                                        }
                                    }).show();
                        }
                    }
                }
            });
            vh.mShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: share an image if you can
                    final Resources res = mContext.getResources();
                    int pos = vh.getAdapterPosition();
                    if (pos != NO_POSITION) {
                        Commodity commodity = mCommodityList.get(pos);
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        final String commodityName = commodity.getCommodity();
                        final String modalPrice = commodity.getModal_Price();
                        final String district = commodity.getDistrict();
                        final String market = commodity.getMarket();
                        final String state = commodity.getState();
                        final String variety = commodity.getVariety().equalsIgnoreCase(commodityName)? "Normal": commodity.getVariety();
                        String share = v.getContext().getResources().getString(R.string.share_data,
                                commodityName, variety, modalPrice, market, district, state);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, share);
                        sendIntent.setType("text/plain");
                        mContext.startActivity(Intent.createChooser(sendIntent,
                                res.getString(R.string.share_heading)));
                    }
                }
            });
            return vh;
        }

        private void animateFavoriteSelect(CommodityDetailAdapter.ViewHolder holder, boolean shouldSelect) {
            // TODO: animate the icon fill
            holder.mFav.setSelected(shouldSelect);
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
        public void onBindViewHolder(final CommodityDetailAdapter.ViewHolder holder, final int position) {
            Commodity commodity = mCommodityList.get(position);
            final Resources res = mContext.getResources();

            final String commodityName = commodity.getCommodity();
            final String modalPrice = commodity.getModal_Price();
            final String district = commodity.getDistrict();
            final String market = commodity.getMarket();
            final String state = commodity.getState();
            // variety name given is same as commodityName, then replace it as Normal
            final String variety = commodity.getVariety().equalsIgnoreCase(commodityName)? "Normal": commodity.getVariety();
            final String nameAndVariety = res.getString(R.string.commodity_name_and_variety, commodityName, variety);

            holder.mFav.setSelected(commodity.isFavorite());

            String modal_price_text = res.getString(R.string.modal_price, modalPrice);
            String market_text = res.getString(R.string.market, market, district);
            holder.mModalPrice.setText(Html.fromHtml(modal_price_text));
            holder.mMarket.setText(market_text);
            holder.mState.setText(state);

            // TODO: (More Button to show details) https://stackoverflow.com/questions/41464629/expand-collapse-animation-in-cardview

            switch (mCardFragmentType){
                case COMMODITY_VARIETY_DETAIL:
                    holder.mVariety.setText(res.getString(R.string.variety, variety));
                    break;
                case FAVORITES:
                    holder.mVariety.setText(res.getString(R.string.commodity_name_and_variety, commodityName, variety));
                    break;
                default:
                    holder.mVariety.setText(res.getString(R.string.commodity_name_and_variety, commodityName, variety));
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

        void setList(List<Commodity> commodityList) {
            if (mCommodityList != null) {
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

    public static class CursorToListAsyncTask extends AsyncTask<String, Void, Pair<DiffUtil.DiffResult, ArrayList<Commodity>>> {

        Cursor mCursor;
        WeakReference<CommodityDetailAdapter> commodityAdapterWeakReference;

        public CursorToListAsyncTask(Cursor cursor, CommodityDetailAdapter commodityDetailAdapter) {
            mCursor = cursor;
            commodityAdapterWeakReference = new WeakReference<>(commodityDetailAdapter);
        }

        @Override
        protected Pair<DiffUtil.DiffResult, ArrayList<Commodity>> doInBackground(String... params) {
            CommodityDetailAdapter commodityDetailAdapter = commodityAdapterWeakReference.get();
            if(commodityDetailAdapter == null) return null;

            final List<Commodity> oldCommodityList = commodityDetailAdapter.getList();
            ArrayList<Commodity> newCommodityList = new ArrayList<>(mCursor.getCount());
            mCursor.moveToFirst();
            while (mCursor.moveToNext()) {
                newCommodityList.add(Commodity.fromCursor(mCursor));
            }
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new CommodityDiffCallback(oldCommodityList, newCommodityList), false);
            return new Pair<>(diffResult, newCommodityList);
            // cursor close is handled by the cursor loader
        }

        @Override
        protected void onPostExecute(Pair<DiffUtil.DiffResult, ArrayList<Commodity>> pair) {
            CommodityDetailAdapter commodityDetailAdapter = commodityAdapterWeakReference.get();
            if(commodityDetailAdapter == null) return;

            commodityDetailAdapter.setList(pair.second);
            pair.first.dispatchUpdatesTo(commodityDetailAdapter);
        }
    }
}
