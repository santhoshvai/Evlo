package info.santhosh.evlo.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;

import info.santhosh.evlo.R;
import info.santhosh.evlo.common.WriteDb;
import info.santhosh.evlo.service.SOAP.parser.SOAPEnvelope;

/**
 * Created by santhoshvai on 12/03/16.
 */
public class GetXmlService  extends IntentService {

    static final String TAG = "GetXmlService";

    public GetXmlService() {
        super("xml-service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url( getApplicationContext().getResources().getString(R.string.price_xml_url) )
                .build();

        try{
            long startTime = System.currentTimeMillis();
            Response response = client.newCall(request).execute(); // synchronous
            final String responseXml = response.body().string(); // get the xml string
            long stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - startTime;
            Log.d(TAG, "elapsedTime to get xml from network: " + Long.toString(elapsedTime) );

            startTime = System.currentTimeMillis();
            Serializer serializer = new Persister(new AnnotationStrategy());
            SOAPEnvelope envelope = serializer.read(SOAPEnvelope.class, responseXml);
//            Log.d(TAG, envelope.getList().getList().get(0).toString());
            stopTime = System.currentTimeMillis();
            elapsedTime = stopTime - startTime;
            Log.d(TAG, "elapsedTime to parse data: " + Long.toString(elapsedTime) );
            startTime = System.currentTimeMillis();

            WriteDb writeDb = new WriteDb(getApplicationContext());
            writeDb.usingCommoditiesList(envelope.getCommodities().getList());

            stopTime = System.currentTimeMillis();
            elapsedTime = stopTime - startTime;
            Log.d(TAG, "elapsedTime to write data: " + Long.toString(elapsedTime) );
        } catch(Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getClass().getSimpleName()+ ": "+ e.getLocalizedMessage());
        }
    }
}
