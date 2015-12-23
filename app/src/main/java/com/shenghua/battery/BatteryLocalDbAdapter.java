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

    public long insertLog(int beginPower, long beginTime,
                          int endPower, long endTime,
                          long plugoutTime, long chargeDuration, boolean uploaded) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(BatteryLocalDbHelper.CHARGE_LOG_BEGIN_POWER, new Integer(beginPower));
        contentValues.put(BatteryLocalDbHelper.CHARGE_LOG_BEGIN_TIME, new Long(beginTime));
        contentValues.put(BatteryLocalDbHelper.CHARGE_LOG_END_POWER, new Integer(endPower));
        contentValues.put(BatteryLocalDbHelper.CHARGE_LOG_END_TIME, new Long(endTime));
        contentValues.put(BatteryLocalDbHelper.CHARGE_LOG_PLUGOUT_TIME, new Long(plugoutTime));
        contentValues.put(BatteryLocalDbHelper.CHARGE_LOG_CHARGE_DURATION, new Long(chargeDuration));
        contentValues.put(BatteryLocalDbHelper.CHARGE_LOG_UPLOADED, new Boolean(uploaded));

        long id = db.insert(BatteryLocalDbHelper.CHARGE_LOG_TABLE, null, contentValues);
        db.close();

        return id;
    }

    public String getAll() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] columns = {BatteryLocalDbHelper.CHARGE_LOG_UID,
                            BatteryLocalDbHelper.CHARGE_LOG_BEGIN_POWER,
                            BatteryLocalDbHelper.CHARGE_LOG_BEGIN_TIME,
                            BatteryLocalDbHelper.CHARGE_LOG_END_POWER,
                            BatteryLocalDbHelper.CHARGE_LOG_END_TIME,
                            BatteryLocalDbHelper.CHARGE_LOG_UPLOADED};
        Cursor cursor = db.query(BatteryLocalDbHelper.CHARGE_LOG_TABLE, columns,
                null, null, null, null, null);

        //int indexUID = cursor.getColumnIndex(BatteryLocalDbHelper.CHARGE_LOG_UID);
        //cursor.getInt(cursor.getColumnIndex(BatteryLocalDbHelper.CHARGE_LOG_BEGIN_POWER));

        StringBuffer sb = new StringBuffer();
        while (cursor.moveToNext()) {
            long id = cursor.getLong(0);
            int beginPower = cursor.getInt(1);
            long beginTime = cursor.getLong(2);
            boolean uploaded = cursor.getInt(5) != 0;
            sb.append(id + ", " + beginPower + ", " + beginTime + ", " + uploaded + "\n");
        }

        db.close();
        return sb.toString();
    }

    public void markChargeLogAsUploaded(JSONArray logs) {
        markChargeLogUploadedState(logs, true);
    }

    public void markChargeLogUploadedState(JSONArray logs, boolean uploaded) {

        SQLiteDatabase db  = dbHelper.getWritableDatabase();
        try {
            for (int i = 0; i < logs.length(); ++i) {
                JSONObject jo = logs.getJSONObject(i);
                ContentValues contentValues = new ContentValues();
                contentValues.put(BatteryLocalDbHelper.CHARGE_LOG_UPLOADED, new Boolean(uploaded));
                String[] whereArgs = {String.valueOf(jo.getLong("lid"))};
                db.update(BatteryLocalDbHelper.CHARGE_LOG_TABLE, contentValues,
                          BatteryLocalDbHelper.CHARGE_LOG_UID + " =? ", whereArgs);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        db.close();
    }

    public JSONArray getChargeLog(boolean pickUploaded) {
        return getChargeLog(pickUploaded, 0);
    }

    public JSONArray getChargeLog(boolean pickUploaded, int queryLimit) {

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String uploadedFilter = pickUploaded ? "1" : "0";
        String limit = queryLimit > 0 ? String.valueOf(queryLimit) : null;
        String[] columns = {BatteryLocalDbHelper.CHARGE_LOG_UID,
                            BatteryLocalDbHelper.CHARGE_LOG_BEGIN_POWER,
                            BatteryLocalDbHelper.CHARGE_LOG_BEGIN_TIME,
                            BatteryLocalDbHelper.CHARGE_LOG_END_POWER,
                            BatteryLocalDbHelper.CHARGE_LOG_END_TIME,
                            BatteryLocalDbHelper.CHARGE_LOG_PLUGOUT_TIME,
                            BatteryLocalDbHelper.CHARGE_LOG_CHARGE_DURATION,
                            BatteryLocalDbHelper.CHARGE_LOG_UPLOADED};

        Cursor cursor = db.query(BatteryLocalDbHelper.CHARGE_LOG_TABLE, columns,
                BatteryLocalDbHelper.CHARGE_LOG_UPLOADED + " = '" + uploadedFilter + "'",
                null, null, null, null, limit);

        //int indexUID = cursor.getColumnIndex(BatteryLocalDbHelper.CHARGE_LOG_UID);
        //cursor.getInt(cursor.getColumnIndex(BatteryLocalDbHelper.CHARGE_LOG_BEGIN_POWER));

        JSONArray ja = new JSONArray();
        try {
            while (cursor.moveToNext()) {
                // get column value
                long id = cursor.getLong(0);
                int beginPower = cursor.getInt(1);
                long beginTime = cursor.getLong(2);
                int endPower = cursor.getInt(3);
                long endTime = cursor.getLong(4);
                long plugoutTime = cursor.getLong(5);
                long chargeDuration = cursor.getLong(6);
                //boolean uploaded = cursor.getInt(7) != 0;

                // create json data
                JSONObject jo = new JSONObject();
                jo.put("lid", id);
                jo.put("bp", beginPower);
                jo.put("bt", beginTime);
                jo.put("ep", endPower);
                jo.put("et", endTime);
                jo.put("ot", plugoutTime);
                jo.put("cd", chargeDuration);

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

        private static final String DATABASE_NAME = "battery.db";

        private static final String CHARGE_LOG_TABLE = "charge_log";
        private static final String CHARGE_LOG_UID = "id";
        private static final String CHARGE_LOG_BEGIN_POWER = "begin_power";
        private static final String CHARGE_LOG_BEGIN_TIME = "begin_time";
        private static final String CHARGE_LOG_END_POWER = "end_power";
        private static final String CHARGE_LOG_END_TIME = "end_time";
        private static final String CHARGE_LOG_PLUGOUT_TIME = "plugout_time";
        private static final String CHARGE_LOG_CHARGE_DURATION = "charge_duration";
        private static final String CHARGE_LOG_UPLOADED = "uploaded";

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
                        CHARGE_LOG_BEGIN_POWER + " INTEGER, " +
                        CHARGE_LOG_BEGIN_TIME + " INTEGER, " +
                        CHARGE_LOG_END_POWER + " INTEGER, " +
                        CHARGE_LOG_END_TIME + " INTEGER, " +
                        CHARGE_LOG_PLUGOUT_TIME + " INTEGER, " +
                        CHARGE_LOG_CHARGE_DURATION + " INTEGER, " +
                        CHARGE_LOG_UPLOADED + " BOOLEAN);";

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
