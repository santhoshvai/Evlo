package info.santhosh.evlo.ui.intro;

import android.app.Activity;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import info.santhosh.evlo.R;
import info.santhosh.evlo.common.ConnectionStatusReceiver;
import info.santhosh.evlo.common.Utils;

public class IntroActivity extends AppCompatActivity implements ConnectionStatusReceiver.ConnectionReceivableActivity {

    ProgressBar progressBar;
    Button getStartedBtn;
    ConstraintLayout introRoot;
    TextView title;
    TextView description;
    ImageView notConnected;
    boolean isDownloadDone = false;
    boolean isOfflineState = false;
    boolean setupDone = false;
    ConnectionStatusReceiver mNetworkReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        progressBar = findViewById(R.id.progressBar);
        getStartedBtn = findViewById(R.id.getStartedBtn);
        introRoot = findViewById(R.id.intro_root);
        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
        notConnected = findViewById(R.id.not_connected);
        getStartedBtn.setScaleX(0);
        getStartedBtn.setScaleY(0);
        mNetworkReceiver = new ConnectionStatusReceiver(this);
        startDownLoadIfPossible();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void startDownLoadIfPossible() {
        if (Utils.isNetworkAvailable(this)) {
            progressBar.setProgress(1);  // fake an always progressive perspective
            new IntroLoadProgress(this).execute();
        } else {
            onOffline();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mNetworkReceiver);
    }

    public void onGetStarted(View v) {
        if (setupDone) { // comes only after onLoadDone
            setResult(Activity.RESULT_OK);
            finish();
            overridePendingTransition(R.anim.fade_in, R.anim.slide_out_up);
        } else {
            // retry button
            if (!Utils.isNetworkAvailable(this)) {
                Toast.makeText(this, R.string.offline_explanation, Toast.LENGTH_LONG).show();
            } else {
                onOnline();
            }
        }
    }

    public void onProgressUpdate(int percentage) {
        if (!isDownloadDone && percentage >= 60) isDownloadDone = true;
        progressBar.setProgress(percentage);
    }

    public void onLoadDone() {
        setupDone = true;
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

    public void onProtoFetchError() {
        Toast.makeText(this, "onProtoFetchError", Toast.LENGTH_LONG).show();
        onOffline();
        notConnected.setVisibility(View.GONE);
        description.setText(R.string.something_went_wrong);
        description.setAlpha(0f);
        description.animate().alpha(1f).setDuration(500).start();
    }

    @Override
    public void onOnline() {
        if (isOfflineState) {
            isOfflineState = false;
            notConnected.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            getStartedBtn.setScaleX(0);
            getStartedBtn.setScaleY(0);
            getStartedBtn.setText(R.string.intro_button);
            description.setText(R.string.intro_1_progress);
            description.setAlpha(0f);
            description.animate().alpha(1f).setDuration(500).start();
            startDownLoadIfPossible();
        }
    }

    @Override
    public void onOffline() {
        if (!isDownloadDone) {
            isOfflineState = true;
            notConnected.setVisibility(View.VISIBLE);
            description.setText(R.string.offline_explanation);
            progressBar.setVisibility(View.GONE);
            getStartedBtn.animate().scaleX(1f).scaleY(1f)
                    .setInterpolator(new OvershootInterpolator())
                    .setDuration(200).start();
            getStartedBtn.setText(R.string.retry);
        }
    }
}
