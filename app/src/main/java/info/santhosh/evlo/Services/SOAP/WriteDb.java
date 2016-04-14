package info.santhosh.evlo.Services.SOAP;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;

import java.util.List;
import java.util.Vector;

import info.santhosh.evlo.data.CommodityContract;
import info.santhosh.evlo.data.CommodityContract.CommodityDataEntry;

/**
 * Created by santhoshvai on 09/04/16.
 */
public class WriteDb {

    static final String TAG = "WriteDb";

    private final Context mContext;

    public WriteDb(Context context) {
        mContext = context;
    }
    // INSERT VALUES TO TABLE HERE

    // commodity_name -> get Id
    // use both id to build commodityData table
    public void usingCommoditiesList(List<Commodity> commodities) {
        // Insert the new weather information into the database
        Vector<ContentValues> cVVector = new Vector<ContentValues>(commodities.size());

        // storing intermediate market values, to prevent check in db
        SimpleArrayMap<String, Long> marketMap = new SimpleArrayMap<>();
        // storing intermediate commodity name values, to prevent check in db
        SimpleArrayMap<String, Long> commodityMap = new SimpleArrayMap<>();

        for(Commodity commodity : commodities) {
            ContentValues commodityValues = new ContentValues();
            long marketId;
            if(marketMap.containsKey(commodity.getMarket())) {
                marketId = marketMap.get(commodity.getMarket());
            } else {
                // STATE -> DISTRICT -> MARKET -> get Id
                long stateId = addState(commodity.getState());
                long districtId = addDistrict(commodity.getDistrict(), stateId);
                marketId = addMarket(commodity.getMarket(), districtId);
                marketMap.put(commodity.getMarket(), marketId);
            }
            // commodity_name -> get Id
            long commodityId = addCommodityName(commodity.getCommodity(), commodity.getVariety());
            commodityValues.put(CommodityDataEntry.COLUMN_COMMODITY_KEY, commodityId);
            commodityValues.put(CommodityDataEntry.COLUMN_MARKET_KEY, marketId);
            commodityValues.put(CommodityDataEntry.COLUMN_ARRIVAL_DATE, commodity.getArrival_Date());
            commodityValues.put(CommodityDataEntry.COLUMN_MAX_PRICE, commodity.getMax_Price());
            commodityValues.put(CommodityDataEntry.COLUMN_MIN_PRICE, commodity.getMin_Price());
            commodityValues.put(CommodityDataEntry.COLUMN_MODAL_PRICE, commodity.getModal_Price());

            cVVector.add(commodityValues);
        }

        // add to database
        int inserted = 0;
        if ( cVVector.size() > 0 ) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            inserted = mContext.getContentResolver().bulkInsert(CommodityDataEntry.CONTENT_URI, cvArray);
        }
        Log.d(TAG, "WriteDb Complete. " + inserted + " Inserted");
    }

    long addState(String stateName) {
        long stateId;

        // First, check if the state with this name exists in the db
        Cursor stateCursor = mContext.getContentResolver().query(
                CommodityContract.StateEntry.CONTENT_URI, // uri
                new String[]{CommodityContract.StateEntry._ID}, // projection
                CommodityContract.StateEntry.COLUMN_STATE_NAME + " = ?", // selection
                new String[]{stateName}, // selectionArgs
                null); // sortOrder

        if (stateCursor.moveToFirst()) { // already exists
            int idIndex = stateCursor.getColumnIndex(CommodityContract.StateEntry._ID);
            stateId = stateCursor.getLong(idIndex);
        } else {
            // Now that the content provider is set up, inserting rows of data is pretty simple.
            // First create a ContentValues object to hold the data you want to insert.
            ContentValues stateValues = new ContentValues();

            // Then add the data, along with the corresponding name of the data type,
            // so the content provider knows what kind of value is being inserted.
            stateValues.put(CommodityContract.StateEntry.COLUMN_STATE_NAME, stateName);

            // Finally, insert location data into the database.
            Uri insertedUri = mContext.getContentResolver().insert(
                    CommodityContract.StateEntry.CONTENT_URI,
                    stateValues
            );

            // The resulting URI contains the ID for the row.  Extract the locationId from the Uri.
            stateId = ContentUris.parseId(insertedUri);
        }

        stateCursor.close();
        // Wait, that worked?  Yes!
        return stateId;
    }

    long addDistrict(String districtName, long stateId) {
        long districtId;

        // First, check if the district with this name exists in the db
        Cursor cursor = mContext.getContentResolver().query(
                CommodityContract.DistrictEntry.CONTENT_URI, // uri
                new String[]{CommodityContract.DistrictEntry._ID}, // projection
                CommodityContract.DistrictEntry.COLUMN_DISTRICT_NAME + " = ? AND " +
                        CommodityContract.DistrictEntry.COLUMN_STATE_KEY + " = ?", // selection
                new String[]{districtName, Long.toString(stateId)}, // selectionArgs
                null); // sortOrder

        if (cursor.moveToFirst()) { // already exists
            int idIndex = cursor.getColumnIndex(CommodityContract.DistrictEntry._ID);
            districtId = cursor.getLong(idIndex);
        } else {

            ContentValues values = new ContentValues();

            values.put(CommodityContract.DistrictEntry.COLUMN_DISTRICT_NAME, districtName);
            values.put(CommodityContract.DistrictEntry.COLUMN_STATE_KEY, stateId);

            // Finally, insert location data into the database.
            Uri insertedUri = mContext.getContentResolver().insert(
                    CommodityContract.DistrictEntry.CONTENT_URI,
                    values
            );

            // The resulting URI contains the ID for the row.  Extract the locationId from the Uri.
            districtId = ContentUris.parseId(insertedUri);
        }

        cursor.close();
        // Wait, that worked?  Yes!
        return districtId;
    }

    long addMarket(String marketName, long districtId) {
        long marketId;

        // First, check if the district with this name exists in the db
        Cursor cursor = mContext.getContentResolver().query(
                CommodityContract.MarketEntry.CONTENT_URI, // uri
                new String[]{CommodityContract.MarketEntry._ID}, // projection
                CommodityContract.MarketEntry.COLUMN_MARKET_NAME + " = ? AND " +
                        CommodityContract.MarketEntry.COLUMN_DISTRICT_KEY + " = ?", // selection
                new String[]{marketName, Long.toString(districtId)}, // selectionArgs
                null); // sortOrder

        if (cursor.moveToFirst()) { // already exists
            int idIndex = cursor.getColumnIndex(CommodityContract.MarketEntry._ID);
            marketId = cursor.getLong(idIndex);
        } else {

            ContentValues values = new ContentValues();

            values.put(CommodityContract.MarketEntry.COLUMN_MARKET_NAME, marketName);
            values.put(CommodityContract.MarketEntry.COLUMN_DISTRICT_KEY, districtId);

            // Finally, insert location data into the database.
            Uri insertedUri = mContext.getContentResolver().insert(
                    CommodityContract.MarketEntry.CONTENT_URI,
                    values
            );

            // The resulting URI contains the ID for the row.  Extract the locationId from the Uri.
            marketId = ContentUris.parseId(insertedUri);
        }

        cursor.close();
        return marketId;
    }

    long addCommodityName(String commodityName, String variety) {
        long commodityId;

        // First, check if the district with this name exists in the db
        Cursor cursor = mContext.getContentResolver().query(
                CommodityContract.CommodityNameEntry.CONTENT_URI, // uri
                new String[]{CommodityContract.CommodityNameEntry._ID}, // projection
                CommodityContract.CommodityNameEntry.COLUMN_COMMODITY_NAME + " = ? AND " +
                        CommodityContract.CommodityNameEntry.COLUMN_VARIETY + " = ?", // selection
                new String[]{commodityName, variety}, // selectionArgs
                null); // sortOrder

        if (cursor.moveToFirst()) { // already exists
            int idIndex = cursor.getColumnIndex(CommodityContract.MarketEntry._ID);
            commodityId = cursor.getLong(idIndex);
        } else {

            ContentValues values = new ContentValues();

            values.put(CommodityContract.CommodityNameEntry.COLUMN_COMMODITY_NAME, commodityName);
            values.put(CommodityContract.CommodityNameEntry.COLUMN_VARIETY, variety);

            // Finally, insert location data into the database.
            Uri insertedUri = mContext.getContentResolver().insert(
                    CommodityContract.CommodityNameEntry.CONTENT_URI,
                    values
            );

            // The resulting URI contains the ID for the row.  Extract the locationId from the Uri.
            commodityId = ContentUris.parseId(insertedUri);
        }

        cursor.close();
        return commodityId;
    }
}
