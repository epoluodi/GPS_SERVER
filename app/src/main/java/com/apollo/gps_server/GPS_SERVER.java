package com.apollo.gps_server;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.apollo.gps_server.WebService.Webservice;

import org.ksoap2.serialization.PropertyInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 14-11-13.
 */
public class GPS_SERVER extends Service{

    private final IBinder binder = new MyBinder();
    DBManager dbManager = null;
    String username="";
    gpsclass gpsclass;
    Timer timer;
    Location locationold = null;

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbManager !=null)
            dbManager.closeDB();
        gpsclass.closegpslistener();
        timer.cancel();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationManager nm =  (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        Notification baseNF = new Notification();
        baseNF.icon = R.drawable.gps;
        baseNF.tickerText = "GPS服务启动";
        baseNF.defaults = Notification.DEFAULT_LIGHTS;
        baseNF.setLatestEventInfo(getApplicationContext(), "", "", null);
        nm.notify(0, baseNF);

        File file = new File(Environment.getExternalStorageDirectory() + "/zhongyuan/");
        if (!file.exists())
            file.mkdir();

        boolean isfirst =CheckDbfile();
        if (isfirst ==false)
            CopyDb();

        dbManager = new DBManager(this,
                Environment.getExternalStorageDirectory() +"/zhongyuan/gpsdata.db" );
        username = dbManager.getusername();

        gpsclass = new gpsclass(getApplicationContext());
        gpsclass.setLocation();



        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Location location;

                if (gpsclass.locationNow ==null)
                    location = gpsclass.getgpsinfo();
                else
                {
                    location = gpsclass.locationNow;
                    gpsclass.locationNow=null;
                }
                if (location == null)
                    return;
                if (locationold == null)
                    locationold = location;
                else {

                    if (location.getLatitude() == locationold.getLatitude() &&
                            location.getLongitude() == locationold.getLongitude())
                        return;
                    else
                        locationold = location;
                }
                double latitude = location.getLatitude();
                double Longitude = location.getLongitude();
                String note= "经度:" + String.valueOf(latitude) + "\n";
                note += "纬度:" + String.valueOf(Longitude) + "\n";

                NotificationManager nm =  (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
                Notification baseNF = new Notification();
                baseNF.icon = R.drawable.gps;
                baseNF.tickerText = "获得当前GPS信息";
                baseNF.defaults = Notification.DEFAULT_LIGHTS;
                baseNF.setLatestEventInfo(getApplicationContext(), "GPS信息", note, null);
                nm.notify(0, baseNF);

                dbManager.insertgpsdata(String.valueOf(latitude),String.valueOf(Longitude));

                try {
                    Webservice webservice;
//                    NT1269.88IP.ORG
                    webservice = new Webservice("Http://218.91.156.62:9229/", 10000);
                    String r = webservice.PDA_GetInterFaceForStringNew(null, "A_CheckNet");
                    Log.i("网络检查", "R:" + r);

                    if (r.equals("1")) {
//                        PlaysoundScan2(getApplicationContext(),1);
                        List<Map<String, String>> gpsList = dbManager.queryGPS();//获取
                        Map<String, String> map;
                        for (int i = 0; i < gpsList.size(); i++) {
                            map = gpsList.get(i);

                            PropertyInfo[] propertyInfos2 = new PropertyInfo[4];
                            PropertyInfo propertyInfo2 = new PropertyInfo();
                            propertyInfo2.setName("latitude");
                            propertyInfo2.setValue(map.get("latitude"));
                            propertyInfos2[0] = propertyInfo2;
                            propertyInfo2 = new PropertyInfo();
                            propertyInfo2.setName("Longitude");
                            propertyInfo2.setValue(map.get("Longitude"));
                            propertyInfos2[1] = propertyInfo2;
                            propertyInfo2 = new PropertyInfo();
                            propertyInfo2.setName("dt");
                            propertyInfo2.setValue(map.get("dt"));
                            propertyInfos2[2] = propertyInfo2;
                            propertyInfo2 = new PropertyInfo();
                            propertyInfo2.setName("user");
                            propertyInfo2.setValue(username);
                            propertyInfos2[3] = propertyInfo2;
                            webservice = new Webservice("Http://218.91.156.62:9229/", 15000);
                            r = webservice.PDA_GetInterFaceForStringNew(propertyInfos2, "PDA_submitgps");
                            if (r.equals("1"))
                                dbManager.deleteGPS(map.get("dt"));
                        }

                    }
//                    else
//                        PlaysoundScan2(getApplicationContext(),2);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        },1000,60000);


    }

    public static void PlaysoundScan2(Context context,int type) {
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



    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class MyBinder extends Binder {
        GPS_SERVER Getserver()
        {
            return GPS_SERVER.this;
        }
    }

    boolean CheckDbfile()
    {
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
            File file = new File(Environment.getExternalStorageDirectory()+"/zhongyuan/gpsdata.db");
            FileOutputStream fileOutputStream =new FileOutputStream(file);
            fileOutputStream.write(bytebuff);
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateusername(String username1)
    {
        username = username1;
        dbManager.updateusername(username);

    }
    public String getUsername() {
        return username;
    }



    public Location getLocation()
    {
        return gpsclass.getgpsinfo();
    }
}
