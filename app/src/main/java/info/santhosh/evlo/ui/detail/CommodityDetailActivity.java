package info.santhosh.evlo.ui.detail;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.ads.MobileAds;

import info.santhosh.evlo.R;

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
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (getSupportFragmentManager().findFragmentById(R.id.commodity_detail_container) == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Fragment fragment = CommodityDetailFragment.newInstance(
                    getIntent().getStringExtra(CommodityDetailFragment.COMMODITY_NAME));

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.commodity_detail_container, fragment)
                    .commit();
        }

        // we set the background for viewgroup in xml, no need for window background
        getWindow().setBackgroundDrawable(null);

        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");

    }

    public static Intent getIntent(Activity activity, String commodityName) {
        Intent intent = new Intent(activity, CommodityDetailActivity.class);
        intent.putExtra(CommodityDetailFragment.COMMODITY_NAME, commodityName);
        return intent;
    }

}
