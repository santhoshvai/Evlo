package info.santhosh.evlo.data;

/**
 * Created by santhoshvai on 15/03/16.
 */
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import java.util.HashSet;

public class TestDb extends AndroidTestCase {

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    // Since we want each test to start with a clean slate
    void deleteTheDatabase() {
        mContext.deleteDatabase(CommodityDbHelper.DATABASE_NAME);
    }

    /*
        This function gets called before each test is executed to delete the database.  This makes
        sure that we always have a clean test.
     */
    public void setUp() {
        deleteTheDatabase();
    }

    public void testCreateDb() throws Throwable {
        Log.d(LOG_TAG, "testCreateDb");
        // build a HashSet of all of the table names we wish to look for
        // Note that there will be another table in the DB that stores the
        // Android metadata (db version information)
        final HashSet<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(CommodityContract.StateEntry.TABLE_NAME);
        tableNameHashSet.add(CommodityContract.DistrictEntry.TABLE_NAME);
        tableNameHashSet.add(CommodityContract.MarketEntry.TABLE_NAME);
        tableNameHashSet.add(CommodityContract.CommodityNameEntry.TABLE_NAME);
        tableNameHashSet.add(CommodityContract.CommodityDataEntry.TABLE_NAME);

        mContext.deleteDatabase(CommodityDbHelper.DATABASE_NAME);
        SQLiteDatabase db = new CommodityDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        // have we created the tables we want?
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

        assertTrue("Error: This means that the database has not been created correctly",
                c.moveToFirst());

        // verify that the tables have been created
        do {
            tableNameHashSet.remove(c.getString(0));
        } while( c.moveToNext() );

        // if this fails, it means that your database doesn't contain both the location entry
        // and weather entry tables
        assertTrue("Error: Your database lacked a column",
                tableNameHashSet.isEmpty());

        // now, do our tables contain the correct columns?
        c = db.rawQuery("PRAGMA table_info(" + CommodityContract.StateEntry.TABLE_NAME + ")",
                null);

        assertTrue("Error: This means that we were unable to query the database for table information.",
                c.moveToFirst());

        // Build a HashSet of all of the column names we want to look for
        final HashSet<String> locationColumnHashSet = new HashSet<String>();
        locationColumnHashSet.add(CommodityContract.StateEntry._ID);
        locationColumnHashSet.add(CommodityContract.StateEntry.COLUMN_STATE_NAME);

        int columnNameIndex = c.getColumnIndex("name");
        do {
            String columnName = c.getString(columnNameIndex);
            locationColumnHashSet.remove(columnName);
        } while(c.moveToNext());

        // if this fails, it means that your database doesn't contain all of the required state
        // entry columns
        assertTrue("Error: The database doesn't contain all of the required location entry columns",
                locationColumnHashSet.isEmpty());
        db.close();
    }

    public void testCommodityDataTable() {
        insertCommodityDataTable();
    }

    public long insertCommodityDataTable() {
        long commodityNameRowId = insertCommodityNameTable();
        long marketRowId = insertMarket();
        // Second Step: Create ContentValues of what you want to insert
        ContentValues testValues = TestUtilities.createCoconutCommodityDataValues(
                commodityNameRowId,marketRowId);
        // 3,4,5,6 STEP
        return insertTable(CommodityContract.CommodityDataEntry.TABLE_NAME, testValues);
    }

    public void testCommodityNameTable() {
        insertCommodityNameTable();
    }

    public long insertCommodityNameTable() {
        // Second Step: Create ContentValues of what you want to insert
        ContentValues testValues = TestUtilities.createCoconutCommodityNameValues();
        // 3,4,5,6 STEP
        return insertTable(CommodityContract.CommodityNameEntry.TABLE_NAME, testValues);
    }

    public void testMarketTable() {
        insertMarket();
    }

    public long insertMarket() {
        long districtRowId = insertDistrict();
        // Second Step: Create ContentValues of what you want to insert
        ContentValues testValues = TestUtilities.createSenjeriMarketValues(districtRowId);
        // 3,4,5,6 STEP
        return insertTable(CommodityContract.MarketEntry.TABLE_NAME, testValues);
    }

    public void testDistrictTable() {
        insertDistrict();
    }

    public long insertDistrict() {
        long stateRowId = insertState();
        // Second Step: Create ContentValues of what you want to insert
        ContentValues testValues = TestUtilities.createCoimbatoreDistrictValues(stateRowId);
        // 1,3,4,5,6 STEP
        return insertTable(CommodityContract.DistrictEntry.TABLE_NAME, testValues);
    }

    public void testStateTable() {
        insertState();
    }

    public long insertState() {
        // Second Step: Create ContentValues of what you want to insert
        ContentValues testValues = TestUtilities.createTamilNaduStateValues();

        // 1,3,4,5,6 STEP
        return insertTable(CommodityContract.StateEntry.TABLE_NAME, testValues);
    }

    public long insertTable(String tableName, ContentValues contentValues) {
        // First step: Get reference to writable database
        // If there's an error in those massive SQL table creation Strings,
        // errors will be thrown here when you try to get a writable database.
        CommodityDbHelper dbHelper = new CommodityDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Third Step: Insert ContentValues into database and get a row ID back
        long rowId;
        rowId = db.insert(tableName, null, contentValues);

        // Verify we got a row back.
        assertTrue(rowId != -1);

        // Data's inserted.  IN THEORY.  Now pull some out to stare at it and verify it made
        // the round trip.

        // Fourth Step: Query the database and receive a Cursor back
        // A cursor is your primary interface to the query results.
        Cursor cursor = db.query(
                tableName,  // Table to Query
                null, // all columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null, // columns to group by
                null, // columns to filter by row groups
                null // sort order
        );

        // Move the cursor to a valid database row and check to see if we got any records back
        // from the query
        assertTrue( String.format("Error: No Records returned from %s query", tableName)
                , cursor.moveToFirst() );

        // Fifth Step: Validate data in resulting Cursor with the original ContentValues
        // (you can use the validateCurrentRecord function in TestUtilities to validate the
        // query if you like)
        TestUtilities.validateCurrentRecord(
                String.format("Error: %s Query Validation Failed", tableName),
                cursor, contentValues);

        // Move the cursor to demonstrate that there is only one record in the database
        assertFalse(
                String.format("Error: More than one record returned from %s query", tableName),
                cursor.moveToNext() );

        // Sixth Step: Close Cursor and Database
        cursor.close();
        db.close();
        return rowId;
    }
}