package info.santhosh.evlo.ui.faq;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import info.santhosh.evlo.R;

/**
 * Created by santhoshvai on 27/01/2018.
 */

public class FaqActivity extends AppCompatActivity implements CustomTabActivityHelper.ConnectionCallback {
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    RecyclerView mRecyclerView;
    FaqAdapter mFaqAdapter;
    CustomTabActivityHelper customTabActivityHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);
        Toolbar toolbar = findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // we set the background for viewgroup in xml, no need for window background
        getWindow().setBackgroundDrawable(null);

        mRecyclerView = findViewById(R.id.faq_list);
        mFaqAdapter = new FaqAdapter();
        // set item decoration
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.divider_grey));
        mRecyclerView.addItemDecoration(dividerItemDecoration);
        mRecyclerView.setAdapter(mFaqAdapter);
        mRecyclerView.setHasFixedSize(true);

        customTabActivityHelper = new CustomTabActivityHelper();
        customTabActivityHelper.setConnectionCallback(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return(true);
        }

        return(super.onOptionsItemSelected(item));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        customTabActivityHelper.setConnectionCallback(null);
    }

    @Override
    public void onCustomTabsConnected() {
    }

    @Override
    public void onCustomTabsDisconnected() {
    }

    @Override
    protected void onStart() {
        super.onStart();
        customTabActivityHelper.bindCustomTabsService(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        customTabActivityHelper.unbindCustomTabsService(this);
    }

    private class FaqAdapter extends RecyclerView.Adapter<FaqAdapter.ViewHolder>  {

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView title;
            final TextView description;
            final ImageView icon;

            ViewHolder(View view) {
                super(view);
                this.title = view.findViewById(R.id.title);
                this.description = view.findViewById(R.id.description);
                this.icon = view.findViewById(R.id.right_icon);
            }

            void bind(@StringRes int titleRes, @StringRes int descriptionRes, @DrawableRes int iconRes) {
                this.title.setText(titleRes);
                this.description.setText(descriptionRes);
                this.icon.setImageResource(iconRes);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.faq_card_row, parent, false);
            final ViewHolder viewHolder = new ViewHolder(view);
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int pos = viewHolder.getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        if (LinksResourceId[pos] != null) {
                            CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder(customTabActivityHelper.getSession())
                                    .setToolbarColor(ContextCompat.getColor(FaqActivity.this, R.color.teal_500))
                                    .setShowTitle(true)
                                    .build();
                            String url = v.getContext().getString(LinksResourceId[pos]);
                            CustomTabActivityHelper.openCustomTab(
                                    FaqActivity.this, customTabsIntent, Uri.parse(url), new CustomTabActivityHelper.CustomTabFallback() {
                                        @Override
                                        public void openUri(Activity activity, Uri uri) {
                                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                            activity.startActivity(intent);
                                        }
                                    });
                        }
                    }
                }
            });
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            @DrawableRes int iconRes = (LinksResourceId[position] == null) ? R.drawable.ic_question_24dp_vd : R.drawable.ic_link_24dp_vd;
            holder.bind(TitlesResourceId[position], DescriptionsResourceId[position], iconRes);
        }

        @Override
        public int getItemCount() {
            return TitlesResourceId.length;
        }
    }

    static final Integer[] TitlesResourceId = new Integer[] {R.string.where_prices_title, R.string.why_markets_title, R.string.why_units_title, R.string.where_historical_title};
    static final Integer[] DescriptionsResourceId = new Integer[] {R.string.where_prices_description, R.string.why_markets_description, R.string.why_units_description, R.string.where_historical_description};
    static final Integer[] LinksResourceId = new Integer[] {R.string.where_prices_link, null, R.string.why_units_link, R.string.where_historical_link};

}
