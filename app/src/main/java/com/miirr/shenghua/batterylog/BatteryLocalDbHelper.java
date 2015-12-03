package com.miirr.shenghua.batterylog;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by shenghua on 12/3/15.
 */
public class BatteryLocalDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "battery.db";
    private static final String PLUG_OPERATION_TABLE = "plug_operation";

    private static final String CHARGE_LOG_TABLE = "charge_log";
    private static final String CHARGE_LOG_UID = "id";
    private static final String CHARGE_LOG_BEGIN_POWER = "begin_power";
    private static final String CHARGE_LOG_BEGIN_TIME = "begin_time";
    private static final String CHARGE_LOG_END_POWER = "end_power";
    private static final String CHARGE_LOG_END_TIME = "end_time";
    private static final String CHARGE_LOG_PLUGOUT_TIME = "plugout_time";
    private static final String CHARGE_LOG_CHARGE_DURATION = "charge_duration";

    private static final String CHARGE_LOG_TO_UPLOAD_TABLE = "charge_log_upload";
    private static final int DATABASE_VERSION = 1;

    public BatteryLocalDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
//            db.execSQL("CREATE TABLE PLUG_OPERATION ()");
            String charge_log_table = " (" +
                CHARGE_LOG_UID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                CHARGE_LOG_BEGIN_POWER + " INTEGER, " +
                CHARGE_LOG_BEGIN_TIME + " INTEGER, " +
                CHARGE_LOG_END_POWER + " INTEGER, " +
                CHARGE_LOG_END_TIME + " INTEGER, " +
                CHARGE_LOG_PLUGOUT_TIME + " INTEGER, " +
                CHARGE_LOG_CHARGE_DURATION + " INTEGER);";

            db.execSQL("CREATE TABLE " + CHARGE_LOG_TABLE + charge_log_table);
            db.execSQL("CREATE TABLE " + CHARGE_LOG_TO_UPLOAD_TABLE + charge_log_table);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE " + CHARGE_LOG_TABLE + " IF EXISTS;");
            onCreate(db);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
