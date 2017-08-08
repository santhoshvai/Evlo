package info.santhosh.evlo.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.evernote.android.job.Job;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.InputStream;

import info.santhosh.evlo.common.WriteDb;
import info.santhosh.evlo.model.CommodityProtos;

/**
 * Created by santhoshvai on 29/07/17.
 */

public class GetProtoDataService extends IntentService {

    private static final String TAG = "GetProtoDataService";
    private static final String PROTO_URL = "https://www.dropbox.com/s/y80ip1cj3k0lds2/commodities_test?dl=1";

    public GetProtoDataService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        final Job.Result result = synchronousProtoRequest(this);
        if (result.equals(Job.Result.RESCHEDULE)) {
            // TODO: reschedule ASAP
        }
    }

    @WorkerThread
    private static Job.Result synchronousProtoRequest(Context context) {
        OkHttpClient client = new OkHttpClient();
        Request requestProto = new Request.Builder()
                .url( PROTO_URL )
                .build();
        Response response;

        try {
            long startTime = System.currentTimeMillis();
            response = client.newCall(requestProto).execute();
            if (!response.isSuccessful()) {
                return Job.Result.RESCHEDULE;
            }
            final InputStream byteStream = response.body().byteStream();
            long stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - startTime;
            Log.d(TAG, "elapsedTime to get proto from network (ms): " + elapsedTime );

            startTime = System.currentTimeMillis();
            CommodityProtos.Commodities commoditiesProto = CommodityProtos.Commodities.parseFrom(byteStream);
            stopTime = System.currentTimeMillis();
            elapsedTime = stopTime - startTime;
            Log.d(TAG, "elapsedTime to parse proto data (ms): " + elapsedTime);

            startTime = System.currentTimeMillis();
            WriteDb.usingProtos(context, commoditiesProto);
            stopTime = System.currentTimeMillis();
            elapsedTime = stopTime - startTime;
            Log.d(TAG, "elapsedTime to write data (ms): " + elapsedTime);
            Log.d(TAG, "size: " + commoditiesProto.getCommodityCount());

            response.body().close();
            return Job.Result.SUCCESS;
        } catch(IOException e) {
            // TODO: log exception to firebase
            e.printStackTrace();
            Log.e(TAG, e.getClass().getSimpleName()+ ": "+ e.getLocalizedMessage());
            return Job.Result.RESCHEDULE;
        } catch(Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getClass().getSimpleName()+ ": "+ e.getLocalizedMessage());
            return Job.Result.FAILURE;
        }
    }


}
