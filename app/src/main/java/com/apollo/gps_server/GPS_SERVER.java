package com.apollo.gps_server;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.apollo.gps_server.WebService.APP;
import com.apollo.gps_server.WebService.Webservice;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import org.ksoap2.serialization.PropertyInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.RunnableFuture;
import java.util.logging.Handler;

/**
 * Created by Administrator on 14-11-13.
 */
public class GPS_SERVER extends Service {

    private final int ScanSpan = 1000 * 10;
    private final IBinder binder = new MyBinder();
//    DBManager dbManager = null;
    private String username = "", deviceid;

    private GPS_SERVER gps_server;
    private Boolean lastupdatestate = true;
    private IGPSCallBack igpsCallBack;
    private BDLocation bdLocation;
    private LocationClient client = null;
    private LocationClientOption mOption, DIYoption;


    public void setIgpsCallBack(IGPSCallBack igpsCallBack) {
        this.igpsCallBack = igpsCallBack;
    }

    public LocationClientOption getDefaultLocationClientOption() {
        if (mOption == null) {
            mOption = new LocationClientOption();
            mOption.setOpenGps(true);
            mOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
            mOption.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系，如果配合百度地图使用，建议设置为bd09ll;
            mOption.setScanSpan(ScanSpan);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
            mOption.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
            mOption.setIsNeedLocationDescribe(true);//可选，设置是否需要地址描述
            mOption.setNeedDeviceDirect(false);//可选，设置是否需要设备方向结果
            mOption.setLocationNotify(false);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
            mOption.setIgnoreKillProcess(true);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
            mOption.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
            mOption.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
            mOption.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        }
        return mOption;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
//        if (dbManager != null)
//            dbManager.closeDB();
        client.unRegisterLocationListener(listener);
        client.stop();
//        gpsclass.closegpslistener();
//        timer.cancel();

    }

    @Override
    public void onCreate() {
        super.onCreate();

        client = new LocationClient(getApplicationContext());
        client.setLocOption(getDefaultLocationClientOption());
        client.registerLocationListener(listener);

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification baseNF = new Notification();
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.gps);
        builder.setTicker("GPS服务启动");
        builder.setDefaults(Notification.DEFAULT_LIGHTS);
        builder.setContentTitle("服务已经启动");
        builder.setContentText("正在搜索位置");

        Notification notification=builder.build();
        nm.notify(0, notification);
//
//        baseNF.icon = R.drawable.gps;
//        baseNF.tickerText = "GPS服务启动";
//        baseNF.defaults = Notification.DEFAULT_LIGHTS;
//        baseNF.setLatestEventInfo(getApplicationContext(), "服务已经启动", "正在搜索位置", null);
//        nm.notify(0, baseNF);

        File file = new File(Environment.getExternalStorageDirectory() + "/zhongyuan/");
        if (!file.exists())
            file.mkdir();

        boolean isfirst = CheckDbfile();
        if (isfirst == false)
            CopyDb();

//        dbManager = new DBManager(this,
//                Environment.getExternalStorageDirectory() + "/zhongyuan/gpsdata.db");

