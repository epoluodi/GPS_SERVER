package com.apollo.gps_server;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.apollo.gps_server.WebService.APP;
import com.apollo.gps_server.WebService.WebThreadDo;
import com.apollo.gps_server.WebService.Webservice;
import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

import org.ksoap2.serialization.PropertyInfo;

import java.util.Timer;
import java.util.TimerTask;


public class GPSAPP extends Activity {



    private Button buttonconfig,btnsendself;
    private TextView textViewusername;
    private TextView textlocation,txtdeviceid;
    private GPS_SERVER gps_server;
    private String username;

    private String Lat,Lng;

    Timer timer;
    private GeoCoder mSearch = null; // 搜索模块，也可去掉地图模块独立使用

    DownloadManager downloadManager;
    DownloadCompleteReceiver downloadCompleteReceiver;


    public class DownloadServer {
        Context context;
        DownloadManager downloadManager;
        DownloadCompleteReceiver downloadCompleteReceiver;

        /**
         * 初始化下载器 *
         */

        public DownloadServer(Context context1, DownloadCompleteReceiver downloadCompleteReceiver1) {
            downloadCompleteReceiver = downloadCompleteReceiver1;
            context = context1;
        }

        public DownloadManager initDownloadServer(final String updateurl) {

            try {
                downloadManager = (DownloadManager) context.getSystemService(context.DOWNLOAD_SERVICE);

                //设置下载地址
                DownloadManager.Request down = new DownloadManager.Request(
                        Uri.parse(updateurl));
                down.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE
                        | DownloadManager.Request.NETWORK_WIFI);
                down.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
                down.setVisibleInDownloadsUi(true);
                down.setDestinationInExternalFilesDir(context,
                        Environment.DIRECTORY_DOWNLOADS, "gps.apk");
                downloadManager.enqueue(down);

                return downloadManager;
            }
            catch (Exception e)
            {
                return null;
            }

        }


    }


    // 接受下载完成后的intent
    public class DownloadCompleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            //判断是否下载完成的广播
            if (intent.getAction().equals(
                    DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {

                //获取下载的文件id
                long downId = intent.getLongExtra(
                        DownloadManager.EXTRA_DOWNLOAD_ID, -1);

                //自动安装apk
                unregisterReceiver(downloadCompleteReceiver);
                installAPK(downloadManager.getUriForDownloadedFile(downId));
            }
        }

        /**
         * 安装apk文件
         */
        private void installAPK(Uri apk) {

            // 通过Intent安装APK文件
            if (apk ==null)
            {

                Toast.makeText(GPSAPP.this,"下载更新失败，请重新尝试",Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intents = new Intent();
            intents.setAction("android.intent.action.VIEW");
            intents.addCategory("android.intent.category.DEFAULT");
            intents.setType("application/vnd.android.package-archive");
            intents.setData(apk);
            intents.setDataAndType(apk, "application/vnd.android.package-archive");
            intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intents);
            finish();


        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpsapp);
        textlocation=(TextView)findViewById(R.id.textlocation);
        textViewusername = (TextView)findViewById(R.id.textuser);
        buttonconfig = (Button)findViewById(R.id.config);
        buttonconfig.setOnClickListener(onClickListener);
        txtdeviceid = (TextView)findViewById(R.id.txtdeviceid);
        txtdeviceid.setText("设备ID:"+APP.getAPP().androidId);
        btnsendself = (Button)findViewById(R.id.sendself);
        btnsendself.setOnClickListener(onClickListenersendself);


        Intent intent = new Intent(GPSAPP.this,GPS_SERVER.class);
        bindService(intent,serviceConnection,BIND_AUTO_CREATE);
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(onGetGeoCoderResultListener);

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                BDLocation location=gps_server.getLocation();
                if (location == null)
                    handler.sendEmptyMessage(0);
                else
                {

                    LatLng ptCenter = new LatLng(location.getLatitude(), location.getLongitude());
                    // 反Geo搜索
                    mSearch.reverseGeoCode(new ReverseGeoCodeOption()
                            .location(ptCenter));

                }

            }
        },1500,5000);


    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what)
            {
                case 0:
                    textlocation.setText("没有获得位置信息");
                    break;
                case 1:
                    textlocation.setText(msg.obj.toString());
                    break;
            }
        }
    };


    OnGetGeoCoderResultListener onGetGeoCoderResultListener=new OnGetGeoCoderResultListener() {
        @Override
        public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

        }

        @Override
        public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
            if (reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                textlocation.setText("没有获得位置信息");
                return;
            }
            textlocation.setText("位置:"+reverseGeoCodeResult.getAddress());
        }
    };
    @Override
    protected void onPause() {
        super.onPause();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
        unbindService(serviceConnection);
    }


    View.OnClickListener onClickListenersendself = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            String updateurl = "http://218.91.156.62:8091/UpdateApp/gps.apk";
            downloadCompleteReceiver = new DownloadCompleteReceiver();
            DownloadServer downloadServer = new DownloadServer(GPSAPP.this, downloadCompleteReceiver);

            registerReceiver(downloadCompleteReceiver, new IntentFilter(
                    DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            downloadManager = downloadServer.initDownloadServer(updateurl);
            if (downloadManager == null)
            {
                unregisterReceiver(downloadCompleteReceiver);
                Toast.makeText(GPSAPP.this,"更新失败",Toast.LENGTH_SHORT).show();
            }
        }
    };
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            final EditText et = new EditText(GPSAPP.this);
            et.setHint("输入设备标识");
            if (!gps_server.getUsername().equals(""))
                et.setText(gps_server.getUsername());
            new AlertDialog.Builder(GPSAPP.this).setTitle("设备标识")

                    .setView(et)


                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String input = et.getText().toString();
                            if (input.equals("")) {
                                Toast.makeText(getApplicationContext(), "设备标识不能为空！" + input, Toast.LENGTH_LONG).show();
                            }
                            else
                            {
                                gps_server.updateusername(input);
                                Toast.makeText(getApplicationContext(), "设置完成！" + input, Toast.LENGTH_LONG).show();
                                textViewusername.setText("注册使用人：" +gps_server.getUsername());
                            }

                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();


        }
    };


    ServiceConnection serviceConnection=  new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            gps_server=((GPS_SERVER.MyBinder)iBinder).Getserver();
            username=gps_server.getUsername();
            textViewusername.setText("注册使用人：" +username);





        }


        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            gps_server=null;
        }
    };




}
