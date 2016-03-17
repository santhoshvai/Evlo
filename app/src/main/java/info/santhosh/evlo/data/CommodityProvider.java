package info.santhosh.evlo.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by santhoshvai on 16/03/16.
 */
public class CommodityProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private CommodityDbHelper mOpenHelper;

    static final int COMMODITY_DATA = 100;
    static final int COMMODITY_DATA_WITH_MARKET = 101;
    static final int COMMODITY_DATA_WITH_STATE = 102;
    static final int STATE = 500;

    private static final SQLiteQueryBuilder sCommodityByMarketQueryBuilder;

    static{
        sCommodityByMarketQueryBuilder = new SQLiteQueryBuilder();

        /*
        from commodity_data
        INNER JOIN market ON commodity_data.market_id = market._id
        INNER JOIN commodity_variety_name ON commodity_data.commodity_id = commodity_variety_name._id
        INNER JOIN district ON market.district_id = district._id
        INNER JOIN state ON district.state_id = state._id
         */
        sCommodityByMarketQueryBuilder.setTables(
                CommodityContract.CommodityDataEntry.TABLE_NAME + " INNER JOIN " +
                        CommodityContract.MarketEntry.TABLE_NAME +
                        " ON " + CommodityContract.CommodityDataEntry.TABLE_NAME +
                        "." + CommodityContract.CommodityDataEntry.COLUMN_MARKET_KEY +
                        " = " + CommodityContract.MarketEntry.TABLE_NAME +
                        "." + CommodityContract.MarketEntry._ID
                        + " INNER JOIN " +
                        CommodityContract.CommodityNameEntry.TABLE_NAME +
                        " ON " + CommodityContract.CommodityDataEntry.TABLE_NAME +
                        "." + CommodityContract.CommodityDataEntry.COLUMN_COMMODITY_KEY +
                        " = " + CommodityContract.CommodityNameEntry.TABLE_NAME +
                        "." + CommodityContract.CommodityNameEntry._ID
                        + " INNER JOIN " +
                        CommodityContract.DistrictEntry.TABLE_NAME +
                        " ON " + CommodityContract.MarketEntry.TABLE_NAME +
                        "." + CommodityContract.MarketEntry.COLUMN_DISTRICT_KEY +
                        " = " + CommodityContract.DistrictEntry.TABLE_NAME +
                        "." + CommodityContract.CommodityNameEntry._ID
                        + " INNER JOIN " +
                        CommodityContract.StateEntry.TABLE_NAME +
                        " ON " + CommodityContract.DistrictEntry.TABLE_NAME +
                        "." + CommodityContract.DistrictEntry.COLUMN_STATE_KEY +
                        " = " + CommodityContract.StateEntry.TABLE_NAME +
                        "." + CommodityContract.StateEntry._ID
        );
    }

    //market.market_name = ?
    private static final String sMarketNameSelection =
            CommodityContract.MarketEntry.TABLE_NAME+
                    "." + CommodityContract.MarketEntry.COLUMN_MARKET_NAME + " = ? ";

    //market.market_name = ? AND commodity_variety_name.commodity_name = ?
    private static final String sMarketNameWithCommodityNameSelection =
            CommodityContract.MarketEntry.TABLE_NAME +
                    "." + CommodityContract.MarketEntry.COLUMN_MARKET_NAME + " = ? AND " +
                    CommodityContract.CommodityNameEntry.TABLE_NAME +
                    "." + CommodityContract.CommodityNameEntry.COLUMN_COMMODITY_NAME + " = ? ";


    static UriMatcher buildUriMatcher() {
        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = CommodityContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, CommodityContract.PATH_COMMODITY_DATA, COMMODITY_DATA);
        matcher.addURI(authority, CommodityContract.PATH_COMMODITY_DATA + "/*", COMMODITY_DATA_WITH_MARKET);

        matcher.addURI(authority, CommodityContract.PATH_STATE, STATE);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new CommodityDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    /*
     Only to find what type of data is given by the returned database cursor. (ITEM or DIR)
    * */
    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // Student: Uncomment and fill out these two cases
            case COMMODITY_DATA_WITH_MARKET:
                return CommodityContract.CommodityDataEntry.CONTENT_ITEM_TYPE;
            case COMMODITY_DATA:
                return CommodityContract.CommodityDataEntry.CONTENT_TYPE;
            case STATE:
                return CommodityContract.StateEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
