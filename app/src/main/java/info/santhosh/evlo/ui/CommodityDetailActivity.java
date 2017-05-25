package info.santhosh.evlo.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;

import info.santhosh.evlo.R;

/**
 * An activity representing a single Commodity detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link CommodityListActivity}.
 */
public class CommodityDetailActivity extends AppCompatActivity {

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commodity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (getSupportFragmentManager().findFragmentById(R.id.commodity_detail_container) == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(CommodityDetailFragment.COMMODITY_NAME,
                    getIntent().getStringExtra(CommodityDetailFragment.COMMODITY_NAME));
            Fragment fragment = new CommodityDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.commodity_detail_container, fragment)
                    .commit();
        }

        // we set the background for viewgroup in xml, no need for window background
        getWindow().setBackgroundDrawable(null);
    }

}