        SharedPreferences sharedPreferences = getSharedPreferences("GPS_ZY", Context.MODE_APPEND);
        username = sharedPreferences.getString("name", "");
        deviceid = APP.getAPP().androidId;
        client.start();

//        gpsclass = new gpsclass(getApplicationContext());
//        gpsclass.setLocation();


//        timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                Location location;
//
//                if (gpsclass.locationNow ==null)
//                    location = gpsclass.getgpsinfo();
//                else
//                {
//                    location = gpsclass.locationNow;
//                    gpsclass.locationNow=null;
//                }
//                if (location == null)
//                    return;
//                if (locationold == null)
//                    locationold = location;
//                else {
//
//                    if (location.getLatitude() == locationold.getLatitude() &&
//                            location.getLongitude() == locationold.getLongitude())
//                        return;
//                    else
//                        locationold = location;
//                }
//                double latitude = location.getLatitude();
//                double Longitude = location.getLongitude();
//                String note= "经度:" + String.valueOf(latitude) + "\n";
//                note += "纬度:" + String.valueOf(Longitude) + "\n";
//
//                NotificationManager nm =  (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
//                Notification baseNF = new Notification();
//                baseNF.icon = R.drawable.gps;
//                baseNF.tickerText = "获得当前GPS信息";
//                baseNF.defaults = Notification.DEFAULT_LIGHTS;
//                baseNF.setLatestEventInfo(getApplicationContext(), "GPS信息", note, null);
//                nm.notify(0, baseNF);
//
//                dbManager.insertgpsdata(String.valueOf(latitude),String.valueOf(Longitude));
//
//                try {
//                    Webservice webservice;
////                    NT1269.88IP.ORG
//                    webservice = new Webservice("Http://218.91.156.62:9229/", 10000);
//                    String r = webservice.PDA_GetInterFaceForStringNew(null, "A_CheckNet");
//                    Log.i("网络检查", "R:" + r);
//
//                    if (r.equals("1")) {
////                        PlaysoundScan2(getApplicationContext(),1);
//                        List<Map<String, String>> gpsList = dbManager.queryGPS();//获取
//                        Map<String, String> map;
//                        for (int i = 0; i < gpsList.size(); i++) {
//                            map = gpsList.get(i);
//
//                            PropertyInfo[] propertyInfos2 = new PropertyInfo[4];
//                            PropertyInfo propertyInfo2 = new PropertyInfo();
//                            propertyInfo2.setName("latitude");
//                            propertyInfo2.setValue(map.get("latitude"));
//                            propertyInfos2[0] = propertyInfo2;
//                            propertyInfo2 = new PropertyInfo();
//                            propertyInfo2.setName("Longitude");
//                            propertyInfo2.setValue(map.get("Longitude"));
//                            propertyInfos2[1] = propertyInfo2;
//                            propertyInfo2 = new PropertyInfo();
//                            propertyInfo2.setName("dt");
//                            propertyInfo2.setValue(map.get("dt"));
//                            propertyInfos2[2] = propertyInfo2;
//                            propertyInfo2 = new PropertyInfo();
//                            propertyInfo2.setName("user");
//                            propertyInfo2.setValue(username);
//                            propertyInfos2[3] = propertyInfo2;
//                            webservice = new Webservice("Http://218.91.156.62:9229/", 15000);
//                            r = webservice.PDA_GetInterFaceForStringNew(propertyInfos2, "PDA_submitgps");
//                            if (r.equals("1"))
//                                dbManager.deleteGPS(map.get("dt"));
//                        }
//
//                    }
////                    else
////                        PlaysoundScan2(getApplicationContext(),2);
//                }
//                catch (Exception e)
//                {
//                    e.printStackTrace();
//                }
//            }
//        },1000,60000);


    }

    public static String GetSysTime() {
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = sDateFormat.format(new java.util.Date());
        return date;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    public static void PlaysoundScan2(Context context, int type) {
        MediaPlayer mediaPlayer;
        switch (type) {
            case 1:
                mediaPlayer = MediaPlayer.create(context, R.raw.soundscan_ok);
                mediaPlayer.start();
                break;
            case 2:
                mediaPlayer = MediaPlayer.create(context, R.raw.soundscan_no);
                mediaPlayer.start();
                break;
            case 3:
                mediaPlayer = MediaPlayer.create(context, R.raw.soundscan_ok2);
                mediaPlayer.start();
                break;
            case 4:
                mediaPlayer = MediaPlayer.create(context, R.raw.talitha);
                mediaPlayer.start();
                break;
            case 5:
                mediaPlayer = MediaPlayer.create(context, R.raw.tejat);
                mediaPlayer.start();
                break;
        }

    }


    private void UpdateData() {

        try {
            Webservice webservice;
//                    NT1269.88IP.ORG
            webservice = new Webservice("Http://vc1818.88ip.org:9229/", 10000);
            String r = webservice.PDA_GetInterFaceForStringNew(null, "A_CheckNet");
            Log.i("网络检查", "R:" + r);

            if (r.equals("1")) {
//

                PropertyInfo[] propertyInfos2 = new PropertyInfo[4];
                PropertyInfo propertyInfo2 = new PropertyInfo();
                propertyInfo2.setName("latitude");
                propertyInfo2.setValue(String.valueOf(bdLocation.getLatitude()));
                propertyInfos2[0] = propertyInfo2;
                propertyInfo2 = new PropertyInfo();
                propertyInfo2.setName("Longitude");
                propertyInfo2.setValue(String.valueOf(bdLocation.getLongitude()));
                propertyInfos2[1] = propertyInfo2;
                propertyInfo2 = new PropertyInfo();
                propertyInfo2.setName("user");
                propertyInfo2.setValue(username);
                propertyInfos2[2] = propertyInfo2;
                propertyInfo2 = new PropertyInfo();
                propertyInfo2.setName("deviceid");
                propertyInfo2.setValue(APP.getAPP().androidId);
                propertyInfos2[3] = propertyInfo2;
                webservice = new Webservice("Http://vc1818.88ip.org:9229/", 15000);
                r = webservice.PDA_GetInterFaceForStringNew(propertyInfos2, "PDA_submitgpsNew");
                if (r.equals("1"))
                    handler.sendEmptyMessage(1);
                else
                    handler.sendEmptyMessage(-1);
            } else
                handler.sendEmptyMessage(-1);
        } catch (Exception e) {
            e.printStackTrace();
            handler.sendEmptyMessage(-1);
        }
    }

    android.os.Handler handler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    lastupdatestate = true;
                    if (igpsCallBack !=null)
                        igpsCallBack.OnUpdateState(true);
                    break;
                case -1:
                    lastupdatestate = false;
                    if (igpsCallBack !=null)
                        igpsCallBack.OnUpdateState(false);
                    break;
            }
        }
    };

    private BDLocationListener listener = new BDLocationListener() {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // TODO Auto-generated method stub


            if (location != null) {

                if (bdLocation == null) {
                    bdLocation = location;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            UpdateData();
                        }
                    }).start();
                } else {
                    if (location.getLatitude() == bdLocation.getLatitude() &&
                            location.getLongitude() == bdLocation.getLongitude()) {
                        if (lastupdatestate)
                            return;
                    }
                    bdLocation = location;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            UpdateData();
                        }
                    }).start();

                }

                if (igpsCallBack != null)
                    igpsCallBack.OnFiishLoc();


