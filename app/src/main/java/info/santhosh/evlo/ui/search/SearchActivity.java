package info.santhosh.evlo.ui.search;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import info.santhosh.evlo.R;

/**
 * Created by santhoshvai on 20/05/17.
 */

public class SearchActivity extends AppCompatActivity {

    private Searchbar mSearchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mSearchBar = (Searchbar) findViewById(R.id.search_toolbar);
        setSupportActionBar(mSearchBar);
    }

}
