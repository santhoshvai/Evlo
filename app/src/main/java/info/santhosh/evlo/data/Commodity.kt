package info.santhosh.evlo.data

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "commodity_data")
data class Commodity (
        @ColumnInfo(name = "_id") @PrimaryKey(autoGenerate = true)
        val id: Long,
        @ColumnInfo(name = "state_name")
        val state: String,
        @ColumnInfo(name = "variety")
        val variety: String,
        @ColumnInfo(name = "district_name")
        val district: String,
        @ColumnInfo(name = "commodity_name")
        val name: String,
        @ColumnInfo(name = "market_name")
        val market: String,
        @ColumnInfo(name = "arrival_date")
        val arrivalDate: Long,
        @ColumnInfo(name = "max_price")
        val maxPrice: String,
        @ColumnInfo(name = "modal_price")
        val modalPrice: String,
        @ColumnInfo(name = "min_price")
        val minPrice: String
)