package info.santhosh.evlo.common;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import info.santhosh.evlo.R;
import info.santhosh.evlo.ui.main.MainActivity;

/**
 * Created by santhoshvai on 05/02/2018.
 */

public class NotificationUtil {
    private static final Object mLock = new Object();
    public static final int PRICE_UPDATES_NOTIFICATION_ID = 777;
    private static NotificationUtil mInstance;

    private Context applnContext;
    private static final String PRICE_UPDATES_CHANNEL_ID = "PRICE_UPDATES_CHANNEL_ID";

    public static NotificationUtil getInstance(Context context) {
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new NotificationUtil(context);
            }
            return mInstance;
        }
    }

    private NotificationUtil(Context context) {
        applnContext = context.getApplicationContext();
        initChannels();
    }

    private void initChannels() {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationManager notificationManager =
                (NotificationManager) applnContext.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(PRICE_UPDATES_CHANNEL_ID,
                applnContext.getString(R.string.price_updates_channel_title),
                NotificationManager.IMPORTANCE_DEFAULT);
        notificationManager.createNotificationChannel(channel);
    }

    public void notifyPriceUpdate(int count) {
        Intent intent = new Intent(applnContext, MainActivity.class);
        // TODO, on open, highlight which commodities have been updated
        PendingIntent contentIntent = PendingIntent.getActivity(applnContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder notificationBuilder
                = new NotificationCompat.Builder(applnContext, PRICE_UPDATES_CHANNEL_ID);
        Resources res = applnContext.getResources();
        String title = res.getQuantityString(R.plurals.update_prices_notif_title, count);
        String body = res.getQuantityString(R.plurals.update_prices_notif_description, count, count);
        notificationBuilder
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.drawable.ic_evlo_notif)
                .setColor(ContextCompat.getColor(applnContext, R.color.teal_500))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(body)
                )
                .setContentIntent(contentIntent)
                .setAutoCancel(true);
        NotificationManager notificationManager =
                (NotificationManager) applnContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(PRICE_UPDATES_NOTIFICATION_ID, notificationBuilder.build());
    }

    public static void notifyBookmarkPriceUpdatesIfNeeded(Context context, int updatesCount) {
        final Context applnContext = context.getApplicationContext();
        final int total = EvloPrefs.updateUnnotifiedBookmarkUodates(applnContext, updatesCount);
        if (total < 1) return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (Utils.isAppInForeground(applnContext)) return;
                if (!Utils.isNowBetweenNineToNine()) return;
                EvloPrefs.resetUnnotifiedBookmarkUpdates(applnContext);
                NotificationUtil.getInstance(applnContext).notifyPriceUpdate(total);
            }
        }).start();
    }
}
