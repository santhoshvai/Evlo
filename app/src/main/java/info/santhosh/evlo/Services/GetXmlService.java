package info.santhosh.evlo.Services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;

import info.santhosh.evlo.R;
import info.santhosh.evlo.Services.SOAP.SOAPEnvelope;
import info.santhosh.evlo.Services.SOAP.WriteDb;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by santhoshvai on 12/03/16.
 */
public class GetXmlService  extends IntentService {

    final String TAG = "IntentService";

    // Must create a default constructor
    public GetXmlService() {
        // Used to name the worker thread, important only for debugging.
        super("xml-service");
    }

    @Override
    public void onCreate() {
        super.onCreate(); // if you override onCreate(), make sure to call super().
        // If a Context object is needed, call getApplicationContext() here.
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // This describes what will happen when service is triggered
        // should be a singleton
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url( getApplicationContext().getResources().getString(R.string.price_xml_url) )
                .build();

        try{
            long startTime = System.currentTimeMillis();
            Response response = client.newCall(request).execute(); // synchronous
            final String responseXml = response.body().string(); // get the xml string
            Serializer serializer = new Persister(new AnnotationStrategy());
            SOAPEnvelope envelope = serializer.read(SOAPEnvelope.class, responseXml);
            Log.d(TAG, envelope.getCommodities().getCommodities().get(0).toString());
            long stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - startTime;
            Log.d(TAG, "elapsedTime to read data: " + Long.toString(elapsedTime) );
            // INSERT VALUES TO TABLE HERE
            // STATE -> DISTRICT -> MARKET -> get Id
            // commodity_name -> get Id
            // use both id to build commodityData table
            // see https://github.com/udacity/Sunshine-Version-2/blob/5.19_accessibility/app/src/main/java/com/example/android/sunshine/app/FetchWeatherTask.java
            startTime = System.currentTimeMillis();

            WriteDb writeDb = new WriteDb(getApplicationContext());
            writeDb.usingCommoditiesList(envelope.getCommodities().getCommodities());

            stopTime = System.currentTimeMillis();
            elapsedTime = stopTime - startTime;
            Log.d(TAG, "elapsedTime to write data: " + Long.toString(elapsedTime) );
        } catch(Exception e) {
            Log.e(TAG, "Exception: "+ e.getMessage());
        }
    }
}
