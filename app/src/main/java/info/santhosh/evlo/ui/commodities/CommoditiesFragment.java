package info.santhosh.evlo.ui.commodities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import info.santhosh.evlo.R;

/**
 * Created by santhoshvai on 07/04/17.
 */

public class CommoditiesFragment extends Fragment {

    public static CommoditiesFragment newInstance() {
        CommoditiesFragment f = new CommoditiesFragment();
        Bundle args = new Bundle();
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.commodities_fragment, container, false);
        return rootView;
    }

}
