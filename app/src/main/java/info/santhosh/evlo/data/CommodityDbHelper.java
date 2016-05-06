package info.santhosh.evlo.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import info.santhosh.evlo.data.CommodityContract.CommodityDataEntry;

/**
 * Manages a local database for commodity data
 * Created by santhoshvai on 13/03/16.
 */
public class CommodityDbHelper  extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 4;

    static final String DATABASE_NAME = "commodities.db";

    public CommodityDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {


        // Create a table to hold commmodity info.
        final String SQL_CREATE_COMMODITY_DATA_TABLE = "CREATE TABLE " + CommodityDataEntry.TABLE_NAME + " ( " +
                CommodityDataEntry._ID + " INTEGER PRIMARY KEY," +
                CommodityDataEntry.COLUMN_COMMODITY_NAME + " TEXT NOT NULL, " +
                CommodityDataEntry.COLUMN_VARIETY + " INTEGER NOT NULL, " +
                CommodityDataEntry.COLUMN_ARRIVAL_DATE + " TEXT NOT NULL, " +
                CommodityDataEntry.COLUMN_MAX_PRICE + " INTEGER NOT NULL, " +
                CommodityDataEntry.COLUMN_MODAL_PRICE + " INTEGER NOT NULL, " +
                CommodityDataEntry.COLUMN_MIN_PRICE + " INTEGER NOT NULL," +
                CommodityDataEntry.COLUMN_MARKET_NAME + " TEXT NOT NULL, " +
                CommodityDataEntry.COLUMN_DISTRICT_NAME + " TEXT NOT NULL, " +
                CommodityDataEntry.COLUMN_STATE_NAME + " TEXT NOT NULL, " +
                // one commodity/market combo
                " UNIQUE (" +
                CommodityDataEntry.COLUMN_COMMODITY_NAME + ", " +
                CommodityDataEntry.COLUMN_VARIETY + ", " +
                CommodityDataEntry.COLUMN_MARKET_NAME + ") ON CONFLICT REPLACE);";
        db.execSQL(SQL_CREATE_COMMODITY_DATA_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL("DROP TABLE IF EXISTS " + CommodityDataEntry.TABLE_NAME);
        onCreate(db);
    }
}
