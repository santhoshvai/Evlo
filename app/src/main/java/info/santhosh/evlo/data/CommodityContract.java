package info.santhosh.evlo.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by santhoshvai on 13/03/16.
 */
public class CommodityContract {
    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "info.santhosh.evlo";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Possible paths (appended to base content URI for possible URI's)
    public static final String PATH_COMMODITY_DATA = "commodity_data";
    public static final String PATH_COMMODITY_VARIETY = "commodity_variety";
    public static final String PATH_COMMODITY_VARIETY_DETAIL = "commodity_variety_detail";
    public static final String PATH_COMMODITY_FAV = "commodity_fav";

    public static final class CommodityDataEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_COMMODITY_DATA).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COMMODITY_DATA;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COMMODITY_DATA;

        public static final String TABLE_NAME = "commodity_data";
        // "Local"
        public static final String COLUMN_VARIETY = "variety";
        // "Onion"
        public static final String COLUMN_COMMODITY_NAME = "commodity_name";

        // "13/03/2016"
        public static final String COLUMN_ARRIVAL_DATE = "arrival_date";
        // "8000"
        public static final String COLUMN_MAX_PRICE = "max_price";
        // "8500"
        public static final String COLUMN_MODAL_PRICE = "modal_price";
        // "9000"
        public static final String COLUMN_MIN_PRICE = "min_price";

        public static final String COLUMN_MARKET_NAME = "market_name";

        public static final String COLUMN_DISTRICT_NAME = "district_name";

        public static final String COLUMN_STATE_NAME = "state_name";

        public static Uri buildCommodityDataUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        // commodity_data/commodity_variety
        public static Uri buildAllCommodityNames() {
            return CONTENT_URI.buildUpon().appendPath(PATH_COMMODITY_VARIETY).build();
        }

        // commodity_data/*
        public static Uri buildCommodityNameDetailUri(String commodityName) {
            return CONTENT_URI.buildUpon().appendPath(PATH_COMMODITY_VARIETY_DETAIL)
                    .appendPath(commodityName).build();
        }

        // commodity_data/commodity_variety/*
        public static Uri buildCommodityNameSearchUri(String commodityName) {
            return CONTENT_URI.buildUpon().appendPath(PATH_COMMODITY_VARIETY)
                    .appendPath(commodityName).build();
        }

        public static String getCommodityNameFromUri(Uri uri) {
            return uri.getLastPathSegment();
        }

    }

    public static final class CommodityFavEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_COMMODITY_FAV).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COMMODITY_FAV;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_COMMODITY_FAV;

        public static final String TABLE_NAME = "commodity_fav";

        public static final String COLUMN_FAV_ID = "fav_id";

        public static Uri buildCommodityFavUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        // content://{package_name}/commodity_fav
        public static Uri buildAllFavsCommodityDetails() {
            return CONTENT_URI;
        }

    }
}
