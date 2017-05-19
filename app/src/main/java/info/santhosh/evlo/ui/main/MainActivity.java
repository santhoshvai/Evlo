package info.santhosh.evlo.ui.main;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.widget.Toast;

import com.facebook.device.yearclass.YearClass;

import info.santhosh.evlo.R;
import info.santhosh.evlo.ui.search.SearchToolbar;

/**
 * Created by santhoshvai on 12/02/17.
 */

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    SearchToolbar searchToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toast.makeText(this, Integer.toString(YearClass.get(getApplicationContext())), Toast.LENGTH_LONG).show();

        // search bar
        searchToolbar = (SearchToolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(searchToolbar);

    }

    // Menu icons are inflated just as they were with actionbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

}
