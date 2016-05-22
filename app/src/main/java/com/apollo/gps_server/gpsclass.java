package com.apollo.gps_server;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * Created by Administrator on 14-6-25.
 */
public class gpsclass {


    Context context;
    LocationManager locationManager;
    LocationManager locationManager2;
    public boolean isgps=false;
    public boolean isgps2=false;
    String provider;
    String provider2;
    public Location locationNow;

    public gpsclass(Context context1)
    {
        context=context1;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationManager2 = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            isgps= true;
        else
            isgps= false;

        if (locationManager2.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            isgps2= true;
        else
            isgps2= false;


        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE); // 高精度
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setSpeedRequired(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW); // 低功耗
        provider = locationManager.getBestProvider(criteria,true); // 获取GPS信息



        Criteria criteria2 = new Criteria();
        criteria2.setAccuracy(Criteria.ACCURACY_COARSE); // 高精度
        criteria2.setAltitudeRequired(false);
        criteria2.setBearingRequired(false);
        criteria2.setCostAllowed(true);
        criteria2.setSpeedRequired(false);
        criteria2.setPowerRequirement(Criteria.POWER_LOW); // 低功耗
        provider2 = locationManager2.getBestProvider(criteria,true); // 获取GPS信息


    }


    public void closegpslistener()
    {
        locationManager.removeUpdates(locationListener);

    }
    public void setLocation()
    {


        // 查找到服务信息
//        Criteria criteria = new Criteria();
//        criteria.setAccuracy(Criteria.ACCURACY_FINE); // 高精度
//        criteria.setAltitudeRequired(false);
//        criteria.setBearingRequired(false);
//        criteria.setCostAllowed(true);
//        criteria.setPowerRequirement(Criteria.POWER_LOW); // 低功耗
//        String provider = locationManager.getBestProvider(criteria, true); // 获取GPS信息

//        locationManager.getProvider(LocationManager.NETWORK_PROVIDER).getName();
//        Location location = locationManager.getLastKnownLocation(provider); // 通过GPS获取位置
//        updateToNewLocation(location);
        // 设置监听器，自动更新的最小时间为间隔N秒(1秒为1*1000，这样写主要为了方便)或最小位移变化超过N米
        locationManager.requestLocationUpdates(provider, 3000, 10,locationListener);

    }

    public Location getgpsinfo()
    {



        Location location = locationManager2.getLastKnownLocation(provider2); // 通过GPS获取位置
//        Log.i("getLongitude", String.valueOf(location.getLongitude()));
//        Log.i("getLatitude", String.valueOf(location.getLatitude()));
        return location;
//
    }


    LocationListener locationListener=new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            locationNow = location;


//            Intent intent=new Intent(Common.NEW_BROADCAST);
//            Bundle bundle=new Bundle();
//            bundle.putDouble("Latitude",location.getLatitude());
//            bundle.putDouble("Longitude",location.getLongitude());
//            intent.putExtras(bundle);
//            intent.setAction(Common.NEW_BROADCAST);
//            context.sendBroadcast(intent);

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };


//
//    public static int submitGPS(List<t_GPS> tGpsList)
//    {
//
//        String xml = "<GPS> \r\n";
//
//        for(int i =0;i<tGpsList.size();i++)
//        {
//
//            xml +="<table> \r\n";
//            xml +="<gpsid>" +tGpsList.get(i).id + "</gpsid> \r\n";
//            xml +="<taskid>" +tGpsList.get(i).taskid + "</taskid> \r\n";
//            xml +="<guestid>" +tGpsList.get(i).guestid + "</guestid> \r\n";
//            xml +="<userid>" +tGpsList.get(i).userId + "</userid> \r\n";
//            xml +="<GPS_Longitude>" +tGpsList.get(i).GPS_Longitude + "</GPS_Longitude> \r\n";
//            xml +="<GPS_Latitude>" +tGpsList.get(i).GPS_Latitude + "</GPS_Latitude> \r\n";
//            xml +="<dt>" +tGpsList.get(i).dt + "</dt> \r\n";
//            xml +="<srvid>" +tGpsList.get(i).srvid + "</srvid> \r\n";
//            xml +="</table> \r\n";
//
//        }
//        xml+="</GPS>";
//
//        PropertyInfo[] propertyInfos2 = new PropertyInfo[1];
//        PropertyInfo propertyInfo2 = new PropertyInfo();
//        propertyInfo2.setName("GPSXML");
//        propertyInfo2.setValue(xml);
//        propertyInfos2[0] = propertyInfo2;
//        Webservice webservice=new Webservice(Common.WCFaddress);
//        String ret = webservice.PDA_GetInterFaceForStringNew(propertyInfos2, "PDA_SubmitGPS");
//
//        return Integer.valueOf(ret);
//
//    }



}
