package com.ben.reminder;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;

import com.ben.reminder.ui.dashboard.DashboardViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.ben.reminder.databinding.ActivityMainBinding;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    //final String TAG = "Reminder";
    private String endpoint = "";  //对端ip
    private String ip = "";  //本机ip
    private String lastip = "";  //上一次的ip
    final int PORT = 6066;
    private String bip = "";  //广播ip
    private ActivityResultLauncher<String> getContentActivity;
    private ActivityResultLauncher<String> launcher;
    private DashboardViewModel dashboardViewModel;
    final String TAG = Thread.currentThread().getStackTrace()[2].getFileName().replace(".java","");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG,"in MainActivity onCreate");
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Log.e(TAG,"after setContentView");

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        //AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
        //        R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications).build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
       // NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        /*Log.e(TAG,Thread.currentThread().getStackTrace().toString());
        for(int i=0;i<Thread.currentThread().getStackTrace().length;i++) {
            Log.e(TAG, Thread.currentThread().getStackTrace()[i].toString());
            Log.e(TAG, Thread.currentThread().getStackTrace()[i].getFileName());
            Log.e(TAG, Thread.currentThread().getStackTrace()[i].getClassName());
            Log.e(TAG, Thread.currentThread().getStackTrace()[i].getMethodName());
            Log.e(TAG, String.valueOf(Thread.currentThread().getStackTrace()[i].getLineNumber()));
        }*/
        Log.e(TAG,"11111");
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        Log.e(TAG,"2222");

