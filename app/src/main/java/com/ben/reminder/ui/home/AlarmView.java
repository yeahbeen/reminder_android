package com.ben.reminder.ui.home;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.app.AlertDialog;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TimePicker;
import android.util.Log;

import androidx.annotation.Nullable;

import com.ben.reminder.databinding.FragmentHomeBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ben.reminder.databinding.TimePickerDlgBinding;

import java.util.Calendar;


//闹钟
public class AlarmView extends LinearLayout {

	private static final String KEY_ALARM_LIST = "alarmList";
	private Button btnAddAlarm;
	private ListView lvAlarmList;
	private Adapter adapter;
	private AlarmManager alarmManager;
	private FloatingActionButton fab;
	private TimePickerDlgBinding time_binding;
	private AlertDialog dialog;
	private TimePicker timepicker;
	private EditText editText;
	final String TAG = Thread.currentThread().getStackTrace()[2].getFileName().replace(".java","");

	public AlarmView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);

	}

	public AlarmView(Context context) {
		super(context);

	}


	public void init(FragmentHomeBinding binding,TimePickerDlgBinding time_bind) {

		lvAlarmList = binding.lvAlarmList;
		fab = binding.fab;
		time_binding = time_bind;
		alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
		adapter = new Adapter(getContext(),this);
		lvAlarmList.setAdapter(adapter);
		readSavedAlarmList();

		View timeview = time_binding.getRoot();
		editText = time_binding.edittext;
		timepicker = time_binding.timepicker;
		timepicker.setIs24HourView(true);
		dialog = new AlertDialog.Builder(getContext())
				.setTitle("添加闹钟")//设置对话框的标题
				.setView(timeview)
				.setNegativeButton("取消", (dialog1, which) -> dialog1.dismiss())
				.setPositiveButton("确定", (dialog12, which) -> {
					dialog12.dismiss();
					addAlarm();
				}).create();

		fab.setOnClickListener(view -> {
			editText.setText("");
			timepicker.setHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
			timepicker.setMinute(Calendar.getInstance().get(Calendar.MINUTE));
			dialog.show();
		});

		lvAlarmList.setOnItemLongClickListener((adapterView, view, position, id) -> {
			new AlertDialog.Builder(getContext()).setTitle("操作选项")
					.setItems(new CharSequence[] { "删除" }, (dialogInterface, which) -> {
						switch (which) {
						case 0:
							deleteAlarm(position);
							break;
						default:
							break;
						}
					}).setNegativeButton("取消", null).show();
			return true;
		});
		
	}
	

	private void cancelAlarm(int position){
		Log.e(TAG,"position:"+position);
		AlarmData ad = (AlarmData) adapter.getItem(position);
		Log.e(TAG,ad.getData());
		String[] str = ad.getData().split("\\|");

		int requestcode = Integer.parseInt(str[1]);
		Intent intent = new Intent(getContext(), AlarmReceiver.class);
		intent.putExtra("content",str[2]);
		PendingIntent pi = PendingIntent.getBroadcast(getContext(), requestcode, intent, PendingIntent.FLAG_IMMUTABLE);
		alarmManager.cancel(pi);
	}

	private void deleteAlarm(int position) {
		cancelAlarm(position);
		adapter.remove(position);
		saveAlarmList();//重新保存

	}

	private  void setAlarm(String time,int requestcode,String content){
		Intent intent = new Intent(getContext(), AlarmReceiver.class);
		intent.putExtra("content",content);
		PendingIntent pi = PendingIntent.getBroadcast(getContext(), requestcode, intent, PendingIntent.FLAG_IMMUTABLE);

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.split(":")[0]));
		calendar.set(Calendar.MINUTE, Integer.parseInt(time.split(":")[1]));
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		// 设置时间小于当前时间，往后推一天
		if (calendar.getTimeInMillis() <= Calendar.getInstance().getTimeInMillis()) {
			calendar.setTimeInMillis(calendar.getTimeInMillis() + 24 * 60 * 60 * 1000);
		}
		Log.e(TAG,calendar.getTime().toString());
		alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
	}

	private  void real_addAlarm(String time,int requestcode,String content,Boolean enable){
		AlarmData ad = new AlarmData(time,content,requestcode,enable);
		Log.e(TAG,ad.getData());
		adapter.add(ad,enable);
		if(enable) {
			setAlarm(time, requestcode, content);
		}

	}

	private void addAlarm() {
		String content = editText.getText().toString();
		Log.e(TAG,content);
		if(content.equals("")){content = " ";}//加个空格，不然后面会出错
		int hourOfDay = timepicker.getHour();
		int minute = timepicker.getMinute();
		String str_hour = String.valueOf(hourOfDay);
		String str_min = String.valueOf(minute);
		if(str_hour.length()==1){   //前面补零
			str_hour = "0"+str_hour;
		}
		if(str_min.length()==1){
			str_min = "0"+str_min;
		}
		String time = str_hour+":"+str_min;
		Log.e(TAG,time);
		int requestcode = (int)(Calendar.getInstance().getTimeInMillis()/1000); //requestcode要不一样，否则只生效一个
		Log.e(TAG,""+requestcode);
		real_addAlarm(time,requestcode,content,true);
		saveAlarmList();
	}

	private void saveAlarmList() {
		SharedPreferences.Editor editor = getContext()
				.getSharedPreferences(AlarmView.class.getName(), Context.MODE_PRIVATE).edit();

		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < adapter.getCount(); i++) {
			sb.append(adapter.getItem(i).getData()).append(",");
		}

		if (sb.length() > 1) {
			String content = sb.toString().substring(0, sb.length() - 1);//去掉最后一个逗号
			editor.putString(KEY_ALARM_LIST, content);
		} else {
			editor.putString(KEY_ALARM_LIST, null);
		}
		editor.commit();
	}

	//清空配置，调试用
	private void clearPref(){
		SharedPreferences.Editor editor = getContext().getSharedPreferences(AlarmView.class.getName(), Context.MODE_PRIVATE).edit();
		editor.clear();
		editor.commit();
	}


	// 读取已存数据
	private void readSavedAlarmList() {

		//clearPref();

		SharedPreferences sp = getContext().getSharedPreferences((AlarmView.class.getName()), Context.MODE_PRIVATE);
		String content = sp.getString(KEY_ALARM_LIST, null);
		Log.e(TAG,String.valueOf(content));
		if (content != null) {
			String[] timeStrings = content.split(",");
			for (String string : timeStrings) {
				String str[] = string.split("\\|");
				real_addAlarm(str[0],Integer.parseInt(str[1]),str[2],Boolean.valueOf(str[3]));
			}
		}

	}

	public void enableAlarm(int position,Boolean enable){
		Log.e(TAG,"enable:"+enable);
		if(enable){
			String[] str = ((AlarmData)adapter.getItem(position)).getData().split("\\|");
			setAlarm(str[0],Integer.parseInt(str[1]),str[2]);
		}else{
			cancelAlarm(position);
		}
		((AlarmData)adapter.getItem(position)).setEnable(enable);
		saveAlarmList();
	}

}
