package info.santhosh.evlo.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import info.santhosh.evlo.data.CommodityContract.StateEntry;
import info.santhosh.evlo.data.CommodityContract.DistrictEntry;
import info.santhosh.evlo.data.CommodityContract.MarketEntry;
import info.santhosh.evlo.data.CommodityContract.CommodityNameEntry;
import info.santhosh.evlo.data.CommodityContract.CommodityDataEntry;

/**
 * Manages a local database for commodity data
 * Created by santhoshvai on 13/03/16.
 */
public class CommodityDbHelper  extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "commodities.db";

    public CommodityDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a table to hold state name.
        final String SQL_CREATE_STATE_TABLE = "CREATE TABLE " + StateEntry.TABLE_NAME + " ( " +
                StateEntry._ID + " INTEGER PRIMARY KEY, " +
                StateEntry.COLUMN_STATE_NAME + " TEXT UNIQUE NOT NULL " +
                " );";
        // Create a table to hold district name.
        final String SQL_CREATE_DISTRICT_TABLE = "CREATE TABLE " + DistrictEntry.TABLE_NAME + " ( " +
                DistrictEntry._ID + " INTEGER PRIMARY KEY, " +
                DistrictEntry.COLUMN_DISTRICT_NAME + " TEXT UNIQUE NOT NULL, " +
                DistrictEntry.COLUMN_STATE_KEY + " INTEGER , " +
                " FOREIGN KEY (" + DistrictEntry.COLUMN_STATE_KEY + ") REFERENCES " +
                StateEntry.TABLE_NAME + " (" + StateEntry._ID + ") " +
                " );";
        // Create a table to hold market name.
        final String SQL_CREATE_MARKET_TABLE = "CREATE TABLE " + MarketEntry.TABLE_NAME + " ( " +
                MarketEntry._ID + " INTEGER PRIMARY KEY," +
                MarketEntry.COLUMN_MARKET_NAME + " TEXT UNIQUE NOT NULL, " +
                MarketEntry.COLUMN_DISTRICT_KEY + " INTEGER , " +
                " FOREIGN KEY (" + MarketEntry.COLUMN_DISTRICT_KEY + ") REFERENCES " +
                DistrictEntry.TABLE_NAME + " (" + DistrictEntry._ID + ") " +
                " );";
        // Create a table to hold market name.
        final String SQL_CREATE_COMMODITY_NAME_TABLE = "CREATE TABLE " + CommodityNameEntry.TABLE_NAME + " ( " +
                CommodityNameEntry._ID + " INTEGER PRIMARY KEY," +
                CommodityNameEntry.COLUMN_COMMODITY_NAME + " TEXT NOT NULL, " +
                CommodityNameEntry.COLUMN_VARIETY + " TEXT NOT NULL, " +
                // only one commodity_name/variety_name combo
                " UNIQUE (" + CommodityNameEntry.COLUMN_COMMODITY_NAME + ", " +
                CommodityNameEntry.COLUMN_VARIETY + ") ON CONFLICT REPLACE);";

        // Create a table to hold commmodity info.
        final String SQL_CREATE_COMMODITY_DATA_TABLE = "CREATE TABLE " + CommodityDataEntry.TABLE_NAME + " ( " +
                CommodityDataEntry._ID + " INTEGER PRIMARY KEY," +
                CommodityDataEntry.COLUMN_COMMODITY_KEY + " INTEGER , " +
                CommodityDataEntry.COLUMN_MARKET_KEY + " INTEGER , " +
                CommodityDataEntry.COLUMN_ARRIVAL_DATE + " TEXT NOT NULL, " +
                CommodityDataEntry.COLUMN_MAX_PRICE + " INTEGER NOT NULL, " +
                CommodityDataEntry.COLUMN_MODAL_PRICE + " INTEGER NOT NULL, " +
                CommodityDataEntry.COLUMN_MIN_PRICE + " INTEGER NOT NULL," +
                " FOREIGN KEY (" + CommodityDataEntry.COLUMN_COMMODITY_KEY + ") REFERENCES " +
                CommodityNameEntry.TABLE_NAME + " (" + CommodityNameEntry._ID + "), " +
                " FOREIGN KEY (" + CommodityDataEntry.COLUMN_MARKET_KEY + ") REFERENCES " +
                MarketEntry.TABLE_NAME + " (" + MarketEntry._ID + "), " +
                // one commodity/market combo per day
                " UNIQUE (" + CommodityDataEntry.COLUMN_ARRIVAL_DATE + ", " +
                CommodityDataEntry.COLUMN_COMMODITY_KEY + ", " +
                CommodityDataEntry.COLUMN_MARKET_KEY + ") ON CONFLICT REPLACE);";
        db.execSQL(SQL_CREATE_STATE_TABLE);
        db.execSQL(SQL_CREATE_DISTRICT_TABLE);
        db.execSQL(SQL_CREATE_MARKET_TABLE);
        db.execSQL(SQL_CREATE_COMMODITY_NAME_TABLE);
        db.execSQL(SQL_CREATE_COMMODITY_DATA_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        db.execSQL("DROP TABLE IF EXISTS " + StateEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DistrictEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MarketEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CommodityNameEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CommodityDataEntry.TABLE_NAME);
        onCreate(db);
    }
}
