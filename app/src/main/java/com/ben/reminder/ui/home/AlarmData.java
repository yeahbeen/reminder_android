package com.ben.reminder.ui.home;

import androidx.annotation.NonNull;

public class AlarmData {

    private String time = "";
    private String content = "";
    private int requestcode;
    private Boolean enable;

    public AlarmData(String time,String content,int requestcode,Boolean enable) {
        this.time = time;
        this.content = content;
        this.requestcode = requestcode;
        this.enable = enable;
    }

    public void setEnable(Boolean enable){
        this.enable = enable;
    }
    //用来保存到文件等
    public String getData() {
        return time+"|"+requestcode+"|"+content+"|"+enable;
    }

    public String getTime(){
        return time;
    }

    public String getContent(){
        return content;
    }

    //显示在列表的内容
    @NonNull
    @Override
    public String toString() {
        return time+" "+content;
    }

}