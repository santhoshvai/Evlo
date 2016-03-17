package info.santhosh.evlo.data;

/**
 * Created by santhoshvai on 15/03/16.
 */

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

import java.util.Map;
import java.util.Set;

public class TestUtilities extends AndroidTestCase {

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    static ContentValues createCoconutCommodityDataValues(long commodityNameRowID, long marketRowId) {
        ContentValues testValues = new ContentValues();
        testValues.put(CommodityContract.CommodityDataEntry.COLUMN_COMMODITY_KEY, commodityNameRowID);
        testValues.put(CommodityContract.CommodityDataEntry.COLUMN_MARKET_KEY, marketRowId);
        testValues.put(CommodityContract.CommodityDataEntry.COLUMN_ARRIVAL_DATE, "15/03/2016");
        testValues.put(CommodityContract.CommodityDataEntry.COLUMN_MIN_PRICE, "900");
        testValues.put(CommodityContract.CommodityDataEntry.COLUMN_MODAL_PRICE, "1100");
        testValues.put(CommodityContract.CommodityDataEntry.COLUMN_MAX_PRICE, "1000");
        return testValues;
    }

    static ContentValues createCoconutCommodityNameValues() {
        ContentValues testValues = new ContentValues();
        testValues.put(CommodityContract.CommodityNameEntry.COLUMN_COMMODITY_NAME, "Coconut");
        testValues.put(CommodityContract.CommodityNameEntry.COLUMN_VARIETY, "Coconut");
        return testValues;
    }

    static ContentValues createSenjeriMarketValues(long districtRowId) {
        ContentValues testValues = new ContentValues();
        testValues.put(CommodityContract.MarketEntry.COLUMN_MARKET_NAME, "Senieri");
        testValues.put(CommodityContract.MarketEntry.COLUMN_DISTRICT_KEY, districtRowId);
        return testValues;
    }

    static ContentValues createCoimbatoreDistrictValues(long stateRowId) {
        ContentValues testValues = new ContentValues();
        testValues.put(CommodityContract.DistrictEntry.COLUMN_DISTRICT_NAME, "Coimbatore");
        testValues.put(CommodityContract.DistrictEntry.COLUMN_STATE_KEY, stateRowId);
        return testValues;
    }

    static ContentValues createTamilNaduStateValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(CommodityContract.StateEntry.COLUMN_STATE_NAME, "Tamil Nadu");
        return testValues;
    }

    /* Test Entry
        <State>Tamil Nadu</State>
        <District>Coimbatore</District>
        <Market>Senjeri</Market>
        <Commodity>Coconut</Commodity>
        <Variety>Coconut</Variety>
        <Arrival_Date>15/03/2016</Arrival_Date>
        <Min_x0020_Price>900</Min_x0020_Price>
        <Max_x0020_Price>1100</Max_x0020_Price>
        <Modal_x0020_Price>1000</Modal_x0020_Price>

        http://sqlfiddle.com/#!5/5d1f27/1
    */
}