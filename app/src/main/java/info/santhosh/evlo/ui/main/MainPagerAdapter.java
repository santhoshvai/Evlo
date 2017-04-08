package info.santhosh.evlo.ui.main;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import info.santhosh.evlo.ui.commodities.CommoditiesFragment;
import info.santhosh.evlo.ui.favorites.FavoritesFragment;
import info.santhosh.evlo.ui.markets.MarketsFragment;

/**
 * Created by santhoshvai on 25/01/2017.
 */

public class MainPagerAdapter extends FragmentStatePagerAdapter {
    private static final int NUM_ITEMS = 3;
    private Fragment currentFragment;
    private Context mContext;

    public MainPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch(position) {
            // This order should be the same as bottom bar menu order
            case 0: return CommoditiesFragment.newInstance();
            case 1: return MarketsFragment.newInstance();
            case 2: return FavoritesFragment.newInstance();
            default:
                throw new IllegalArgumentException("Wrong main activity fragment item id");
        }
    }

    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        if (getCurrentFragment() != object) {
            currentFragment = ((Fragment) object);
        }
        super.setPrimaryItem(container, position, object);
    }

    /**
     * Get the current fragment
     */
    public Fragment getCurrentFragment() {
        return currentFragment;
    }
}
