package info.santhosh.evlo.ui.main;

import android.os.Bundle;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationViewPager;
import com.facebook.device.yearclass.YearClass;

import info.santhosh.evlo.R;
import info.santhosh.evlo.ui.search.SearchToolbar;

/**
 * Created by santhoshvai on 12/02/17.
 */

public class MainActivity extends AppCompatActivity implements AHBottomNavigation.OnTabSelectedListener {

    private static final String TAG = "MainActivity";

    SearchToolbar searchToolbar;

    // fragments switching
    private AHBottomNavigationViewPager viewPager;
    private FragmentStatePagerAdapter adapter;

    // bottom bar related
    private AHBottomNavigationAdapter navigationAdapter;
    private AHBottomNavigation bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toast.makeText(this, Integer.toString(YearClass.get(getApplicationContext())), Toast.LENGTH_LONG).show();

        // view pager
        viewPager = (AHBottomNavigationViewPager) findViewById(R.id.view_pager);
        viewPager.setOffscreenPageLimit(2);
        adapter = new MainPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        // search bar
        searchToolbar = (SearchToolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(searchToolbar);

        // bottom bar
        bottomNavigation = (AHBottomNavigation) findViewById(R.id.bottom_bar);
        bottomNavigation.setOnTabSelectedListener(this);
        bottomNavigation.setAccentColor(ContextCompat.getColor(getApplicationContext(), R.color.teal_500));
        bottomNavigation.setInactiveColor(ContextCompat.getColor(getApplicationContext(), R.color.inactive_bottombar));
        navigationAdapter = new AHBottomNavigationAdapter(this, R.menu.bottom_bar_menu);
        navigationAdapter.setupWithBottomNavigation(bottomNavigation);
    }

    /**
     * Called when a bottom bar tab has been selected (clicked)
     *
     * @param position    int: Position of the selected tab
     * @param wasSelected boolean: true if the tab was already selected
     * @return boolean: true for updating the tab UI, false otherwise
     */
    @Override
    public boolean onTabSelected(int position, boolean wasSelected) {

        if(position == 0) searchToolbar.setTitle(R.string.search_commodities);
        else if(position == 1) searchToolbar.setTitle(R.string.search_markets);
        // TODO: hide search on position 3

        if (wasSelected) {
            return true;
        }

        viewPager.setCurrentItem(position, false);

        return true;
    }
}
