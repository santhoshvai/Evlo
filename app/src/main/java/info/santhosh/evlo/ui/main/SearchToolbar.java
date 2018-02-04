package info.santhosh.evlo.ui.main;

import android.content.Context;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import info.santhosh.evlo.R;
import info.santhosh.evlo.widget.TransformingToolbar;

/**
 * Created by santhoshvai on 07/04/17.
 */

public class SearchToolbar extends TransformingToolbar {

    public SearchToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
        final VectorDrawableCompat drawable =
                VectorDrawableCompat.create(getResources(), R.drawable.ic_search_grey, null);
        setNavigationIcon(drawable);
    }
}
