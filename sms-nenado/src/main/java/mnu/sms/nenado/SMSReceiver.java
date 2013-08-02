package mnu.sms.nenado;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsMessage;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SMSReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(Constants.TAG, "SMS onReceive");

        Bundle bundle = intent.getExtras();
        SmsMessage[] messages = null;

        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            messages = new SmsMessage[pdus.length];
            for (int i = 0; i < messages.length; i++) {
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                String phoneNumberSendSMS = messages[i].getOriginatingAddress();

//                if (phoneNumberSendSMS.startsWith("+"))
//                    continue;

                String msgText = messages[i].getMessageBody();

                DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                String date = dateFormat.format(new Date(messages[i].getTimestampMillis()));

                Log.v(Constants.TAG, "onReceive, sms text = " + msgText);


                Intent intent1 = new Intent(context, SMSNenadoActivity.class);

                intent1.putExtra(Constants.EXTRA_TEXT, msgText);
                intent1.putExtra(Constants.EXTRA_ADRESS, phoneNumberSendSMS);
                intent1.putExtra(Constants.EXTRA_DATE, date);
                PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                        intent1, 0);

                showNotification(context, contentIntent);
            }
        }

    }

    private void showNotification(Context context, PendingIntent contentIntent) {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("My notification")
                        .setContentText("Hello World!");
        mBuilder.setContentIntent(contentIntent);
//        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());

    }

}