package com.apollo.gps_server;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.apollo.gps_server.WebService.WebThreadDo;
import com.apollo.gps_server.WebService.Webservice;

import org.ksoap2.serialization.PropertyInfo;

import java.util.Timer;
import java.util.TimerTask;


public class GPSAPP extends Activity {



    Button buttonconfig,btnsendself;
    TextView textViewusername;
    TextView textlocation;
    GPS_SERVER gps_server;
    String username;

    String Lat,Lng;

    Timer timer;
    SharedPreferences sharedPreferences ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpsapp);
        textlocation=(TextView)findViewById(R.id.textlocation);
        textViewusername = (TextView)findViewById(R.id.textuser);
        buttonconfig = (Button)findViewById(R.id.config);
        buttonconfig.setOnClickListener(onClickListener);

        btnsendself = (Button)findViewById(R.id.sendself);
        btnsendself.setOnClickListener(onClickListenersendself);

        sharedPreferences = getSharedPreferences("GPS_ZY",MODE_PRIVATE);
        Intent intent = new Intent(GPSAPP.this,GPS_SERVER.class);
        startService(intent);


        try
        {
            Thread.sleep(500);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Location location=gps_server.getLocation();
                if (location == null)
                    handler.sendEmptyMessage(0);
                else
                {
                    Message message = handler.obtainMessage();
                    message.what=1;
                    Lng = String.valueOf(location.getLongitude());
                    Lat = String.valueOf(location.getLatitude());

                    message.obj = "经度：" + String.valueOf(location.getLatitude())+
                            "\n纬度：" +  String.valueOf(location.getLongitude());
                    handler.sendMessage(message);
                }

            }
        },1500,5000);


    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(GPSAPP.this,GPS_SERVER.class);
        bindService(intent,serviceConnection,BIND_AUTO_CREATE);

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


    @Override
    protected void onPause() {
        super.onPause();
        unbindService(serviceConnection);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }


    View.OnClickListener onClickListenersendself = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final EditText et = new EditText(GPSAPP.this);
            et.setHint("输入当前位置公司名称");
            new AlertDialog.Builder(GPSAPP.this).setTitle("输入位置信息")

                    .setView(et)


                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String input = et.getText().toString();


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
                                propertyInfo2.setValue(Lat);
                                propertyInfos2[1] = propertyInfo2;
                                propertyInfo2 = new PropertyInfo();
                                propertyInfo2.setName("Longitude");
                                propertyInfo2.setValue(Lng);
                                propertyInfos2[2] = propertyInfo2;


                                WebThreadDo webThreadDo = new WebThreadDo(propertyInfos2, "A_PDA_sendGPSinfo");
                                webThreadDo.requstWebinterfaceForString(true);
                                String r = webThreadDo.amessage.obj.toString();

                                if (r.equals("1")) {
                                    Toast.makeText(GPSAPP.this, "发送成功", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(GPSAPP.this, "发送失败", Toast.LENGTH_SHORT).show();
                                }

                            }

                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();

        }
    };
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            final EditText et = new EditText(GPSAPP.this);
            et.setHint("输入设备标识");
            new AlertDialog.Builder(GPSAPP.this).setTitle("设备标识")

                    .setView(et)


                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String input = et.getText().toString();
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("name",input);
                            editor.commit();
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
