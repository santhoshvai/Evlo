package info.santhosh.evlo.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import info.santhosh.evlo.data.CommodityContract.CommodityDataEntry;

/**
 * Manages a local database for commodity data
 * Created by santhoshvai on 13/03/16.
 */
class CommodityDbHelper  extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 5;

    private static final String DATABASE_NAME = "commodities.db";

    CommodityDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {


        // Create a table to hold commmodity info.
        final String SQL_CREATE_COMMODITY_DATA_TABLE = "CREATE TABLE " + CommodityDataEntry.TABLE_NAME + " ( " +
                CommodityDataEntry._ID + " INTEGER PRIMARY KEY," +
                CommodityDataEntry.COLUMN_COMMODITY_NAME + " TEXT NOT NULL, " +
                CommodityDataEntry.COLUMN_VARIETY + " TEXT NOT NULL, " +
                CommodityDataEntry.COLUMN_ARRIVAL_DATE + " INTEGER NOT NULL, " +
                CommodityDataEntry.COLUMN_MAX_PRICE + " INTEGER NOT NULL, " +
                CommodityDataEntry.COLUMN_MODAL_PRICE + " INTEGER NOT NULL, " +
                CommodityDataEntry.COLUMN_MIN_PRICE + " INTEGER NOT NULL," +
                CommodityDataEntry.COLUMN_MARKET_NAME + " TEXT NOT NULL, " +
                CommodityDataEntry.COLUMN_DISTRICT_NAME + " TEXT NOT NULL, " +
                CommodityDataEntry.COLUMN_STATE_NAME + " TEXT NOT NULL); ";

        //create a table to hold commodity favs info -- just the id
        final String SQL_CREATE_COMMODITY_FAV_TABLE = "CREATE TABLE "
                + CommodityContract.CommodityFavEntry.TABLE_NAME + " ( "
                + CommodityContract.CommodityFavEntry._ID + " INTEGER PRIMARY KEY, "
                + CommodityContract.CommodityFavEntry.COLUMN_FAV_ID + " INTEGER NOT NULL UNIQUE, "
                + "FOREIGN KEY("  + CommodityContract.CommodityFavEntry.COLUMN_FAV_ID + ") "
                + "REFERENCES " + CommodityDataEntry.TABLE_NAME + "(" + CommodityDataEntry._ID + ")"
                + ");";

        db.execSQL(SQL_CREATE_COMMODITY_DATA_TABLE);
        db.execSQL(SQL_CREATE_COMMODITY_FAV_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL("DROP TABLE IF EXISTS " + CommodityDataEntry.TABLE_NAME);
        onCreate(db);
    }

//    @Override
//    public void onConfigure(SQLiteDatabase db) {
//        super.onConfigure(db);
//        db.enableWriteAheadLogging();
//    }
}
