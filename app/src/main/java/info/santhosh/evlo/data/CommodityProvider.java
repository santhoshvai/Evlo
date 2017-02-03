package info.santhosh.evlo.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Created by santhoshvai on 16/03/16.
 */
public class CommodityProvider extends ContentProvider {

    private final static String TAG = CommodityProvider.class.getSimpleName();

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private CommodityDbHelper mOpenHelper;

    static final int COMMODITY_DATA = 100;
    static final int COMMODITY_DATA_WITH_NAME = 101;
    static final int COMMODITY_NAME = 102;
    static final int SEARCH_FOR_COMMODITY_NAME = 103;

    static final int COMMODITY_FAV = 200;

    private static final SQLiteQueryBuilder sCommodityByMarketQueryBuilder;
    private static final SQLiteQueryBuilder sCommodityByFavouriteQueryBuilder;

    static{
        sCommodityByMarketQueryBuilder = new SQLiteQueryBuilder();
        sCommodityByFavouriteQueryBuilder = new SQLiteQueryBuilder();
        /*
        from commodity_data
         */
        sCommodityByMarketQueryBuilder.setTables(
                CommodityContract.CommodityDataEntry.TABLE_NAME
        );

        /*
        commodity_data INNER JOIN commodity_fav ON commodity_fav.fav_id = commodity_data._id
         */
        sCommodityByFavouriteQueryBuilder.setTables(
                CommodityContract.CommodityDataEntry.TABLE_NAME + " INNER JOIN " +
                        CommodityContract.CommodityFavEntry.TABLE_NAME +
                        " ON " + CommodityContract.CommodityFavEntry.TABLE_NAME +
                        "." + CommodityContract.CommodityFavEntry.COLUMN_FAV_ID +
                        " = " + CommodityContract.CommodityDataEntry.TABLE_NAME +
                        "." + CommodityContract.CommodityDataEntry._ID);
    }

    // commodity_data.commodity_name = ?
    private static final String sCommodityNameSelection =
                    CommodityContract.CommodityDataEntry.TABLE_NAME +
                    "." + CommodityContract.CommodityDataEntry.COLUMN_COMMODITY_NAME + " = ? ";

    // commodity_data.commodity_name = ?
    private static final String sCommodityNameSearchSelection =
            CommodityContract.CommodityDataEntry.TABLE_NAME +
                    "." + CommodityContract.CommodityDataEntry.COLUMN_COMMODITY_NAME + " LIKE ?";
    /* Stetho: SELECT DISTINCT commodity_name FROM commodity_data WHERE commodity_name LIKE "%apple%" */


