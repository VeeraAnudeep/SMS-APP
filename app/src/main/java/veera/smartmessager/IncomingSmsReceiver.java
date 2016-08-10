package veera.smartmessager;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by veera on 11/8/16.
 */
public class IncomingSmsReceiver extends BroadcastReceiver {
    private String TAG = IncomingSmsReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationCompat.Builder notificationBuilder;
        notificationBuilder = new NotificationCompat.Builder(context);

        final Bundle bundle = intent.getExtras();

        try {

            if (bundle != null) {

                final Object[] pdusObj = (Object[]) bundle.get("pdus");

                for (Object aPdusObj : pdusObj != null ? pdusObj : new Object[0]) {

                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) aPdusObj);
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();

                    String message = currentMessage.getDisplayMessageBody();
                    NotificationCompat.BigTextStyle bigTextStyle = new android.support.v4.app.NotificationCompat.BigTextStyle();

                    //Use this in future if to show multiple messages
                    NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
                    inboxStyle.addLine(message).setSummaryText(message).setBigContentTitle(phoneNumber);

                    bigTextStyle.setSummaryText(message).bigText(message).setBigContentTitle(phoneNumber);

                    Toast.makeText(context, phoneNumber + " :" + message, Toast.LENGTH_SHORT).show();
                    notificationBuilder
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setTicker(message)
                            .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                            .setContentTitle(phoneNumber)
                            .setContentText(message)
                            .setNumber(0)
                            .setDefaults(Notification.DEFAULT_ALL);
                    notificationBuilder.setStyle(bigTextStyle);
                    Intent messageIntent = new Intent();
                    messageIntent.setAction(Intent.ACTION_VIEW);
                    messageIntent.putExtra("address", phoneNumber);
                    messageIntent.setClass(context, MessagesActivity.class);
                    Intent backIntent = new Intent(context, MainActivity.class);
                    backIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent pendingIntent = PendingIntent.getActivities(context, 0,
                            new Intent[]{backIntent, messageIntent}, PendingIntent.FLAG_ONE_SHOT);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
                        notificationBuilder.setPriority(Notification.PRIORITY_MAX);
                        notificationBuilder.setVibrate(new long[0]);
                        notificationBuilder.setAutoCancel(true);
                    }
                    notificationBuilder.setContentIntent(pendingIntent);
                    NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(context);
                    mNotificationManager.notify(1, notificationBuilder.build());
                }
            }
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }
}
