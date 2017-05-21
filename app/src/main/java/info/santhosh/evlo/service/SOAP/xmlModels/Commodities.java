package info.santhosh.evlo.service.SOAP.xmlModels;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by santhoshvai on 12/03/16.
 */
@Root(name = "NewDataSet")
public class Commodities {

    @ElementList(name = "NewDataSet", inline = true)
    private List<Commodity> commodities;

    public List<Commodity> getList() {
        return commodities;
    }

//    public Commodities() { }

    @Override
    public String toString() {
        return "Commodities{" +
                "commodities=" + commodities +
                '}';
    }
}
