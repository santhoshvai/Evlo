package info.santhosh.evlo.service.SOAP;

import android.util.Log;

import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.Convert;
import org.simpleframework.xml.convert.Converter;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

/**
 * Created by santhoshvai on 12/03/16.
 */

@Root(name = "Envelope")
@Namespace(prefix = "soap")
// Set the converter that's used for serialization
@Convert(value = SOAPEnvelope.SOAPEnvelopeConverter.class)
public class SOAPEnvelope {

    private static String TAG = "SOAPEnvelope";

    // Keep the content of vehicles list here
    private Commodities commodities;


    public Commodities getCommodities() {
        Log.d(TAG, "getCommodities");
        return commodities;
    }

    protected void setCommodities(Commodities commodities) {
        this.commodities = commodities;
    }

    // The converter implementation for SOAPEnvelope
    public static class SOAPEnvelopeConverter implements Converter<SOAPEnvelope> {
        @Override
        public SOAPEnvelope read(InputNode node) throws Exception {
            SOAPEnvelope envelope = new SOAPEnvelope();
            InputNode commoditiesNode = findCommoditiesNode(node); // Search the Vehicles list element

            if( commoditiesNode == null ) {
                // This is bad - do something useful here
                throw new Exception("No commodities node!");
            }
            /*
             * A default serializer is used to deserialize the full node. The
             * returned object is set into the envelops's object, where you can
             * get it through a get()-method.
             */
            Serializer ser = new Persister();
            envelope.setCommodities(ser.read(Commodities.class, commoditiesNode));

            return envelope;
        }


        @Override
        public void write(OutputNode node, SOAPEnvelope value) throws Exception {
            // If you read (deserialize) only there's no need to implement this
            throw new UnsupportedOperationException("Not supported yet.");
        }


        private InputNode findCommoditiesNode(InputNode rootNode) throws Exception {
            InputNode next;

            while( ( next = rootNode.getNext() ) != null ) {
                if( next.getName().equals("NewDataSet") == true ) {
                    Log.d("TAG", "Found the tag that contains all commodities");
                    return next;
                }
            }

            return null;
        }
    }
}
