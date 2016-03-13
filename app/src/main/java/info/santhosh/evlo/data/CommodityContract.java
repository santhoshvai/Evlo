package info.santhosh.evlo.data;

import android.net.Uri;
import android.provider.BaseColumns;

import info.santhosh.evlo.BuildConfig;

/**
 * Created by santhoshvai on 13/03/16.
 */
public class CommodityContract {
    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = BuildConfig.APPLICATION_ID;

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    public static final String PATH_COMMODITY = "commodity";
    public static final String PATH_STATE = "state";
    public static final String PATH_DISTRICT = "district";
    public static final String PATH_MARKET = "market";

    public static final class StateEntry implements BaseColumns {
        public static final String TABLE_NAME = "state";
        public static final String COLUMN_STATE_NAME = "state_name";
    }
    public static final class DistrictEntry implements BaseColumns {
        public static final String TABLE_NAME = "district";
        public static final String COLUMN_DISTRICT_NAME = "district_name";
    }
    public static final class MarketEntry implements BaseColumns {
        public static final String TABLE_NAME = "market";
        public static final String COLUMN_STATE_KEY = "state_id";
        public static final String COLUMN_DISTRICT_KEY = "district_id";
        public static final String COLUMN_MARKET_NAME = "market_name";
    }

    public static final class CommodityEntry implements BaseColumns {
        public static final String TABLE_NAME = "commodity";
        // "Chapathi"
        public static final String COLUMN_VARIETY = "variety";
        // "Anantapur"
        public static final String COLUMN_DISTRICT = "district";
        // "Tamarind fruit"
        public static final String COLUMN_COMMODITY = "commodity";
        // Column with the foreign key into the market table.
        public static final String COLUMN_MARKET_KEY = "market_id";
        // "13/03/2016"
        public static final String COLUMN_ARRIVAL_DATE = "arrival_date";
        // "8000"
        public static final String COLUMN_MAX_PRICE = "max_price";
        // "8500"
        public static final String COLUMN_MODAL_PRICE = "modal_price";
        // "9000"
        public static final String COLUMN_MIN_PRICE = "min_price";
    }
}
