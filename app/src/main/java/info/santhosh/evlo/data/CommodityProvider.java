package info.santhosh.evlo.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by santhoshvai on 16/03/16.
 */
public class CommodityProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private CommodityDbHelper mOpenHelper;

    static final int COMMODITY_DATA = 100;
    static final int COMMODITY_DATA_WITH_MARKET = 101;
    static final int ALL_COMMODITY_DATA_FOR_MARKET = 102;
    static final int ALL_COMMODITY_DATA_FOR_STATE = 103;
    static final int ALL_COMMODITY_DATA_FOR_COMMODITY_NAME = 104;
    static final int COMMODITY_NAME = 200;
    static final int MARKET = 300;
    static final int DISTRICT = 400;
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

    //state.state_name = ?
    private static final String sStateNameSelection =
            CommodityContract.StateEntry.TABLE_NAME+
                    "." + CommodityContract.StateEntry.COLUMN_STATE_NAME + " = ? ";

    //commodity_variety_name.commodity_name = ?
    private static final String sCommodityNameSelection =
                    CommodityContract.CommodityNameEntry.TABLE_NAME +
                    "." + CommodityContract.CommodityNameEntry.COLUMN_COMMODITY_NAME + " = ? ";

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
        //commodity_data
        matcher.addURI(authority, CommodityContract.PATH_COMMODITY_DATA, COMMODITY_DATA);
        // commodity_data/*/market/*
        matcher.addURI(authority, CommodityContract.PATH_COMMODITY_DATA + "/*" +
                CommodityContract.PATH_MARKET + "/*", COMMODITY_DATA_WITH_MARKET);
        matcher.addURI(authority, CommodityContract.PATH_MARKET + "/*", ALL_COMMODITY_DATA_FOR_MARKET);
        matcher.addURI(authority, CommodityContract.PATH_STATE + "/*", ALL_COMMODITY_DATA_FOR_STATE);
        // commodity_variety_name/*
        matcher.addURI(authority, CommodityContract.PATH_COMMODITY_NAME
                + "/*", ALL_COMMODITY_DATA_FOR_COMMODITY_NAME);
        matcher.addURI(authority, CommodityContract.PATH_COMMODITY_NAME, COMMODITY_NAME);
        matcher.addURI(authority, CommodityContract.PATH_MARKET, MARKET);
        matcher.addURI(authority, CommodityContract.PATH_DISTRICT, DISTRICT);
        matcher.addURI(authority, CommodityContract.PATH_STATE, STATE);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new CommodityDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // commodity_data/*/market/*
            case COMMODITY_DATA_WITH_MARKET:
            {
                String commodity = CommodityContract.CommodityDataEntry.getCommodityFromUri(uri);
                String market = CommodityContract.CommodityDataEntry.getMarketFromUri(uri);
                retCursor = sCommodityByMarketQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        sMarketNameWithCommodityNameSelection, // selection
                        new String[]{commodity, market}, //selectionArgs
                        null, //groupBy
                        null, //having
                        sortOrder
                );
                break;
            }
            // "commodity_data/*"
            case COMMODITY_DATA:
            {
                String commodity = CommodityContract.CommodityDataEntry.getCommodityFromUri(uri);
                retCursor = sCommodityByMarketQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        sCommodityNameSelection, // selection
                        new String[]{commodity}, //selectionArgs
                        null, //groupBy
                        null, //having
                        sortOrder
                );
                break;
            }
            // "market/*"
            case ALL_COMMODITY_DATA_FOR_MARKET: {
                String market = CommodityContract.MarketEntry.getMarketFromUri(uri);
                retCursor = sCommodityByMarketQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        sMarketNameSelection, // selection
                        new String[]{market}, //selectionArgs
                        null, //groupBy
                        null, //having
                        sortOrder
                );
                break;
            }
            // "state/*"
            case ALL_COMMODITY_DATA_FOR_STATE: {
                String state = CommodityContract.StateEntry.getStateFromUri(uri);
                retCursor = sCommodityByMarketQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        sStateNameSelection, // selection
                        new String[]{state}, //selectionArgs
                        null, //groupBy
                        null, //having
                        sortOrder
                );
                break;
            }
            // "commodity_name"
            case COMMODITY_NAME: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        CommodityContract.CommodityNameEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "market"
            case MARKET: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        CommodityContract.MarketEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "district"
            case DISTRICT: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        CommodityContract.DistrictEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "state"
            case STATE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        CommodityContract.StateEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Setting the notification URI of the cursor to the one that was passed causes the cursor
        // to register a content observer, to watch for changes that happen to that URI and any of
        // its descendants. This allows the content provider to easily tell the UI when the
        // cursor changes, on operations like database insert or update.
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    /*
     Only to find what type of data is given by the returned database cursor. (ITEM or DIR)
    * */
    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case COMMODITY_DATA_WITH_MARKET:
                return CommodityContract.CommodityDataEntry.CONTENT_TYPE;
            case COMMODITY_DATA:
                return CommodityContract.CommodityDataEntry.CONTENT_TYPE;
            case ALL_COMMODITY_DATA_FOR_MARKET:
                return CommodityContract.CommodityDataEntry.CONTENT_TYPE;
            case ALL_COMMODITY_DATA_FOR_STATE:
                return CommodityContract.CommodityDataEntry.CONTENT_TYPE;
            case COMMODITY_NAME:
                return CommodityContract.CommodityNameEntry.CONTENT_TYPE;
            case MARKET:
                return CommodityContract.MarketEntry.CONTENT_TYPE;
            case DISTRICT:
                return CommodityContract.DistrictEntry.CONTENT_TYPE;
            case STATE:
                return CommodityContract.StateEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case COMMODITY_DATA: {
                long _id = db.insert(CommodityContract.CommodityDataEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = CommodityContract.CommodityDataEntry.buildCommodityDataUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case COMMODITY_NAME: {
                long _id = db.insert(CommodityContract.CommodityNameEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = CommodityContract.CommodityNameEntry.buildCommodityNameUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case MARKET: {
                long _id = db.insert(CommodityContract.MarketEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = CommodityContract.MarketEntry.buildMarketUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case STATE: {
                long _id = db.insert(CommodityContract.StateEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = CommodityContract.StateEntry.buildStateUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case DISTRICT: {
                long _id = db.insert(CommodityContract.DistrictEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = CommodityContract.DistrictEntry.buildDistrictUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case COMMODITY_DATA:
                rowsDeleted = db.delete(
                        CommodityContract.CommodityDataEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MARKET:
                rowsDeleted = db.delete(
                        CommodityContract.MarketEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case COMMODITY_NAME:
                rowsDeleted = db.delete(
                        CommodityContract.CommodityNameEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case STATE:
                rowsDeleted = db.delete(
                        CommodityContract.StateEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case DISTRICT:
                rowsDeleted = db.delete(
                        CommodityContract.DistrictEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case COMMODITY_DATA:
                rowsUpdated = db.update(
                        CommodityContract.CommodityDataEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case MARKET:
                rowsUpdated = db.update(
                        CommodityContract.MarketEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case COMMODITY_NAME:
                rowsUpdated = db.update(
                        CommodityContract.CommodityNameEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case STATE:
                rowsUpdated = db.update(
                        CommodityContract.StateEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case DISTRICT:
                rowsUpdated = db.update(
                        CommodityContract.DistrictEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case COMMODITY_DATA:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(CommodityContract.CommodityDataEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
