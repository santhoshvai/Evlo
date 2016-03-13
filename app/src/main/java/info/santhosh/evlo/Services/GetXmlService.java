package info.santhosh.evlo.Services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.convert.AnnotationStrategy;
import org.simpleframework.xml.core.Persister;

import info.santhosh.evlo.R;
import info.santhosh.evlo.Services.SOAP.SOAPEnvelope;
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
            Response response = client.newCall(request).execute(); // synchronous
            final String responseXml = response.body().string();
            //Log.d(TAG, responseXml);
            Serializer serializer = new Persister(new AnnotationStrategy());
            SOAPEnvelope envelope = serializer.read(SOAPEnvelope.class, responseXml);
            Log.d(TAG, envelope.getCommodities().getCommodities().get(0).toString());
        } catch(Exception e) {
            Log.e(TAG, "Exception: "+ e.getMessage());
        }
    }
}
