package com.ben.reminder.ui.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.media.MediaPlayer;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.content.Intent;
import android.util.Log;
import com.ben.reminder.R;

public class PlayAlarmAty extends Activity {
	
	private MediaPlayer mp;
	final String TAG = Thread.currentThread().getStackTrace()[2].getFileName().replace(".java","");
	
	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_player_aty);

		mp = MediaPlayer.create(this, R.raw.mine);
		mp.start();
		Intent it = getIntent();
		String msg = it.getStringExtra("content");
		Log.e(TAG,msg);
        new AlertDialog.Builder(PlayAlarmAty.this).setTitle("闹钟").setMessage(msg)
        .setPositiveButton("关闭闹铃", (dialog, which) -> {
			mp.stop();
			mp.release();
			PlayAlarmAty.this.finish();
		}).show();

	}

}