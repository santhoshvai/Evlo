package info.santhosh.evlo.data.dbModels;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import info.santhosh.evlo.data.CommodityContract;

import static info.santhosh.evlo.common.Constants.IndianCurrencyFormat;

/**
 * Created by santhoshvai on 10/02/2017.
 */

public class Commodity implements Parcelable {

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
    private long arrival_Date;
    private String max_Price;
    private String modal_Price;
    private String min_Price;
    private boolean isFavorite;

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

    public long getArrival_Date() {
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

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public static Commodity fromCursor(Cursor cursor) {

        return new Commodity(
            cursor.getInt(COL_COMMODITY_DETAIL_ID),
            cursor.getString(COL_STATE_NAME),
            cursor.getString(COL_VARIETY),
            cursor.getString(COL_DISTRICT_NAME),
            cursor.getString(COL_COMMODITY_NAME),
            cursor.getString(COL_MARKET_NAME),
            cursor.getLong(COL_ARRIVAL_DATE),
            IndianCurrencyFormat.format(cursor.getInt(COL_MAX_PRICE)),
            IndianCurrencyFormat.format(cursor.getInt(COL_MODAL_PRICE)),
            IndianCurrencyFormat.format(cursor.getInt(COL_MIN_PRICE)),
            !cursor.isNull(COL_COMMODITY_FAV_ROW_ID)
        );
    }

    private Commodity(int id, String state, String variety, String district, String commodity, String market, long arrival_Date, String max_Price, String modal_Price, String min_Price, boolean isFavorite) {
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
        this.isFavorite = isFavorite;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Commodity commodity1 = (Commodity) o;

        if (id != commodity1.id) return false;
        if (arrival_Date != commodity1.arrival_Date) return false;
        if (isFavorite != commodity1.isFavorite) return false;
        if (!state.equals(commodity1.state)) return false;
        if (!variety.equals(commodity1.variety)) return false;
        if (!district.equals(commodity1.district)) return false;
        if (!commodity.equals(commodity1.commodity)) return false;
        if (!market.equals(commodity1.market)) return false;
        if (!max_Price.equals(commodity1.max_Price)) return false;
        if (!modal_Price.equals(commodity1.modal_Price)) return false;
        return min_Price.equals(commodity1.min_Price);

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + state.hashCode();
        result = 31 * result + variety.hashCode();
        result = 31 * result + district.hashCode();
        result = 31 * result + commodity.hashCode();
        result = 31 * result + market.hashCode();
        result = 31 * result + (int) (arrival_Date ^ (arrival_Date >>> 32));
        result = 31 * result + max_Price.hashCode();
        result = 31 * result + modal_Price.hashCode();
        result = 31 * result + min_Price.hashCode();
        result = 31 * result + (isFavorite ? 1 : 0);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.state);
        dest.writeString(this.variety);
        dest.writeString(this.district);
        dest.writeString(this.commodity);
        dest.writeString(this.market);
        dest.writeLong(this.arrival_Date);
        dest.writeString(this.max_Price);
        dest.writeString(this.modal_Price);
        dest.writeString(this.min_Price);
        dest.writeByte(this.isFavorite ? (byte) 1 : (byte) 0);
    }

    protected Commodity(Parcel in) {
        this.id = in.readInt();
        this.state = in.readString();
        this.variety = in.readString();
        this.district = in.readString();
        this.commodity = in.readString();
        this.market = in.readString();
        this.arrival_Date = in.readLong();
        this.max_Price = in.readString();
        this.modal_Price = in.readString();
        this.min_Price = in.readString();
        this.isFavorite = in.readByte() != 0;
    }

    public static final Parcelable.Creator<Commodity> CREATOR = new Parcelable.Creator<Commodity>() {
        @Override
        public Commodity createFromParcel(Parcel source) {
            return new Commodity(source);
        }

        @Override
        public Commodity[] newArray(int size) {
            return new Commodity[size];
        }
    };
}
