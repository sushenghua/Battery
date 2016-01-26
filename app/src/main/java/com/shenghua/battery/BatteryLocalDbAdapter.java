package com.shenghua.battery;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by shenghua on 12/3/15.
 */
public class BatteryLocalDbAdapter {

    private BatteryLocalDbHelper dbHelper;

    public BatteryLocalDbAdapter(Context context) {
        dbHelper = new BatteryLocalDbHelper(context);
    }

    public long insertLog(int lastPlug, int plug, int power, long time) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(BatteryLocalDbHelper.CHARGE_LOG_LAST_PLUG, new Integer(lastPlug));
        contentValues.put(BatteryLocalDbHelper.CHARGE_LOG_PLUG, new Integer(plug));
        contentValues.put(BatteryLocalDbHelper.CHARGE_LOG_POWER, new Integer(power));
        contentValues.put(BatteryLocalDbHelper.CHARGE_LOG_TIME, new Long(time));

        long id = db.insert(BatteryLocalDbHelper.CHARGE_LOG_TABLE, null, contentValues);
        db.close();

        return id;
    }

    public String getAll() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] columns = {BatteryLocalDbHelper.CHARGE_LOG_UID,
                            BatteryLocalDbHelper.CHARGE_LOG_LAST_PLUG,
                            BatteryLocalDbHelper.CHARGE_LOG_PLUG,
                            BatteryLocalDbHelper.CHARGE_LOG_POWER,
                            BatteryLocalDbHelper.CHARGE_LOG_TIME};
        Cursor cursor = db.query(BatteryLocalDbHelper.CHARGE_LOG_TABLE, columns,
                null, null, null, null, null);

        //int indexUID = cursor.getColumnIndex(BatteryLocalDbHelper.CHARGE_LOG_UID);
        //cursor.getInt(cursor.getColumnIndex(BatteryLocalDbHelper.CHARGE_LOG_BEGIN_POWER));

        StringBuffer sb = new StringBuffer();
        while (cursor.moveToNext()) {
            long id = cursor.getLong(0);
            int lastPlug = cursor.getInt(1);
            int plug = cursor.getInt(2);
            int power = cursor.getInt(3);
            long time = cursor.getLong(4);
            sb.append(id + ", " + lastPlug + ", " + plug + ", " + power + ", " + time + "\n");
        }

        db.close();
        return sb.toString();
    }

    private String plugStr(int plug) {
        switch (plug) {
            case -1:
                return "null";
            case 10:
                return "out";
            case 0:
                return "ac";
            case 1:
                return "usb";
            case 2:
                return "wless";
        }
        return "err";
    }

    public String getLatestChargeLogString(int queryLimit) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String limit = queryLimit > 0 ? String.valueOf(queryLimit) : null;
        String[] columns = {
                BatteryLocalDbHelper.CHARGE_LOG_UID,
                BatteryLocalDbHelper.CHARGE_LOG_LAST_PLUG,
                BatteryLocalDbHelper.CHARGE_LOG_PLUG,
                BatteryLocalDbHelper.CHARGE_LOG_POWER,
                BatteryLocalDbHelper.CHARGE_LOG_TIME
        };

        Cursor cursor = db.query(BatteryLocalDbHelper.CHARGE_LOG_TABLE, columns,
                null,
                null, null, null, BatteryLocalDbHelper.CHARGE_LOG_TIME + " DESC", limit);

        StringBuffer sb = new StringBuffer();
        while (cursor.moveToNext()) {
            long id = cursor.getLong(0);
            int lastPlug = cursor.getInt(1);
            int plug = cursor.getInt(2);
            int power = cursor.getInt(3);
            long time = cursor.getLong(4);
//            sb.append(id + ", " + lastPlug + ", " + plug + ", " + power + ", " + time + "\n");
            sb.append(id + ", " + plugStr(lastPlug) + ", " + plugStr(plug) + ", " + power + ", " + time + "\n");
        }

        db.close();
        return sb.toString();
    }

    public JSONArray getLatestChargeLog(int queryLimit) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String limit = queryLimit > 0 ? String.valueOf(queryLimit) : null;
        String[] columns = {
                BatteryLocalDbHelper.CHARGE_LOG_UID,
                BatteryLocalDbHelper.CHARGE_LOG_LAST_PLUG,
                BatteryLocalDbHelper.CHARGE_LOG_PLUG,
                BatteryLocalDbHelper.CHARGE_LOG_POWER,
                BatteryLocalDbHelper.CHARGE_LOG_TIME
        };

        Cursor cursor = db.query(BatteryLocalDbHelper.CHARGE_LOG_TABLE, columns,
                null,
                null, null, null, BatteryLocalDbHelper.CHARGE_LOG_TIME + " DESC", limit);

        //int indexUID = cursor.getColumnIndex(BatteryLocalDbHelper.CHARGE_LOG_UID);
        //cursor.getInt(cursor.getColumnIndex(BatteryLocalDbHelper.CHARGE_LOG_BEGIN_POWER));

        JSONArray ja = new JSONArray();
        try {
            while (cursor.moveToNext()) {
                // get column value
                long id = cursor.getLong(0);
                int lastPlug = cursor.getInt(1);
                int plug = cursor.getInt(2);
                int power = cursor.getInt(3);
                long time = cursor.getLong(4);

                // create json data
                JSONObject jo = new JSONObject();
                jo.put("lid", id);
                jo.put("lastPlug", lastPlug);
                jo.put("plug", plug);
                jo.put("power", power);
                jo.put("time", time);

                ja.put(jo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        db.close();
        return ja;
    }

    static class BatteryLocalDbHelper extends SQLiteOpenHelper {

        private static final String TAG = "BatteryLocalDbHelper";

        private static final String DATABASE_NAME = "batteryDebug.db";

        private static final String CHARGE_LOG_TABLE = "charge_log_debug";
        private static final String CHARGE_LOG_UID = "id";
        private static final String CHARGE_LOG_LAST_PLUG = "last_plug";
        private static final String CHARGE_LOG_PLUG = "plug";
        private static final String CHARGE_LOG_POWER = "power";
        private static final String CHARGE_LOG_TIME = "time";

        private static final int DATABASE_VERSION = 1;

        public BatteryLocalDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                Log.d(TAG, "onCreate called");
                String charge_log_table = " (" +
                        CHARGE_LOG_UID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        CHARGE_LOG_LAST_PLUG + " INTEGER, " +
                        CHARGE_LOG_PLUG + " INTEGER, " +
                        CHARGE_LOG_POWER + " INTEGER, " +
                        CHARGE_LOG_TIME + " INTEGER);";

                db.execSQL("CREATE TABLE " + CHARGE_LOG_TABLE + charge_log_table);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            try {
                db.execSQL("DROP TABLE IF EXISTS " + CHARGE_LOG_TABLE + ";");
                onCreate(db);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
