package info.santhosh.evlo.Services.SOAP;

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