    static UriMatcher buildUriMatcher() {
        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = CommodityContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        //commodity_data
        matcher.addURI(authority, CommodityContract.PATH_COMMODITY_DATA, COMMODITY_DATA);
        //commodity_data/commodity_variety_detail/*
        matcher.addURI(authority,CommodityContract.PATH_COMMODITY_DATA
                + "/"
                + CommodityContract.PATH_COMMODITY_VARIETY_DETAIL
                +"/*", COMMODITY_DATA_WITH_NAME);
        // commodity_data/commodity_variety/*
        matcher.addURI(authority, CommodityContract.PATH_COMMODITY_DATA
                + "/"
                + CommodityContract.PATH_COMMODITY_VARIETY
                + "/*", SEARCH_FOR_COMMODITY_NAME);
        // commodity_data/commodity_variety
        matcher.addURI(authority, CommodityContract.PATH_COMMODITY_DATA
                + "/"
                + CommodityContract.PATH_COMMODITY_VARIETY,
                COMMODITY_NAME);
        //commodity_fav
        matcher.addURI(authority, CommodityContract.PATH_COMMODITY_FAV,
                COMMODITY_FAV);
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
            // "commodity_data/*"
            case COMMODITY_DATA_WITH_NAME:
            {
                String commodity = CommodityContract.CommodityDataEntry.getCommodityNameFromUri(uri);
                retCursor = sCommodityByMarketQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        sCommodityNameSelection, // selection
                        new String[]{commodity}, //selectionArgs
                        null, //groupBy
                        null, //having
                        sortOrder
                );
                break;
            }
            // commodity_data/commodity_variety/*
            case SEARCH_FOR_COMMODITY_NAME: {
                String commodityName =  CommodityContract.CommodityDataEntry.getCommodityNameFromUri(uri);
                retCursor = mOpenHelper.getReadableDatabase().query(
                        true, // each row must be unique
                        CommodityContract.CommodityDataEntry.TABLE_NAME,
                        projection,
                        sCommodityNameSearchSelection,
                        new String[]{"%"+commodityName+"%"}, // example LIKE %apple%
                        CommodityContract.CommodityDataEntry.COLUMN_COMMODITY_NAME, // groupby
                        null,
                        sortOrder,
                        null
                );
                break;
            }
            // "commodity_data/commodity_variety" - DISTINCT: http://stackoverflow.com/a/13879436/3394023
            case COMMODITY_NAME: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        true, // distinct
                        CommodityContract.CommodityDataEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        CommodityContract.CommodityDataEntry.COLUMN_COMMODITY_NAME, // groupby
                        null,
                        sortOrder,
                        null // limit
                );
                break;
            }
            case COMMODITY_FAV: {
                retCursor = sCommodityByFavouriteQueryBuilder.query(
                        mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        CommodityContract.CommodityDataEntry.COLUMN_COMMODITY_NAME, //groupBy
                        null, //having
                        sortOrder,
                        null
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
            case SEARCH_FOR_COMMODITY_NAME:
                return CommodityContract.CommodityDataEntry.CONTENT_TYPE;
            case COMMODITY_DATA:
                return CommodityContract.CommodityDataEntry.CONTENT_TYPE;
            case COMMODITY_DATA_WITH_NAME:
                return CommodityContract.CommodityDataEntry.CONTENT_TYPE;
            case COMMODITY_NAME:
                return CommodityContract.CommodityDataEntry.CONTENT_TYPE;
            case COMMODITY_FAV:
                return CommodityContract.CommodityFavEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
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
            case COMMODITY_FAV: {
                long _id = db.insert(CommodityContract.CommodityFavEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = CommodityContract.CommodityFavEntry.buildCommodityFavUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        notifyChange(getContext(), uri);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
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
            case COMMODITY_FAV:
                rowsDeleted = db.delete(CommodityContract.CommodityFavEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsDeleted != 0) {
            notifyChange(getContext(), uri);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
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
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            notifyChange(getContext(), uri);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case COMMODITY_DATA:
                db.beginTransaction();
                // TODO: when arrival date is the same, dont update or insert (Logic must be in writeDb to send only needed ones)
                String selection = CommodityContract.CommodityDataEntry.COLUMN_COMMODITY_NAME +
                        "=? AND " +
                        CommodityContract.CommodityDataEntry.COLUMN_VARIETY +
                        "=? AND " +
                        CommodityContract.CommodityDataEntry.COLUMN_MARKET_NAME +
                        "=? AND " +
                        CommodityContract.CommodityDataEntry.COLUMN_DISTRICT_NAME +
                        "=?";
                String[] selectionArgs;
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        selectionArgs = new String[] {
                                value.getAsString(CommodityContract.CommodityDataEntry.COLUMN_COMMODITY_NAME),
                                value.getAsString(CommodityContract.CommodityDataEntry.COLUMN_VARIETY),
                                value.getAsString(CommodityContract.CommodityDataEntry.COLUMN_MARKET_NAME),
                                value.getAsString(CommodityContract.CommodityDataEntry.COLUMN_DISTRICT_NAME)};
                        // the row is updated if the above four values are same
                        int affected = db.update(
                                CommodityContract.CommodityDataEntry.TABLE_NAME, value, selection, selectionArgs);
                        if (affected == 0) { // only if no row was updated do the insert
                            long _id = db.insert(CommodityContract.CommodityDataEntry.TABLE_NAME, null, value);
                            if (_id != -1) {
                                returnCount++;
                            }
                        }
                    }
                    db.setTransactionSuccessful();
                } catch (SQLException e) {
                    Log.e(TAG, "BulkInsert", e);
                } finally {
                    db.endTransaction();
                }
                notifyChange(getContext(), uri);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    private void notifyChange(Context context, Uri uri) {
        try {
            ContentResolver contentResolver = context.getContentResolver();
            contentResolver.notifyChange(uri, null);
        } catch (NullPointerException e) {
            Log.e(TAG, "notifyChange: nullpointerException");
        }
    }
}
