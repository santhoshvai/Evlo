package info.santhosh.evlo.data.dbModels;

import android.database.Cursor;

import info.santhosh.evlo.data.CommodityContract;

import static info.santhosh.evlo.common.Constants.IndianCurrencyFormat;

/**
 * Created by santhoshvai on 10/02/2017.
 */

public class Commodity {

    private static final String TAG = "Commodity-DbModel";

    public static final String[] COMMODITY_DETAIL_COLUMNS = {
            CommodityContract.CommodityDataEntry.TABLE_NAME + "." + CommodityContract.CommodityDataEntry._ID,
            CommodityContract.CommodityDataEntry.COLUMN_ARRIVAL_DATE,
            CommodityContract.CommodityDataEntry.COLUMN_MAX_PRICE,
            CommodityContract.CommodityDataEntry.COLUMN_MIN_PRICE,
            CommodityContract.CommodityDataEntry.COLUMN_MODAL_PRICE,
            CommodityContract.CommodityDataEntry.COLUMN_COMMODITY_NAME,
            CommodityContract.CommodityDataEntry.COLUMN_VARIETY,
            CommodityContract.CommodityDataEntry.COLUMN_STATE_NAME,
            CommodityContract.CommodityDataEntry.COLUMN_DISTRICT_NAME,
            CommodityContract.CommodityDataEntry.COLUMN_MARKET_NAME,
            CommodityContract.CommodityFavEntry.TABLE_NAME + "." + CommodityContract.CommodityFavEntry._ID
    };


    // These indices are tied to COMMODITY_DETAIL_COLUMNS.  If COMMODITY_DETAIL_COLUMNS change, these must change.
    static final int COL_COMMODITY_DETAIL_ID = 0;
    static final int COL_ARRIVAL_DATE = 1;
    static final int COL_MAX_PRICE = 2;
    static final int COL_MIN_PRICE = 3;
    static final int COL_MODAL_PRICE = 4;
    static final int COL_COMMODITY_NAME = 5;
    static final int COL_VARIETY = 6;
    static final int COL_STATE_NAME = 7;
    static final int COL_DISTRICT_NAME = 8;
    static final int COL_MARKET_NAME = 9;
    static final int COL_COMMODITY_FAV_ROW_ID = 10;

    private int id;
    private String state;
    private String variety;
    private String district;
    private String commodity;
    private String market;
    private String arrival_Date;
    private String max_Price;
    private String modal_Price;
    private String min_Price;
    private int fav_row_id;

    public int getId() {
        return id;
    }

    public String getState() {
        return state;
    }

    public String getVariety() {
        return variety;
    }

    public String getDistrict() {
        return district;
    }

    public String getCommodity() {
        return commodity;
    }

    public String getMarket() {
        return market;
    }

    public String getArrival_Date() {
        return arrival_Date;
    }

    public String getMax_Price() {
        return max_Price;
    }

    public String getModal_Price() {
        return modal_Price;
    }

    public String getMin_Price() {
        return min_Price;
    }

    public int getFav_row_id() {
        return fav_row_id;
    }

    public static Commodity fromCursor(Cursor cursor) {
        Double maxPriceDouble = Double.valueOf(cursor.getString(COL_MAX_PRICE));
        Double modalPriceDouble = Double.valueOf(cursor.getString(COL_MODAL_PRICE));
        Double minPriceDouble = Double.valueOf(cursor.getString(COL_MIN_PRICE));
        int fav_row_id = -1;
        if (!cursor.isNull(COL_COMMODITY_FAV_ROW_ID)) {
            fav_row_id = cursor.getInt(COL_COMMODITY_FAV_ROW_ID);
        }
        return new Commodity(
                cursor.getInt(COL_COMMODITY_DETAIL_ID),
                cursor.getString(COL_STATE_NAME),
                cursor.getString(COL_VARIETY),
                cursor.getString(COL_DISTRICT_NAME),
                cursor.getString(COL_COMMODITY_NAME),
                cursor.getString(COL_MARKET_NAME),
                cursor.getString(COL_ARRIVAL_DATE),
                IndianCurrencyFormat.format(maxPriceDouble),
                IndianCurrencyFormat.format(modalPriceDouble),
                IndianCurrencyFormat.format(minPriceDouble),
                fav_row_id);
    }

    private Commodity(int id, String state, String variety, String district, String commodity, String market, String arrival_Date, String max_Price, String modal_Price, String min_Price, int fav_row_id) {
        this.id = id;
        this.state = state;
        this.variety = variety;
        this.district = district;
        this.commodity = commodity;
        this.market = market;
        this.arrival_Date = arrival_Date;
        this.max_Price = max_Price;
        this.modal_Price = modal_Price;
        this.min_Price = min_Price;
        this.fav_row_id = fav_row_id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Commodity commodity1 = (Commodity) o;

        if (!state.equals(commodity1.state)) return false;
        if (!variety.equals(commodity1.variety)) return false;
        if (!district.equals(commodity1.district)) return false;
        if (!commodity.equals(commodity1.commodity)) return false;
        return market.equals(commodity1.market);

    }

    @Override
    public int hashCode() {
        int result = state.hashCode();
        result = 31 * result + variety.hashCode();
        result = 31 * result + district.hashCode();
        result = 31 * result + commodity.hashCode();
        result = 31 * result + market.hashCode();
        return result;
    }
}