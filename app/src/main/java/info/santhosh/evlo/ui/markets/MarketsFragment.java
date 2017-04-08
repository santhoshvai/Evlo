package info.santhosh.evlo.ui.markets;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by santhoshvai on 07/04/17.
 */

public class MarketsFragment extends Fragment {

    public static MarketsFragment newInstance() {
        MarketsFragment f = new MarketsFragment();
        Bundle args = new Bundle();
        f.setArguments(args);
        return f;
    }
}
