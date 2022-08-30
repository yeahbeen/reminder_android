package com.ben.reminder.ui.dashboard;

import android.util.Log;


import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DashboardViewModel extends ViewModel {

    private final MutableLiveData<String> ip;
    private final MutableLiveData<String> endpoint;
    private final MutableLiveData<String> info;
    ArrayList<MutableLiveData<String>> infoArr = new ArrayList<>();
    private int index = 0;
    final String TAG = Thread.currentThread().getStackTrace()[2].getFileName().replace(".java","");
    public String saveInfo = ""; //保存信息，初始化时使用

    public DashboardViewModel() {
        Log.e(TAG,"in DashboardViewModel()");
        ip = new MutableLiveData<>();
        ip.setValue("0.0.0.0");
        endpoint = new MutableLiveData<>();
        endpoint.setValue("0.0.0.0");
        info = new MutableLiveData<>();
        for(int i=0;i<30;i++){
            infoArr.add(new MutableLiveData<>());
        }

    }


    public MutableLiveData<String> getIp() {
        return ip;
    }

    public MutableLiveData<String> getEndpoint() {
        return endpoint;
    }

    public MutableLiveData<String> getInfo() {
        return info;
    }
    public void setInfo(String value) {
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat= new SimpleDateFormat("HH:mm:ss", Locale.CHINESE);
        String msg = dateFormat.format(date)+"-"+value+"\n";

        infoArr.get(index++).postValue(msg);
        saveInfo += msg;
        if(index>=10){index = 0;}
    }

    public ArrayList<MutableLiveData<String>> getInfoArr(){return infoArr;}




}