package info.santhosh.evlo.ui;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationViewPager;
import com.facebook.device.yearclass.YearClass;

import info.santhosh.evlo.R;

/**
 * Created by santhoshvai on 12/02/17.
 */

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // bottom bar related
    private AHBottomNavigationAdapter navigationAdapter;
    private AHBottomNavigationViewPager viewPager;
    private AHBottomNavigation bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toast.makeText(this, Integer.toString(YearClass.get(getApplicationContext())), Toast.LENGTH_LONG).show();
        Log.d(TAG, "onCreate: " + YearClass.get(getApplicationContext()));

        bottomNavigation = (AHBottomNavigation) findViewById(R.id.bottom_bar);
        bottomNavigation.setAccentColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
        bottomNavigation.setInactiveColor(ContextCompat.getColor(getApplicationContext(), R.color.inactive_bottombar));

        navigationAdapter = new AHBottomNavigationAdapter(this, R.menu.bottom_bar_menu);
        navigationAdapter.setupWithBottomNavigation(bottomNavigation);
    }
}