//        getContentActivity = registerForActivityResult(new ActivityResultContracts.GetContent(),
//                uri -> {
//                    Log.e(TAG,String.valueOf(uri));
//                    sendFile(uri);
//                });
        getContentActivity = registerForActivityResult(new ActivityResultContracts.GetMultipleContents()/*GetContent()*/,
                list -> {
            Log.e(TAG,String.valueOf(list));
            for(Uri uri : list){
                sendFile(uri);
                try {
                    Thread.currentThread().sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        launcher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                result -> {
                    if (result.equals(true)) {
                        Log.e(TAG,"授权成功");
                        getContentActivity.launch("*/*");
                    } else {
                        Log.e(TAG,"授权失败");
                    }
                });
/*
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             //   Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
             //           .setAction("Action", null).show();
                Toast.makeText(MainActivity.this, "Replace with your own action",Toast.LENGTH_LONG).show();
            }
        });
*/

        Log.e(TAG,getFilesDir().getAbsolutePath());
        Log.e(TAG,getCacheDir().getAbsolutePath());
        Log.e(TAG,getExternalFilesDir(null).getAbsolutePath());
        Log.e(TAG,getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath());
        Log.e(TAG,Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath());
        Log.e(TAG,Environment.getExternalStorageDirectory().getAbsolutePath());

        //顺序不能变
        getLocalIp();
        onClickStart();
        notifyOnline();

        requestPermission();

    }
    public void requestPermission(){
      //  if(Build.VERSION.SDK_INT<Build.VERSION_CODES.Q) {
            int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "需要授权");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }else{
                Log.e(TAG, "已授权");
            }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try{
            for(int i=0;i<permissions.length;i++){
                if(grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    Log.e(TAG,"【"+permissions[i]+"】权限授权成功");
                    dashboardViewModel.setInfo("文件读写权限授权成功。");
                }else{
                    Log.e(TAG,"【"+permissions[i]+"】权限授权未成功");
                    dashboardViewModel.setInfo("文件读写权限授权未成功，无法发送和接收文件，可以按刷新按钮重新授权！");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            Log.e(TAG,"权限申请回调中发生异常");
        }
    }
    public void onClickStart(){
        if(ip.equals("")){
            return;
        }
        //监听发文件请求
        new Thread() {
            @Override
            public void run() {
                try{
                    String host = ip;
                    //int port = 6066;
                    ServerSocket serverSocket = new ServerSocket();
                    serverSocket.bind(new InetSocketAddress(host,PORT));
                    Log.e(TAG,"本地地址:"+serverSocket.getInetAddress());
                    dashboardViewModel.setInfo("正在地址"+serverSocket.getInetAddress()+"上监听发文件请求..");
                    while(true){
                        Socket server = serverSocket.accept();
                        Log.e(TAG,"接收到来自"+server.getRemoteSocketAddress()+"的发文件请求..");
                        //dashboardViewModel.setInfo("接收到来自"+server.getRemoteSocketAddress()+"的发文件请求..");
                        new RecvThread(MainActivity.this,server).start();

                    }
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }.start();

        //监听对端是否上线,获取对端ip
        new Thread() {
            @Override
            public void run(){
                try{
                    DatagramPacket rp = new DatagramPacket(new byte[1024],1024);
                    DatagramSocket sock = new DatagramSocket(PORT);
                    while(true) {
                        sock.receive(rp);
                        byte[] recvByte = Arrays.copyOfRange(rp.getData(), 0, rp.getLength());
                        String rs = new String(recvByte);
                        Log.e(TAG,rs);
                        String tmpe = rp.getAddress().toString().replace("/", "");
                        Log.e(TAG,tmpe);
                        if(!tmpe.equals(ip)) {
                            endpoint = tmpe;
                            dashboardViewModel.getEndpoint().postValue(endpoint);
                            // dashboardViewModel.getInfo().postValue(dashboardViewModel.getInfo().getValue()+"\n获取到对端IP"+endpoint);
                            // final TextView infot = (TextView)MainActivity.this.findViewById(R.id.infotext);
                            // infot.post(() -> infot.append("获取到对端IP"+endpoint));
                            dashboardViewModel.setInfo("获取到对端IP:"+endpoint);

                            //infotext.setText(endpoint);
                            if(rs.equals("reminderonline")) {
                                Log.e(TAG,"notify online");
                                byte[] buffer = "reminderroger".getBytes();
                                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, rp.getAddress(), PORT);
                                sock.send(packet);
                            }
                        }
                    }
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }.start();
    }
    public void getLocalIp(){

        //获取本机ip
        lastip = ip;
        WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        Log.e(TAG, "int ip:"+ipAddress);
        String tempip;
        if(ipAddress!=0) {
            tempip =  ((ipAddress & 0xff)+"."+(ipAddress>>8 & 0xff)+"."
                    +(ipAddress>>16 & 0xff)+"."+(ipAddress>>24 & 0xff));
            Log.e(TAG, "ip:"+tempip);

        }else{
            dashboardViewModel.setInfo("获取不到本机IP，请检查连接！");
            return;
        }

        if(Pattern.matches("\\d+\\.\\d+.\\d+.\\d+",tempip)){
            ip = tempip;
            dashboardViewModel.getIp().setValue(ip);
            //dashboardViewModel.getInfo().setValue(dashboardViewModel.getInfo().getValue()+"\n获取到本机IP"+ip);
            //final TextView infot = (TextView)MainActivity.this.findViewById(R.id.infotext);
            //infot.append("获取到本机IP"+ip);
            dashboardViewModel.setInfo("获取到本机IP:"+ip);
        }else{
            dashboardViewModel.setInfo("获取不到本机IP！");
        }

    }

    public void notifyOnline(){
        if(ip.equals("")){
            return;
        }
        //自己上线通知对端
        new Thread() {
            @Override
            public void run() {
                try {
                    DatagramSocket socket = new DatagramSocket();
                    socket.setBroadcast(true);
                    String[] ip_arr = ip.split("\\.");
                    ip_arr[3] = "255" ;
                    bip = "";
                    for(int i=0;i<ip_arr.length;i++) {
                        if(i==ip_arr.length-1) {
                            bip = bip + ip_arr[i];
                        }else {
                            bip = bip + ip_arr[i] + ".";
                        }
                    }
                    Log.e(TAG,"bip:"+bip);
                    //dashboardViewModel.getInfo().postValue(dashboardViewModel.getInfo().getValue()+"\n向"+bip+"广播自己上线的通知");
                    dashboardViewModel.setInfo("向"+bip+"广播自己上线的通知..");
                    //final TextView infot = (TextView)MainActivity.this.findViewById(R.id.infotext);
                    //infot.post(() -> infot.append("\n向"+bip+"广播自己上线的通知"));
                    InetAddress address = InetAddress.getByName(bip);
                    byte[] buffer = "reminderonline".getBytes();
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, PORT);
                    socket.send(packet);
                    socket.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }.start();

    }
    public void onClickSendFile(){
        if(endpoint.equals("")){
            dashboardViewModel.setInfo("未获取对端ip，请尝试刷新！");
            return;
        }
        launcher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }
    public void onClickRefresh(){
        getLocalIp();
        if(!ip.equals(lastip)){ //如果ip相同就不要重新开始了
            onClickStart();
        }
        notifyOnline();
        requestPermission();
        //dashboardViewModel.setInfo("刷新完成！");
    }
    private void sendFile(Uri uri) {
        if(uri==null) {return;}
        Log.e(TAG,uri.toString());
        new Thread() {
      //     @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void run() {
                InputStream f = null;
                Socket client = null;
                try {
                    //获取文件信息
                    String filename = "unknown";
                    long filesize = 0;

                    if(uri.toString().contains("content://")) {
                        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                        if (cursor != null) {
                            while (cursor.moveToNext()) {
                                String[] columns = cursor.getColumnNames();
                                Log.e(TAG, Arrays.toString(columns));
                                for (String s : columns) {
                                    Log.e(TAG, s);
                                    int index = cursor.getColumnIndex(s);
                                    int type = cursor.getType(index);
                                    Log.e(TAG, String.valueOf(type));
                                    switch (type) {
                                        case Cursor.FIELD_TYPE_INTEGER:
                                            int column = cursor.getInt(index);
                                            Log.e(TAG, String.valueOf(column));
                                            break;
                                        case Cursor.FIELD_TYPE_STRING:
                                            String column2 = cursor.getString(index);
                                            Log.e(TAG, column2);
                                            break;
                                        case Cursor.FIELD_TYPE_BLOB:
                                            Log.e(TAG, Arrays.toString(cursor.getBlob(index)));
                                            break;
                                        case Cursor.FIELD_TYPE_FLOAT:
                                            Log.e(TAG, String.valueOf(cursor.getFloat(index)));
                                            break;
                                        case Cursor.FIELD_TYPE_NULL:
                                            Log.e(TAG, "NULL type");
                                            break;
                                    }
                                }
                                int name_index = cursor.getColumnIndex("_display_name");
                                if (name_index > -1) {
                                    filename = cursor.getString(name_index);
                                }
                                int size_index = cursor.getColumnIndex("_size");
                                if (size_index > -1) {
                                    filesize = cursor.getInt(size_index);
                                }
                            }
                            cursor.close();
                            f = getContentResolver().openInputStream(uri);
                        }
                    }else if(uri.toString().contains("file://")){
                        File file = new File(String.valueOf(uri).replace("file://",""));
                        filename = file.getName();
                        filesize = file.length();
                        f = new FileInputStream(file);
                        //Uri.fromFile()
                    }else{
                        dashboardViewModel.setInfo("无法识别文件路径:"+ uri);
                        return;
                    }
                    Log.e(TAG, "filename:" + filename);
                    Log.e(TAG, "filesize:" + filesize);
                    //发送文件
                    String serverName = endpoint;
                    //dashboardViewModel.setInfo("连接"+serverName+"...");
                    client = new Socket(serverName, PORT);
                    DataOutputStream out = new DataOutputStream(client.getOutputStream());
                    DataInputStream in = new DataInputStream(client.getInputStream());
                 //   InputStream f = getContentResolver().openInputStream(uri);
                    filesize = f.available();
                    Log.e(TAG,"size:"+filesize);

                    dashboardViewModel.setInfo("向"+serverName+"发送文件sendfile|"+filename+"|"+filesize);
                    //out.writeUTF("sendfile|"+filename+"|"+size);
                    out.write(("sendfile|" + filename + "|" + filesize).getBytes());
                    byte[] rs = new byte[1024];
                    int rc = in.read(rs);
                    Log.e(TAG,"rc:"+rc);
                    String recvStr = new String(Arrays.copyOfRange(rs, 0, rc));
                    Log.e(TAG,recvStr);
                    long count = 0;
                    while(true){
                        int readcount = f.read(rs);
                       // Log.e(TAG,"readcount:"+readcount);
                        if(readcount == -1){
                            break;
                        }
                        // System.out.println(new String(rs));
                        out.write(rs,0,readcount);
                        count += readcount;
                    }
                    // out.writeUTF("done");
                    Log.e(TAG,"count:"+count);
                    dashboardViewModel.setInfo(filename+"发送完成!");
                   // client.close();
                    //f.close();
                } catch (IOException e) {
                    Log.e(TAG,e.toString());
                    dashboardViewModel.setInfo("发送失败:"+e.getMessage());
                    e.printStackTrace();
                }finally{
                    try {
                        if (f != null) {
                            f.close();
                        }
                        if(client != null){
                            client.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }
    /**
     * 实现返回时，不退出应用
     */
    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        Log.e(TAG,"in onBackPressed");
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }
}