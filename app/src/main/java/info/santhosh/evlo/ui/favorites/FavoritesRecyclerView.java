package info.santhosh.evlo.ui.favorites;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import info.santhosh.evlo.common.EmptyRecyclerView;
import info.santhosh.evlo.common.Utils;
import info.santhosh.evlo.ui.CommodityListActivity;

/**
 * Created by santhoshvai on 10/12/2016.
 */

public class FavoritesRecyclerView extends EmptyRecyclerView {

    public FavoritesRecyclerView(Context context) {
        super(context);
    }

    public FavoritesRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FavoritesRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setEmptyViewText() {
        if (mEmptyView != null && getAdapter() != null) {
            // TODO: must change to favorites activity
            // set the empty view text
            if(getAdapter() instanceof CommodityListActivity.CommodityAdapter) {
                CommodityListActivity.CommodityAdapter adapter = (CommodityListActivity.CommodityAdapter) getAdapter();
                String searchFilter = adapter.getFilterSearch();
                Utils.setEmptyViewText(getContext(), (TextView) mEmptyView, searchFilter);
            }
        }
    }
}
