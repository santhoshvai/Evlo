package info.santhosh.evlo.data.util;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.evernote.android.job.Job;

import java.io.IOException;
import java.io.InputStream;

import info.santhosh.evlo.common.DataFetchStatusProvider;
import info.santhosh.evlo.common.EvloPrefs;
import info.santhosh.evlo.common.WriteDb;
import info.santhosh.evlo.model.CommodityProtos;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by santhoshvai on 27/11/17.
 */

public final class ProtoRequestAndStore {
    static final String TAG = "ProtoRequestAndStore";
//    private static final String PROTO_URL = "https://www.dropbox.com/s/y80ip1cj3k0lds2/commodities_test?dl=1";
    private static final String PROTO_URL = "https://s3.ap-south-1.amazonaws.com/evlo-proto/commodities_proto.bin";

    @WorkerThread
    public static Job.Result synchronousProtoRequest(Context context) {
        return synchronousProtoRequest(context, null, null);
    }

    @WorkerThread
    public static Job.Result synchronousProgressiveProtoRequest(Context context, final Progress.ProgressListener progressListener, final WriteDb.WriteDbProgressListener writeDbProgressListener) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addNetworkInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Response originalResponse = chain.proceed(chain.request());
                        return originalResponse.newBuilder()
                                .body(new Progress.ProgressResponseBody(originalResponse.body(), progressListener))
                                .build();
                    }
                })
                .build();
        return synchronousProtoRequest(context, client, writeDbProgressListener);
    }

    @WorkerThread
    private static Job.Result synchronousProtoRequest(Context context, @Nullable OkHttpClient client, @Nullable final WriteDb.WriteDbProgressListener writeDbProgressListener) {
        DataFetchStatusProvider.getInstance(context).setDataFetchStatus(DataFetchStatusProvider.STARTED);

        if (client == null) client = new OkHttpClient();
        Request requestProto = new Request.Builder()
                .url( PROTO_URL )
                .build();
        Response response = null;

        try {
            response = client.newCall(requestProto).execute();
            if (!response.isSuccessful() || response.body() == null) {
                DataFetchStatusProvider.getInstance(context).setDataFetchStatus(DataFetchStatusProvider.ERROR);
                return Job.Result.RESCHEDULE;
            }
            final InputStream byteStream = response.body().byteStream();
            CommodityProtos.Commodities commoditiesProto = CommodityProtos.Commodities.parseFrom(byteStream);
            WriteDb.usingProtos(context, commoditiesProto, writeDbProgressListener);
            Log.d(TAG, "Received proto list size: " + commoditiesProto.getCommodityCount());

            if (commoditiesProto.getCommodityCount() > 0) {
                if (!EvloPrefs.getDataHasLoadedAtleastOnce(context)) {
                    EvloPrefs.setDataHasLoadedAtleastOnce(context, true);
                }
                DataFetchStatusProvider.getInstance(context).setDataFetchStatus(DataFetchStatusProvider.DONE);
            } else {
                DataFetchStatusProvider.getInstance(context).setDataFetchStatus(DataFetchStatusProvider.ERROR);
            }

            return Job.Result.SUCCESS;
        } catch (IOException e) {
            DataFetchStatusProvider.getInstance(context).setDataFetchStatus(DataFetchStatusProvider.ERROR);
            return Job.Result.RESCHEDULE;
        } catch(Exception e) {
            // TODO: log exception to firebase
            e.printStackTrace();
            DataFetchStatusProvider.getInstance(context).setDataFetchStatus(DataFetchStatusProvider.ERROR);
            return Job.Result.FAILURE;
        } finally {
            if (response != null && response.body() != null) {
                response.body().close();
            }
        }
    }

}
