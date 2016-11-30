package info.santhosh.evlo.common;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.View;

import info.santhosh.evlo.R;

/**
 * Created by santhoshvai on 30/11/2016.
 */

public class ColorUtil {

    /**
     * Set the background color of a view to be transparent
     * @param v View for which the background color has to be set
     */
    public static void setTransparentBackgroundColor(View v) {
        v.setBackgroundColor(Color.TRANSPARENT);
    }

    /**
     * Set the background color of a view to be one we want for ListRowSelection -- used for tablets
     * @param v View for which the background color has to be set
     */
    public static void setListRowSelectionBackgroundColor(Context context, View v) {
        v.setBackgroundColor(ContextCompat.getColor(context, R.color.colorListRowSelect));
    }
}
