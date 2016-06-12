package com.apollo.gps_server;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.apollo.gps_server.WebService.WebThreadDo;
import com.apollo.gps_server.WebService.Webservice;
import com.baidu.location.BDLocation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ksoap2.serialization.PropertyInfo;

public class MapActivity extends Activity {

    private MapView mapView;
    private BaiduMap baiduMap;
    private GPS_SERVER gps_server;
    private ImageView btnloc, btnsetting, btnsendloc, btnrefreshc, btnstate;
    private Boolean isfristrun = true;
    private TextView statetxt;
    private BitmapDescriptor bitmapDescriptor, bitmapDescriptor2;
    private JSONArray jsonArray1, jsonArray2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mapView = (MapView) findViewById(R.id.map);
        mapView.showZoomControls(false);
        baiduMap = mapView.getMap();

        btnloc = (ImageView) findViewById(R.id.btnloc);
        btnloc.setOnClickListener(onClickListenerloc);
        btnsetting = (ImageView) findViewById(R.id.btnsetting);
        btnsetting.setOnClickListener(onClickListenersetting);

        btnsendloc = (ImageView) findViewById(R.id.btnsendloc);
        btnsendloc.setOnClickListener(onClickListenersendloc);
        btnrefreshc = (ImageView) findViewById(R.id.btnrefreshc);
        btnrefreshc.setOnClickListener(onClickListenerrefreshc);
        btnstate = (ImageView) findViewById(R.id.state);
        statetxt = (TextView) findViewById(R.id.statetxt);

        bitmapDescriptor = BitmapDescriptorFactory
                .fromResource(R.drawable.c);
        bitmapDescriptor2 = BitmapDescriptorFactory
                .fromResource(R.drawable.p);

