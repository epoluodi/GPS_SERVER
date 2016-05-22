package com.apollo.gps_server;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 14-6-5.
 */
public class DBManager {

    public static String DBPath;

    public String userdbpath="";
    public SQLiteDatabase db;
    private Context context;
    SQLiteOpenHelper sqLiteOpenHelper;


    public DBManager(Context context, String userDbpath) {
        sqLiteOpenHelper = new SQLiteOpenHelper(context,userDbpath,null,1) {
            @Override
            public void onCreate(SQLiteDatabase sqLiteDatabase) {

            }

            @Override
            public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {

            }
        };

        userdbpath= userDbpath;
        //因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0, mFactory);
        //所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
        this.context = context;

        db  = sqLiteOpenHelper.getWritableDatabase();
    }




    public void closeDB() {
        if (db !=null)
            db.close();
    }


    public String getusername()
    {
        String user="";
        try {
            Cursor cursor = db.rawQuery("select name from config ", null);
            if (cursor.moveToNext()) {
                user = cursor.getString(0);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }


    public void updateusername(String username)
    {

        try {
            ContentValues cv = new ContentValues();
            cv.put("name",username);
            db.update("config",cv,""
                    ,null);




        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return;
    }

    public static String GetSysDate() {
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = sDateFormat.format(new java.util.Date());
        return date;
    }


    public void insertgpsdata(String latitude,String Longitude)
    {

        try {
            ContentValues cv = new ContentValues();
            cv.put("latitude",latitude);
            cv.put("Longitude",Longitude);
            cv.put("dt",GetSysDate());
            db.insert("gps","",cv);




        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return;
    }






    public List<Map<String,String>> queryGPS() {
        try {
            Map<String,String> map;
            List<Map<String,String>> list = new ArrayList<Map<String, String>>();
            Cursor c = db.rawQuery("SELECT * FROM gps", null);
            while (c.moveToNext()) {
                map = new HashMap<String, String>();
                map.put("latitude",c.getString(0));
                map.put("Longitude",c.getString(1));
                map.put("dt",c.getString(2));
                list.add(map);
            }
            c.close();
            return list;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

    }


    public void deleteGPS(String dt) {

        db.delete("gps","dt = ? ",new String[]{dt});

    }





}
