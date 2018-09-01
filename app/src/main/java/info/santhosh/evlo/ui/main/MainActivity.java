package info.santhosh.evlo.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.transition.AutoTransition;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.crashlytics.android.Crashlytics;
import com.google.ads.consent.ConsentInformation;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;

import co.mobiwise.materialintro.shape.Focus;
import co.mobiwise.materialintro.shape.FocusGravity;
import co.mobiwise.materialintro.shape.ShapeType;
import co.mobiwise.materialintro.view.MaterialIntroView;
import info.santhosh.evlo.BuildConfig;
import info.santhosh.evlo.R;
import info.santhosh.evlo.common.AnalyticsUtils;
import info.santhosh.evlo.common.EvloPrefs;
import info.santhosh.evlo.common.Utils;
import info.santhosh.evlo.data.scheduleJobs.CommodityJob;
import info.santhosh.evlo.ui.faq.FaqActivity;
import info.santhosh.evlo.ui.intro.IntroActivity;
import info.santhosh.evlo.ui.search.SearchActivity;

/**
 * Created by santhoshvai on 12/02/17.
 */

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int INTRO_REQUEST_CODE = 77;

    SearchToolbar mSearchToolbar;

    private static final int SEARCH_BAR_TRANSITION_DURATION = 200;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // transition from the cold start launch theme
        setTheme(R.style.AppTheme_NoActionBar); // https://plus.google.com/+AndroidDevelopers/posts/Z1Wwainpjhd
        super.onCreate(savedInstanceState);

        // start Intro Activity If Needed
        if(EvloPrefs.getIsFirstRun(this)) {
            startActivityForResult(new Intent(this, IntroActivity.class), INTRO_REQUEST_CODE);
        }

        setContentView(R.layout.activity_main);

        // search bar
        mSearchToolbar = (SearchToolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mSearchToolbar);


        // just delegate the search icon click to action clicking the toolbar
        mSearchToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchToolbar.performClick();
            }
        });

        mSearchToolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Prepare the keyboard as soon as the user touches the Toolbar
                // This will make the transition look faster
                Utils.showKeyboard(MainActivity.this);
                AnalyticsUtils.logItemSelection(MainActivity.this, "Search toolbar");
                if (Utils.isAPI21Plus()) {
                    transitionToSearch();
                } else { // use default animation on lower api's due to bad performance
                    Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                    startActivity(intent);
                }
            }
        });


        if (getSupportFragmentManager().findFragmentById(R.id.main_fragment_container) == null) {
            FavoritesFragment favoritesFragment = FavoritesFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_fragment_container, favoritesFragment)
                    .commit();
        }

        MobileAds.initialize(this, BuildConfig.AD_APP_ID);

        // schedule evernote jobs
        if (savedInstanceState == null) {
            CommodityJob.scheduleJobWhenCharging();
            CommodityJob.scheduleJobWhenNotChargingWiFiOnly();
            if (!EvloPrefs.getIsFirstRun(this)) {
                CommodityJob.scheduleJobImmediately();
            }
        }
    }

    private void transitionToSearch() {
        // create a transition that navigates to search when complete
        Transition transition = new AutoTransition();
        transition.setDuration(SEARCH_BAR_TRANSITION_DURATION);
        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(@NonNull Transition transition) {}

            @Override
            public void onTransitionEnd(@NonNull Transition transition) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);

                // we are handing the enter transitions ourselves
                // this line overrides that
//                overridePendingTransition(0, 0);

                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }

            @Override
            public void onTransitionCancel(@NonNull Transition transition) {}

            @Override
            public void onTransitionPause(@NonNull Transition transition) {}

            @Override
            public void onTransitionResume(@NonNull Transition transition) {}
        });

        // all we have to do is change the attributes of the toolbar and the TransitionManager animates the changes

        TransitionManager.beginDelayedTransition(mSearchToolbar, transition);
        FrameLayout.LayoutParams frameLP = (FrameLayout.LayoutParams) mSearchToolbar.getLayoutParams();
        frameLP.setMargins(0, 0, 0, 0);
        mSearchToolbar.setLayoutParams(frameLP);
        mSearchToolbar.hideContent();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // when you are back from the SearchActivity animate the 'shrinking' of the Toolbar and
        // fade its contents back in
        fadeToolbarIn();

        // in case we are not coming here from the SearchActivity the Toolbar would have been already visible
        // so the above method has no effect
    }

    private void fadeToolbarIn() {
        final int toolbarMargin = getResources().getDimensionPixelSize(R.dimen.search_toolbar_margin);
        Transition transition = new AutoTransition();
        transition.setDuration(SEARCH_BAR_TRANSITION_DURATION);
        TransitionManager.beginDelayedTransition(mSearchToolbar, transition);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mSearchToolbar.getLayoutParams();
        layoutParams.setMargins(toolbarMargin, toolbarMargin, toolbarMargin, toolbarMargin);
        mSearchToolbar.showContent();
        mSearchToolbar.setLayoutParams(layoutParams);
    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean isEU = ConsentInformation.getInstance(this).isRequestLocationInEeaOrUnknown();
        if (!isEU) {
            menu.removeItem(R.id.changeAdSettings);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.disclaimer:
                AnalyticsUtils.logMenuClick(this, getString(R.string.disclaimer));
                DisclaimerDialog.showDisclaimer(this);
                return true;
            case R.id.faq:
                AnalyticsUtils.logMenuClick(this, getString(R.string.faq));
                startActivity(new Intent(this, FaqActivity.class));
                return true;
            case R.id.contactUs:
                AnalyticsUtils.logMenuClick(this, getString(R.string.contact_us));
                Utils.composeEmail(this, getString(R.string.feedback_email), getString(R.string.contact_us_subject));
                return true;
            case R.id.changeAdSettings:
                AnalyticsUtils.logMenuClick(this, getString(R.string.change_ad_settings));
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_fragment_container);
                if (fragment instanceof FavoritesFragment) {
                    ((FavoritesFragment) fragment).changeAdSettings();
                } else {
                    Crashlytics.logException(
                            new IllegalStateException("User opted to change ad settings, but we couldnt find favorites fragment on the main activity"));
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INTRO_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                new MaterialIntroView.Builder(this)
                        .enableIcon(false)
                        .setFocusGravity(FocusGravity.LEFT)
                        .setFocusType(Focus.NORMAL)
                        .setDelayMillis(500)
                        .enableFadeAnimation(true)
                        .performClick(true)
                        .setInfoText("Hi There! Start by searching for commodities, Tap on the search bar.")
                        .setShape(ShapeType.CIRCLE)
                        .setTarget(mSearchToolbar)
                        .setUsageId("search_bar") //THIS SHOULD BE UNIQUE ID
                        .show();
            } else {
                finish();
            }
        }
    }
}
