package com.ben.reminder;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.ben.reminder.ui.dashboard.DashboardViewModel;

public class RecvThread extends Thread {
    final private Socket sock;
    final private Context ct;
   // final String TAG = "RecvThread";
    final String TAG = Thread.currentThread().getStackTrace()[2].getFileName().replace(".java","");
    DashboardViewModel dashboardViewModel;
    
    public RecvThread(Context context,Socket s) throws IOException{
        sock = s;
        ct = context;
        dashboardViewModel = new ViewModelProvider((ViewModelStoreOwner) context).get(DashboardViewModel.class);
    }
    //@RequiresApi(api = Build.VERSION_CODES.Q)
    public void run(){
        OutputStream f = null;
        try{
            DataInputStream in = new DataInputStream(sock.getInputStream());
            DataOutputStream out = new DataOutputStream(sock.getOutputStream());
          //  String recvStr = in.readUTF();
            byte[] rs = new byte[1024];
            int rc = in.read(rs);
            Log.e(TAG,"rc:"+rc);    
            String recvStr = new String(Arrays.copyOfRange(rs, 0, rc));
            Log.e(TAG,recvStr);
            dashboardViewModel.setInfo("接收到来自"+sock.getRemoteSocketAddress()+"发文件请求:"+recvStr);
           // while(!recvStr.equals("done")){
                // if(server.isClosed()){return;}
                if(recvStr.contains("sendfile")){
//                	out.writeUTF("receivefile");
                	out.write("receivefile".getBytes());

                    /*
                    //File file = new File(ct.getExternalFilesDir(null),recvStr.split("\\|")[1]);
                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),recvStr.split("\\|")[1]);
                    long filesize = Long.parseLong(recvStr.split("\\|")[2]);
                    dashboardViewModel.setInfo("文件存放路径:"+file.getAbsoluteFile());
                    Log.e(TAG,"filepath:"+file.getAbsoluteFile());
                    Log.e(TAG,"filesize:"+filesize);
                    OutputStream f = new FileOutputStream(file);
                    long count = 0;
                    while(true){
                        int readcount = in.read(rs);
                       // Log.e(TAG,"readcount:"+readcount);
                        f.write(rs,0,readcount);
                        count += readcount;
                     //   Log.e(TAG,"count:"+count);
                        if(count >= filesize){
                            break;
                        }
                    }
                    Log.e(TAG,"count:"+count);
                    */

                    String filename = recvStr.split("\\|")[1];
                    long filesize = Long.parseLong(recvStr.split("\\|")[2]);
                    Log.e(TAG,"filesize:"+filesize);
                    if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q) {
                        String ext = "";
                        if (filename.lastIndexOf(".") > -1) {
                            ext = filename.substring(filename.lastIndexOf(".") + 1);
                        }
                        Log.e(TAG, "ext:"+ext);
                        String mtype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
                        Log.e(TAG, "mime type:"+mtype);

                        String path = "Download/Reminder/";
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(MediaStore.Downloads.RELATIVE_PATH, path);
                        contentValues.put(MediaStore.Downloads.DISPLAY_NAME, filename);
                        contentValues.put(MediaStore.Downloads.MIME_TYPE, mtype);

                        ContentResolver contentResolver = ct.getContentResolver();
                        Uri uri = null;
                        synchronized(this) {
                            uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
                        }
                        Log.e(TAG, String.valueOf(MediaStore.Downloads.EXTERNAL_CONTENT_URI));
                        if (uri == null) {
                            dashboardViewModel.setInfo("存储失败！");
                            return;
                        }
                        dashboardViewModel.setInfo("文件存放路径:"+Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+path+filename);
                        f = contentResolver.openOutputStream(uri);
                    }else{
                        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/Reminder");
                        //File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                        Log.e(TAG,"dir:"+dir.getAbsoluteFile());
                        synchronized(this) {
                            if (!dir.exists()) {
                                if (!dir.mkdirs()) {
                                    dashboardViewModel.setInfo("创建目录失败:" + dir.getAbsoluteFile());
                                    return;
                                }
                            }
                        }
                        File file = new File(dir,recvStr.split("\\|")[1]);
                        filesize = Long.parseLong(recvStr.split("\\|")[2]);
                        dashboardViewModel.setInfo("文件存放路径:"+file.getAbsoluteFile());
                        Log.e(TAG,"filepath:"+file.getAbsoluteFile());
                        Log.e(TAG,"filesize:"+filesize);
                        f = new FileOutputStream(file);
                    }

                    if(filesize!=0){
                        long count = 0;
                        while(true){
                            int readcount = in.read(rs);
                           // Log.e(TAG,"readcount:"+readcount);
                            f.write(rs,0,readcount);
                            count += readcount;
                            //   Log.e(TAG,"count:"+count);
                            if(count >= filesize){
                                break;
                            }
                        }
                        Log.e(TAG,"count:"+count);
                    }



                    Log.e(TAG,"receive file done");
                    dashboardViewModel.setInfo(filename+"接收完毕!");
                   // f.close();
                  //  sock.close();
                }
        }catch(IOException e){
            dashboardViewModel.setInfo("接收失败:"+e.getMessage());
            e.printStackTrace();
        }finally{
            try {
                if (f != null) {
                    f.flush();
                    f.close();
                }
                sock.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
