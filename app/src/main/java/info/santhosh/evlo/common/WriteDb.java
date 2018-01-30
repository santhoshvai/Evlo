package info.santhosh.evlo.common;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Date;
import java.util.List;
import java.util.Vector;

import info.santhosh.evlo.data.CommodityContract;
import info.santhosh.evlo.data.CommodityContract.CommodityDataEntry;
import info.santhosh.evlo.model.CommodityProtos;
import info.santhosh.evlo.service.SOAP.xmlModels.Commodity;

/**
 * Created by santhoshvai on 09/04/16.
 */
public class WriteDb {

    static final String TAG = "WriteDb";

    // INSERT VALUES TO TABLE HERE
    // commodity_name -> get Id
    // use both id to build commodityData table
    public static void usingCommoditiesList(@NonNull Context context, List<Commodity> commodities) {
        if (commodities == null || commodities.isEmpty()) return;

        // Insert the new commodity information into the database
        Vector<ContentValues> cVVector = new Vector<ContentValues>(commodities.size());

        int i = 0;
        // arrival date is unique per xml, means unique per day
        // hence we can skip previously obtained data on the same day
        Date lastStoredDate = EvloPrefs.getLastArrivalDate(context);
        Date newArrivalDate = Utils.convertArrivalDate(commodities.get(0).getArrival_Date());
        if (lastStoredDate != null && newArrivalDate != null && lastStoredDate.equals(newArrivalDate)) {
            i = EvloPrefs.getLastRowOrder(context); // last stored index of array would be row_order - 1
        }

        while (i < commodities.size()) {
            final Commodity commodity = commodities.get(i);
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
            i++;
        }

        // add to database
        int inserted = 0;
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            inserted = context.getContentResolver().bulkInsert(CommodityDataEntry.CONTENT_URI, cvArray);
        }

        final Commodity finalCommodity = commodities.get(commodities.size() - 1);
        EvloPrefs.setLastArrivalDate(context, finalCommodity.getArrival_Date());
        EvloPrefs.setLastRowOrder(context, Integer.parseInt(finalCommodity.getRowOrder()));

        Log.d(TAG, "WriteDb List Complete. " + inserted + " Inserted");
    }

    public static void usingProtos(@NonNull Context context, CommodityProtos.Commodities protos, @Nullable WriteDbProgressListener progressListener) {
        if (protos == null || protos.getCommodityCount() < 1) {
            if (progressListener != null) progressListener.update(1,1, true);
            return;
        }

        // Insert the new commodity information into the database
        Vector<ContentValues> cvVector = new Vector<>(protos.getCommodityCount());

        int i = 0;
        // arrival date is unique per proto file, means unique per day
        // hence we can skip previously obtained data on the same day
        final long lastArrivalTimeStamp = EvloPrefs.getLastArrivalDateTimeStamp(context);
        final long newArrivalTimeStamp = protos.getCommodity(0).getArrivalTimeStamp();

        if (lastArrivalTimeStamp != -1 && lastArrivalTimeStamp == newArrivalTimeStamp) {
            i = EvloPrefs.getLastRowOrder(context); // last stored index of array would be row_order - 1
        }

        final int length = protos.getCommodityCount(); // we multiply by 2 as we give 50% weightage to bulkinsert
        while (i < protos.getCommodityCount()) {
            final CommodityProtos.Commodity commodity = protos.getCommodity(i);
            ContentValues commodityValues = new ContentValues();
            commodityValues.put(CommodityDataEntry.COLUMN_COMMODITY_NAME, commodity.getCommodity());
            commodityValues.put(CommodityDataEntry.COLUMN_VARIETY, commodity.getVariety());
            commodityValues.put(CommodityDataEntry.COLUMN_ARRIVAL_DATE, commodity.getArrivalTimeStamp());
            commodityValues.put(CommodityDataEntry.COLUMN_MAX_PRICE, commodity.getMaxPrice());
            commodityValues.put(CommodityDataEntry.COLUMN_MIN_PRICE, commodity.getMinPrice());
            commodityValues.put(CommodityDataEntry.COLUMN_MODAL_PRICE, commodity.getModalPrice());
            commodityValues.put(CommodityDataEntry.COLUMN_MARKET_NAME, commodity.getMarket());
            commodityValues.put(CommodityDataEntry.COLUMN_DISTRICT_NAME, commodity.getDistrict());
            commodityValues.put(CommodityDataEntry.COLUMN_STATE_NAME, commodity.getState());
            cvVector.add(commodityValues);
            i++;
            if (progressListener != null) {
                int progressTotal = length * 2; // we give the first 50% weightage, so 0..0.5
                progressListener.update(i , progressTotal, false);
            }
        }

        // add to database
        int inserted = 0;
        if (cvVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cvVector.size()];
            cvVector.toArray(cvArray);
            if (progressListener != null) DataFetchStatusProvider.getInstance(context).setWriteDbProgressListener(progressListener);
            inserted = context.getContentResolver().bulkInsert(CommodityDataEntry.CONTENT_URI, cvArray);
            if (progressListener != null) {
                progressListener.update(100, 100, true);
                DataFetchStatusProvider.getInstance(context).setWriteDbProgressListener(null);
            }
        }

        EvloPrefs.setLastArrivalDateTimeStamp(context, newArrivalTimeStamp);
        EvloPrefs.setLastRowOrder(context, protos.getCommodity(protos.getCommodityCount() -1).getRowOrder());

        Log.d(TAG, "WriteDb List Complete. " + inserted + " Inserted");
    }

    public static @Nullable Uri addUsingCommoditiesFavId(@NonNull Context context, int favId) {
        ContentValues commodityFavValues = new ContentValues();
        commodityFavValues.put(CommodityContract.CommodityFavEntry.COLUMN_FAV_ID, favId);

        return context.getContentResolver().insert(CommodityContract.CommodityFavEntry.CONTENT_URI, commodityFavValues);
    }

    public static int removeUsingCommoditiesFavId(@NonNull Context context, int favId) {
        return context.getContentResolver().delete(CommodityContract.CommodityFavEntry.CONTENT_URI,
                CommodityContract.CommodityFavEntry.COLUMN_FAV_ID + "=?",
                new String[] { Integer.toString(favId) });
    }

    public interface WriteDbProgressListener {
        void update(int doneSoFar, int length, boolean done);
    }

}
