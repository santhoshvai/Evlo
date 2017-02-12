package info.santhosh.evlo.common;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;
import java.util.Vector;

import info.santhosh.evlo.data.CommodityContract;
import info.santhosh.evlo.data.CommodityContract.CommodityDataEntry;
import info.santhosh.evlo.service.SOAP.xmlModels.Commodity;

/**
 * Created by santhoshvai on 09/04/16.
 */
public class WriteDb {

    static final String TAG = "WriteDb";

    private final Context mContext;

    public WriteDb(Context context) {
        mContext = context.getApplicationContext();
    }
    // INSERT VALUES TO TABLE HERE

    // commodity_name -> get Id
    // use both id to build commodityData table
    public void usingCommoditiesList(List<Commodity> commodities) {
        // Insert the new commodity information into the database
        Vector<ContentValues> cVVector = new Vector<ContentValues>(commodities.size());

        for(Commodity commodity : commodities) {
            ContentValues commodityValues = new ContentValues();
            commodityValues.put(CommodityDataEntry.COLUMN_COMMODITY_NAME, commodity.getCommodity());
            commodityValues.put(CommodityDataEntry.COLUMN_VARIETY, commodity.getVariety());
            commodityValues.put(CommodityDataEntry.COLUMN_ARRIVAL_DATE, commodity.getArrival_Date());
            commodityValues.put(CommodityDataEntry.COLUMN_MAX_PRICE, commodity.getMax_Price());
            commodityValues.put(CommodityDataEntry.COLUMN_MIN_PRICE, commodity.getMin_Price());
            commodityValues.put(CommodityDataEntry.COLUMN_MODAL_PRICE, commodity.getModal_Price());
            commodityValues.put(CommodityDataEntry.COLUMN_MARKET_NAME, commodity.getMarket());
            commodityValues.put(CommodityDataEntry.COLUMN_DISTRICT_NAME, commodity.getDistrict());
            commodityValues.put(CommodityDataEntry.COLUMN_STATE_NAME, commodity.getState());
            cVVector.add(commodityValues);
        }

        // add to database
        int inserted = 0;
        if ( cVVector.size() > 0 ) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            inserted = mContext.getContentResolver().bulkInsert(CommodityDataEntry.CONTENT_URI, cvArray);
        }
        Log.d(TAG, "WriteDb List Complete. " + inserted + " Inserted");
    }

    public @Nullable Uri addUsingCommoditiesFavId(int favId) {
        ContentValues commodityFavValues = new ContentValues();
        commodityFavValues.put(CommodityContract.CommodityFavEntry.COLUMN_FAV_ID, favId);
        return mContext.getContentResolver().insert(CommodityContract.CommodityFavEntry.CONTENT_URI, commodityFavValues);
    }

    public int removeUsingCommoditiesFavId(int favId) {
        return mContext.getContentResolver().delete(CommodityContract.CommodityFavEntry.CONTENT_URI,
                CommodityContract.CommodityFavEntry.COLUMN_FAV_ID + "=?",
                new String[] { Integer.toString(favId) });
    }

}
