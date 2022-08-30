package com.ben.reminder.ui.home;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Switch;
import android.widget.TextView;
 
import com.ben.reminder.R;

import java.util.ArrayList;
import java.util.List;

public class Adapter extends BaseAdapter {

    final String TAG = Thread.currentThread().getStackTrace()[2].getFileName().replace(".java","");
    private List<String> list_title;
    private List<AlarmData> list_T;
    private List<Boolean> list_switch;

    private Context mContext;
    private AlarmView alarmView;

    public Adapter(Context context,AlarmView alarmView) {
        mContext=context;
        this.alarmView = alarmView;
        list_T = new ArrayList<>();
        list_switch=new ArrayList<>();
    }

    public void addItem(String title){
        list_title.add(title);
        list_switch.add(true);
        notifyDataSetChanged();
    }
    public void add(AlarmData ad,Boolean enable){
        list_T.add(ad);
        list_switch.add(enable);
        notifyDataSetChanged();
    }
    public void remove(int position) {
        list_T.remove(position);
        list_switch.remove(position);
        notifyDataSetChanged();
    }
    public void setSwitch(int position,Boolean checked){
        list_switch.set(position,checked);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list_T.size();
    }

    @Override
    public AlarmData getItem(int i) {
        return list_T.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ViewHolder holder = null;
        if(view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.item, null);
            holder = new ViewHolder();
            holder.time = (TextView) view.findViewById(R.id.time);
            holder.content = (TextView) view.findViewById(R.id.content);
            holder.aSwitch = (Switch) view.findViewById(R.id.aSwitch);
            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();

        }
        holder.time.setText(list_T.get(i).getTime());
        holder.content.setText(list_T.get(i).getContent());
        holder.aSwitch.setChecked(list_switch.get(i));
        ViewHolder finalHolder = holder;
        holder.aSwitch.setOnClickListener(view1 -> {
            list_switch.set(i,finalHolder.aSwitch.isChecked());
            alarmView.enableAlarm(i,finalHolder.aSwitch.isChecked());
        });
        return view;
        }

        private class ViewHolder{
            TextView time;
            TextView content;
            Switch aSwitch;
        }
}
