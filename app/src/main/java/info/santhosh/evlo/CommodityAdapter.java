package info.santhosh.evlo;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by santhoshvai on 16/04/16.
 */
public class CommodityAdapter extends RecyclerView.Adapter<CommodityAdapter.ViewHolder> {

    private Cursor mCursor;
    final private Context mContext;
    private String mFilterSearch;

    /**
     * Cache of the children views for a commodity list item.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mCommodityNameView;
//        public final TextView mVarietyNameView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mCommodityNameView = (TextView) view.findViewById(R.id.commodity_name);
//            mVarietyNameView = (TextView) view.findViewById(R.id.variety_name);
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
    public void onBindViewHolder(CommodityAdapter.ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        // Read from cursor

        String commodityName = mCursor.getString(CommodityListActivity.COL_COMMODITY_NAME);

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
