package info.santhosh.evlo.common;

import java.text.DecimalFormat;

/**
 * Created by santhoshvai on 10/02/2017.
 */

public final class Constants {
    public static final DecimalFormat IndianCurrencyFormat = new DecimalFormat("##,##,###");
    public static final String INTENT_COMMON = "info.santhosh.evlo.intent.";
    public static final String INTENT_DATA_FETCH_START = INTENT_COMMON + "DATA_FETCH_START";
    public static final String INTENT_DATA_FETCH_DONE = INTENT_COMMON + "DATA_FETCH_DONE";
    public static final String INTENT_DATA_FETCH_ERROR = INTENT_COMMON + "DATA_FETCH_ERROR";
    public static final long SEVEN_DAYS_IN_MILLIS = 1000 * 7 * 60 * 60 * 24;

    private Constants() {
    }
}