        Intent intent = new Intent(this, GPS_SERVER.class);
        startService(intent);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);

        baiduMap.setMyLocationEnabled(true);
        baiduMap.setMyLocationConfigeration(new MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.NORMAL, true, null));
        baiduMap.setOnMarkerClickListener(markerClickListener);
        baiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                baiduMap.hideInfoWindow();
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });
    }

    BaiduMap.OnMarkerClickListener markerClickListener = new BaiduMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {

            Button button = new Button(getApplicationContext());
            button.setBackgroundResource(R.drawable.popup);
            InfoWindow.OnInfoWindowClickListener listener = null;
            button.setTextColor(getResources().getColor(R.color.black));
            JSONObject jsonObject ;
            int i = marker.getExtraInfo().getInt("index");
            try {


                if (marker.getExtraInfo().getInt("model") == 0) {
                    jsonObject = jsonArray1.getJSONObject(i);
                    button.setText("[" + jsonObject.getString("cid") + "]" +
                            jsonObject.getString("name"));
                }
                else
                {
                    jsonObject = jsonArray2.getJSONObject(i);
                    button.setText(jsonObject.getString("name"));
                }
                listener = new InfoWindow.OnInfoWindowClickListener() {
                    public void onInfoWindowClick() {
                        baiduMap.hideInfoWindow();
                    }
                };
                LatLng ll = marker.getPosition();
                InfoWindow mInfoWindow = new InfoWindow(BitmapDescriptorFactory.fromView(button), ll, -47, listener);
                baiduMap.showInfoWindow(mInfoWindow);

            } catch (Exception e) {
                e.printStackTrace();
            }


            return true;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        baiduMap.clear();
        unbindService(serviceConnection);
        baiduMap.setMyLocationEnabled(false);
        bitmapDescriptor.recycle();
        bitmapDescriptor2.recycle();
        mapView.onDestroy();
    }


    View.OnClickListener onClickListenerloc = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                BDLocation bdLocation = gps_server.getLocation();
                LatLng ll = new LatLng(bdLocation.getLatitude(),
                        bdLocation.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(17.0f);
                baiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
            catch (Exception e)
            {e.printStackTrace();}

        }
    };

    View.OnClickListener onClickListenersetting = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Intent intent = new Intent(MapActivity.this, GPSAPP.class);
            startActivity(intent);
        }
    };


    View.OnClickListener onClickListenersendloc = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final EditText et = new EditText(MapActivity.this);
            et.setHint("输入当前位置客户名称");
            new AlertDialog.Builder(MapActivity.this).setTitle("输入位置信息")

                    .setView(et)


                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String input = et.getText().toString();

                            BDLocation bdLocation = gps_server.getLocation();
                            if (input.equals("")) {
                                Toast.makeText(getApplicationContext(), "不能为空！" + input, Toast.LENGTH_LONG).show();
                            } else {
                                PropertyInfo[] propertyInfos2 = new PropertyInfo[3];
                                PropertyInfo propertyInfo2 = new PropertyInfo();
                                propertyInfo2.setName("companyname");
                                propertyInfo2.setValue(input);
                                propertyInfos2[0] = propertyInfo2;
                                propertyInfo2 = new PropertyInfo();
                                propertyInfo2.setName("latitude");
                                propertyInfo2.setValue(String.valueOf(bdLocation.getLatitude()));
                                propertyInfos2[1] = propertyInfo2;
                                propertyInfo2 = new PropertyInfo();
                                propertyInfo2.setName("Longitude");
                                propertyInfo2.setValue(String.valueOf(bdLocation.getLongitude()));
                                propertyInfos2[2] = propertyInfo2;


                                WebThreadDo webThreadDo = new WebThreadDo(propertyInfos2, "A_PDA_sendGPSinfo");
                                webThreadDo.requstWebinterfaceForString(true);
                                String r = webThreadDo.amessage.obj.toString();

                                if (r.equals("1")) {
                                    Toast.makeText(MapActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MapActivity.this, "发送失败", Toast.LENGTH_SHORT).show();
                                }

                            }

                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();

        }
    };

    View.OnClickListener onClickListenerrefreshc = new View.OnClickListener() {
        @Override
        public void onClick(View view) {


            new Thread(runnable).start();


        }
    };

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Webservice webservice = new Webservice("Http://218.91.156.62:9229/", 10000);
            String r = webservice.PDA_GetInterFaceForStringNew(null, "A_PDA_getGPSList");
            if (r.equals("-1"))
                handler.sendEmptyMessage(1);
            else {
                Message message = handler.obtainMessage();
                message.what = 0;
                message.obj = r;

                handler.sendMessage(message);
            }
        }
    };
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case 0:

                    try {


                        JSONObject jsonObjectmain = new JSONObject(msg.obj.toString());

                        jsonArray1 = jsonObjectmain.getJSONArray("c");
                        JSONObject jsonObject;
                        baiduMap.clear();
                        Bundle bundle ;


                        for (int i = 0; i < jsonArray1.length(); i++) {

                            jsonObject = jsonArray1.getJSONObject(i);
                            LatLng latLng = new LatLng(Float.valueOf(jsonObject.getString("lat")),
                                    Float.valueOf(jsonObject.getString("lng")));
                            MarkerOptions markerOptions = new MarkerOptions().position(latLng).
                                    icon(bitmapDescriptor)
                                    .draggable(false).title(jsonObject.getString("cid"));
                            bundle = new Bundle();
                            bundle.putInt("model", 0);
                            bundle.putInt("index",i);
                            markerOptions.extraInfo(bundle);
                            markerOptions.animateType(MarkerOptions.MarkerAnimateType.drop);
                            baiduMap.addOverlay(markerOptions);


                        }

                        jsonArray2 = jsonObjectmain.getJSONArray("p");
                        for (int i = 0; i  < jsonArray2.length() ; i++) {


                            jsonObject = jsonArray2.getJSONObject(i);
                            if (gps_server.getUsername().equals(jsonObject.getString("name")))
                                continue;
                            LatLng latLng = new LatLng(Float.valueOf(jsonObject.getString("lat")),
                                    Float.valueOf(jsonObject.getString("lng")));
                            MarkerOptions markerOptions = new MarkerOptions().position(latLng).
                                    icon(bitmapDescriptor2)
                                    .draggable(false).title(jsonObject.getString("name"));
                            bundle = new Bundle();
                            bundle.putInt("model", 1);
                            bundle.putInt("index",i);
                            markerOptions.extraInfo(bundle);
                            markerOptions.animateType(MarkerOptions.MarkerAnimateType.drop);
                            baiduMap.addOverlay(markerOptions);


                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(MapActivity.this, "刷新成功", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    Toast.makeText(MapActivity.this, "刷新失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    void UpdateLoc() {

        BDLocation bdLocation = gps_server.getLocation();
        MyLocationData locData = new MyLocationData.Builder()
                .accuracy(bdLocation.getRadius())
                // 此处设置开发者获取到的方向信息，顺时针0-360
                .direction(bdLocation.getDirection()).latitude(bdLocation.getLatitude())
                .longitude(bdLocation.getLongitude()).build();
        baiduMap.setMyLocationData(locData);

    }

    GPS_SERVER.IGPSCallBack igpsCallBack = new GPS_SERVER.IGPSCallBack() {
        @Override
        public void OnFiishLoc() {
            UpdateLoc();
            if (isfristrun) {
                isfristrun = false;
                onClickListenerloc.onClick(null);
            }
        }

        @Override
        public void OnUpdateState(Boolean b) {
            if (b) {
                btnstate.setBackground(getResources().getDrawable(R.drawable.ygreen));
                statetxt.setText("已连接");
            } else {
                btnstate.setBackground(getResources().getDrawable(R.drawable.yred));
                statetxt.setText("已断开");
            }

        }
    };
    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            gps_server = ((GPS_SERVER.MyBinder) iBinder).Getserver();
            gps_server.setIgpsCallBack(igpsCallBack);

        }


        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            gps_server = null;
        }
    };


}
