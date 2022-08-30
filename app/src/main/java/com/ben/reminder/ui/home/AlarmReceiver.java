package com.ben.reminder.ui.home;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.ben.reminder.MainActivity;
import com.ben.reminder.R;


public class AlarmReceiver extends BroadcastReceiver {
	//final String TAG = "AlarmReceiver";
	final String TAG = Thread.currentThread().getStackTrace()[2].getFileName().replace(".java","");

	@Override
	public void onReceive(Context context, Intent intent) {
		String content = intent.getStringExtra("content");
		/*
		Intent i = new Intent(context, PlayAlarmAty.class);
		Log.e(TAG, content);
		i.putExtra("content", content);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(i);
*/
	//	PendingIntent pi = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent pi = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context,"100")
		        .setSmallIcon(R.drawable.ic_notifications_black_24dp)
		        .setContentTitle("闹铃")
		        .setContentText(content)
		        .setPriority(NotificationCompat.PRIORITY_HIGH)
		        .setCategory(NotificationCompat.CATEGORY_ALARM)
				.setAutoCancel(true)
		        .setFullScreenIntent(pi, true);
	    NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		if(Build.VERSION.SDK_INT>Build.VERSION_CODES.O) {
			NotificationChannel nc = new NotificationChannel("100","msg",NotificationManager.IMPORTANCE_HIGH);
			nm.createNotificationChannel(nc);
		}
	    Notification n = mBuilder.build();
		nm.notify(100, n);

	}
}