//                Message locMsg = locHander.obtainMessage();
//                Bundle locData;
//                locData = Algorithm(location);
//                if (locData != null) {
//                    locData.putParcelable("loc", location);
//                    locMsg.setData(locData);
//                    locHander.sendMessage(locMsg);
//                }
            }

        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    };


    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class MyBinder extends Binder {
        public GPS_SERVER Getserver() {
            return GPS_SERVER.this;
        }
    }

    boolean CheckDbfile() {
        File file = new File(Environment.getExternalStorageDirectory() + "/zhongyuan/gpsdata.db");
//        file.delete();
        return file.exists();
    }

    void CopyDb() {
        InputStream inputStream;
        try {

            inputStream = getResources().openRawResource(R.raw.gpsdata);
            byte[] bytebuff = new byte[inputStream.available()];
            inputStream.read(bytebuff);
            File file = new File(Environment.getExternalStorageDirectory() + "/zhongyuan/gpsdata.db");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(bytebuff);
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateusername(String username1) {
        username = username1;
        SharedPreferences sharedPreferences = getSharedPreferences("GPS_ZY", Context.MODE_APPEND);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", username);
        editor.commit();

    }

    public String getUsername() {
        return username;
    }


    public BDLocation getLocation() {
        return bdLocation;
    }


    public interface IGPSCallBack {
        void OnFiishLoc();

        void OnUpdateState(Boolean b);
    }
}
