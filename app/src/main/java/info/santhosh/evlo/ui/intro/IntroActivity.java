package info.santhosh.evlo.ui.intro;

import android.app.Activity;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import info.santhosh.evlo.R;

public class IntroActivity extends AppCompatActivity {

    ProgressBar progressBar;
    Button getStartedBtn;
    ConstraintLayout introRoot;
    TextView title;
    TextView description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        progressBar = findViewById(R.id.progressBar);
        new IntroLoadProgress(this).execute();
        progressBar.setProgress(1);  // fake an always progressive perspective
        getStartedBtn = findViewById(R.id.getStartedBtn);
        getStartedBtn.setScaleX(0);
        getStartedBtn.setScaleY(0);
        introRoot = findViewById(R.id.intro_root);
        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
    }

    public void onGetStarted(View v) {
        setResult(Activity.RESULT_OK);
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.slide_out_up);
    }

    public void onProgressUpdate(int percentage) {
        progressBar.setProgress(percentage);
    }

    public void onLoadDone() {
//        TransitionManager.beginDelayedTransition(introRoot);
//        progressBar.setVisibility(View.GONE);
//        getStartedBtn.setVisibility(View.VISIBLE);
        progressBar.animate().alpha(0f).setDuration(200).start();
        getStartedBtn.animate().scaleX(1f).scaleY(1f)
                .setInterpolator(new OvershootInterpolator())
                .setStartDelay(200).setDuration(300).start();
        title.setText(getString(R.string.intro_1_title_done));
        title.setAlpha(0f);
        title.animate().alpha(1f).setDuration(500).start();
        description.setText(getString(R.string.intro_1_desc_done));
        description.setAlpha(0f);
        description.animate().alpha(1f).setDuration(500).start();
    }


}
