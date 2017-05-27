package info.santhosh.evlo.widget;

import android.content.Context;
import android.support.transition.TransitionManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;

/**
 * Created by santhoshvai on 24/11/2016.
 */

/**
 * Simple RecyclerView subclass that supports providing an empty view (which
 * is displayed when the adapter has no data and hidden otherwise).
 */
public class EmptyRecyclerView extends RecyclerView {

    private static final String TAG = "EmptyRecyclerView";
    public View mEmptyView;
    private WeakReference<SetEmptyViewCallback> mSetEmptyViewCallbackWeakReference = new WeakReference<>(null);

    private boolean shouldAnimate = false;

    private AdapterDataObserver mDataObserver = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            updateEmptyView();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            super.onItemRangeChanged(positionStart, itemCount);
            updateEmptyView();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            updateEmptyView();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
            updateEmptyView();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount);
            updateEmptyView();
        }
    };

    public EmptyRecyclerView(Context context) {
        super(context);
    }

    public EmptyRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EmptyRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Designate a view as the empty view. When the backing adapter has no
     * data this view will be made visible and the recycler view hidden.
     *
     */
    public void setEmptyView(View emptyView) {
        mEmptyView = emptyView;
    }

    public void setEmptyViewCallback(SetEmptyViewCallback setEmptyViewCallback) {
        this.mSetEmptyViewCallbackWeakReference = new WeakReference<>(setEmptyViewCallback);
    }

    @Override
    public void setAdapter(RecyclerView.Adapter adapter) {
        if (getAdapter() != null) {
            getAdapter().unregisterAdapterDataObserver(mDataObserver);
        }
        if (adapter != null) {
            adapter.registerAdapterDataObserver(mDataObserver);
        }
        super.setAdapter(adapter);
        updateEmptyView();
    }

    public void setShouldAnimate(boolean shouldAnimate) {
        this.shouldAnimate = shouldAnimate;
    }

    public void updateEmptyView() {
        if (mEmptyView != null && getAdapter() != null) {
            boolean showEmptyView = getAdapter().getItemCount() == 0;
            if (showEmptyView && shouldAnimate) {
                TransitionManager.beginDelayedTransition((ViewGroup) mEmptyView.getRootView());
            }
            mEmptyView.setVisibility(showEmptyView ? VISIBLE : GONE);
            setVisibility(showEmptyView ? GONE : VISIBLE);

            if (showEmptyView && mSetEmptyViewCallbackWeakReference != null) {
                final SetEmptyViewCallback emptyViewCallback = mSetEmptyViewCallbackWeakReference.get();
                if (emptyViewCallback != null) {
                    emptyViewCallback.setEmptyView(mEmptyView);
                }
            }
        } else if (mEmptyView == null){
            Log.e(TAG, "Empty view should be set before setting the adapter");
        }
    }

//    public void setEmptyViewText() {
//        // set the empty view text
//        if(getAdapter() instanceof CommodityListActivity.CommodityAdapter) {
//            CommodityListActivity.CommodityAdapter adapter = (CommodityListActivity.CommodityAdapter) getAdapter();
//            String searchFilter = adapter.getFilterSearch();
//            Utils.setEmptyViewText(getContext(), (TextView) mEmptyView, searchFilter);
//        }
//    }

    public interface SetEmptyViewCallback {
        void setEmptyView(View emptyView);
    }
}