package info.santhosh.evlo.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.transition.AutoTransition;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;

import info.santhosh.evlo.R;
import info.santhosh.evlo.common.Utils;
import info.santhosh.evlo.ui.favorites.FavoritesFragment;
import info.santhosh.evlo.ui.search.SearchActivity;

/**
 * Created by santhoshvai on 12/02/17.
 */

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    SearchToolbar mSearchToolbar;

    private static final int SEARCH_BAR_TRANSITION_DURATION = 250;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                transitionToSearch();
            }
        });

        if (savedInstanceState == null) {
            FavoritesFragment favoritesFragment = FavoritesFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_fragment_container, favoritesFragment)
                    .commit();
        }

    }

    private void transitionToSearch() {
        // create a transition that navigates to search when complete
        Transition transition = new AutoTransition();
        transition.setDuration(SEARCH_BAR_TRANSITION_DURATION);
        transition.addListener(new Transition.TransitionListener() {
            @Override
            public void onTransitionStart(@NonNull Transition transition) {

            }

            @Override
            public void onTransitionEnd(@NonNull Transition transition) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);

                // we are handing the enter transitions ourselves
                // this line overrides that
                overridePendingTransition(0, 0);
            }

            @Override
            public void onTransitionCancel(@NonNull Transition transition) {

            }

            @Override
            public void onTransitionPause(@NonNull Transition transition) {

            }

            @Override
            public void onTransitionResume(@NonNull Transition transition) {

            }
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

}
