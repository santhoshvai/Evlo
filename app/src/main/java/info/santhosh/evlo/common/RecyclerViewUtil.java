package info.santhosh.evlo.common;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
            final ImageView mFav;
            final ImageView mShare;

            ViewHolder(View view) {
                super(view);
                this.mVariety = (TextView) view.findViewById(R.id.text_variety);
                this.mModalPrice = (TextView) view.findViewById(R.id.text_modal_price);
                this.mState = (TextView) view.findViewById(R.id.text_state_name);
                this.mMarket = (TextView) view.findViewById(R.id.text_market_district);
                this.mFav = (ImageView) view.findViewById(R.id.favorite_icon);
                this.mShare = (ImageView) view.findViewById(R.id.share_icon);
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
                    int pos = vh.getAdapterPosition();
                    if (pos != NO_POSITION) {
                        Commodity commodity = mCommodityList.get(pos);
                        v.setSelected(!v.isSelected());
                        new FavoriteAddorRemoveAsyncTask(v.getContext(), v.isSelected()).execute(commodity.getId());
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

        @Override
        public void onBindViewHolder(final CommodityDetailAdapter.ViewHolder holder, final int position) {
            Commodity commodity = mCommodityList.get(position);
            final Resources res = mContext.getResources();

            // Read from cursor

            final String commodityName = commodity.getCommodity();
            final String modalPrice = commodity.getModal_Price();
            final String district = commodity.getDistrict();
            final String market = commodity.getMarket();
            final String state = commodity.getState();
            // variety name given is same as commodityName, then replace it as Normal
            final String variety = commodity.getVariety().equalsIgnoreCase(commodityName)? "Normal": commodity.getVariety();
            final String nameAndVariety = res.getString(R.string.commodity_name_and_variety, commodityName, variety);

            if(commodity.getFav_row_id() < 0) holder.mFav.setSelected(false);
            else holder.mFav.setSelected(true);

            String modal_price_text = res.getString(R.string.modal_price, modalPrice);
            String market_text = res.getString(R.string.market, market, district);
            holder.mModalPrice.setText(Html.fromHtml(modal_price_text));
            holder.mMarket.setText(market_text);
            holder.mState.setText(state);

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
            return super.getChangePayload(oldItemPosition, newItemPosition);
        }
    }

    public static class CursorToListAsyncTask extends AsyncTask<String, Void, DiffUtil.DiffResult> {

        Cursor mCursor;
        WeakReference<CommodityDetailAdapter> commodityAdapterWeakReference;

        public CursorToListAsyncTask(Cursor cursor, CommodityDetailAdapter commodityDetailAdapter) {
            mCursor = cursor;
            commodityAdapterWeakReference = new WeakReference<CommodityDetailAdapter>(commodityDetailAdapter);
        }

        @Override
        protected DiffUtil.DiffResult doInBackground(String... params) {
            ArrayList<Commodity> commodities = new ArrayList<>(mCursor.getCount());
            while (mCursor.moveToNext()) {
                commodities.add(Commodity.fromCursor(mCursor));
            }
            CommodityDetailAdapter commodityDetailAdapter = commodityAdapterWeakReference.get();
            if(commodityDetailAdapter == null) return null;
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new CommodityDiffCallback(commodityDetailAdapter.getList(), commodities), false);
            commodityDetailAdapter.setList(commodities);
            return diffResult;
            // cursor close is handled by the cursor loader
        }

        @Override
        protected void onPostExecute(DiffUtil.DiffResult diffResult) {
            CommodityDetailAdapter commodityDetailAdapter = commodityAdapterWeakReference.get();
            if(commodityDetailAdapter == null) return;
            diffResult.dispatchUpdatesTo(commodityDetailAdapter);
        }
    }
}
