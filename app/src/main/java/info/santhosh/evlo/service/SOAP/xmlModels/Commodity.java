package info.santhosh.evlo.service.SOAP.xmlModels;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Created by santhoshvai on 12/03/16.
 */
@Root(name = "Table")
public class Commodity {
    public Commodity() {}

    @Attribute(name="id")
    private String Id;
    @Attribute(name="rowOrder")
    private String RowOrder;
    @Element(name = "State")
    private String State;
    @Element(name = "Variety")
    private String Variety;
    @Element(name = "District")
    private String District;
    @Element(name = "Commodity")
    private String Commodity;
    @Element(name = "Market")
    private String Market;
    @Element(name = "Arrival_Date")
    private String Arrival_Date;
    @Element(name = "Max_x0020_Price")
    private String Max_Price;
    @Element(name = "Modal_x0020_Price")
    private String Modal_Price;
    @Element(name = "Min_x0020_Price")
    private String Min_Price;

    public String getId() {
        return Id;
    }

    public String getRowOrder() {
        return RowOrder;
    }

    public String getState() {
        return State;
    }

    public String getVariety() {
        return Variety;
    }

    public String getDistrict() {
        return District;
    }

    public String getCommodity() {
        return Commodity;
    }

    public String getMarket() {
        return Market;
    }

    public String getArrival_Date() {
        return Arrival_Date;
    }

    public String getMax_Price() {
        return Max_Price;
    }

    public String getModal_Price() {
        return Modal_Price;
    }

    public String getMin_Price() {
        return Min_Price;
    }

    @Override
    public String toString() {
        return "Commodity{" +
                "Id='" + Id + '\'' +
                ", RowOrder='" + RowOrder + '\'' +
                ", State='" + State + '\'' +
                ", Variety='" + Variety + '\'' +
                ", District='" + District + '\'' +
                ", Commodity='" + Commodity + '\'' +
                ", Market='" + Market + '\'' +
                ", Arrival_Date='" + Arrival_Date + '\'' +
                ", Max_Price='" + Max_Price + '\'' +
                ", Modal_Price='" + Modal_Price + '\'' +
                ", Min_Price='" + Min_Price + '\'' +
                '}';
    }
}
