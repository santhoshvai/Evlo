package info.santhosh.evlo.ui.intro;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import info.santhosh.evlo.R;

public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
    }

    public void getStarted(View v) {
        setResult(Activity.RESULT_OK);
        finish();
    }
}
