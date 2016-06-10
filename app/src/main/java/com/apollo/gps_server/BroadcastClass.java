package com.apollo.gps_server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Administrator on 14-11-13.
 */
public class BroadcastClass extends BroadcastReceiver{


    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context,"GPS服务启动",Toast.LENGTH_SHORT).show();

            Intent intent1 = new Intent(context,GPS_SERVER.class);
            context.startService(intent1);
            Log.i("服务" ,"启动");
    }
}
