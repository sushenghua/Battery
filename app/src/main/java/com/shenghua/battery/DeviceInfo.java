package com.shenghua.battery;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

/**
 * Created by shenghua on 12/21/15.
 */
public class DeviceInfo {

    private static Location sLocation = null;
    private static LocationListener sLocationListener = null;

    public static JSONObject getDeviceInfo() {
        return null;
    }

    private static boolean supportSpeedyCharge() {
        return false;
    }

    private static String getCpuInfo() {
        String cpuInformation = "";
        //StringBuffer sb = new StringBuffer();
        //sb.append("abi: ").append(Build.CPU_ABI).append("\n");
        if (new File("/proc/cpuinfo").exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(new File("/proc/cpuinfo")));
                String aLine;
                while ((aLine = br.readLine()) != null) {
                    if (aLine.startsWith("Hardware")) {
                        cpuInformation = aLine.substring(aLine.indexOf(":") + 1).trim();
                        break;
                    }
                    //sb.append(aLine + "\n");
                }
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //return sb.toString();
        return cpuInformation;
    }

    private static String getManufacturerInfo() {
        return Build.MANUFACTURER;
    }

    private static String getModelInfo() {
        return Build.MODEL;
    }

    private static String getBuildInfo() {
        return Build.ID;
    }

    private static String getAndroidVersion() {
        String release = Build.VERSION.RELEASE;
        int sdk = Build.VERSION.SDK_INT;
        return release + "(" + sdk + ")";
    }

//    private static String getUUID() {
//        return
//    }

    public static String getMacAddress() {
        return getMacAddress("wlan0");
    }

    public static void test(Context context) {
        Log.d("--->Device Mac Address", DeviceInfo.getMacAddress());
        Location l = getLastBestLocation(context);
        if (l != null) {
            Log.d("--->Device Location", l.toString());
        }
        else {
            Log.d("--->Device Location", "null");
        }
    }

    private static String getMacAddress(String interfaceName) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (interfaceName != null) {
                    if (!intf.getName().equalsIgnoreCase(interfaceName)) continue;
                }
                byte[] mac = intf.getHardwareAddress();
                if (mac == null) return "";
                StringBuilder buf = new StringBuilder();
                for (int idx = 0; idx < mac.length; idx++)
                    buf.append(String.format("%02X:", mac[idx]));
                if (buf.length() > 0) buf.deleteCharAt(buf.length() - 1);
                return buf.toString();
            }
        } catch (Exception ex) {
        } // for now eat exceptions
        return "";
        /*try {
            // this is so Linux hack
            return loadFileAsString("/sys/class/net/" +interfaceName + "/address").toUpperCase().trim();
        } catch (IOException ex) {
            return null;
        }*/
    }

    public static void setLocation(Location location) {
        sLocation = location;
    }

    private static Location getLastBestLocation(Context context) {

        PackageManager pm = context.getPackageManager();
        int permission = pm.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,
                context.getPackageName());
        if (permission == PackageManager.PERMISSION_GRANTED) {
            Log.d("--->Location Service", "-----> permission granted");
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            Location locationGPS = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location locationNet = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            long gpsLocationTime = 0;
            if (null != locationGPS) gpsLocationTime = locationGPS.getTime();

            long netLocationTime = 0;
            if (null != locationNet) netLocationTime = locationNet.getTime();

            if ( netLocationTime < gpsLocationTime ) {
                return locationGPS;
            }
            else {
                return locationNet;
            }
        }
        return null;
    }

    public static void registerLocationListener(Context context, LocationListener listener) {
        PackageManager pm = context.getPackageManager();
        int permission = pm.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,
                context.getPackageName());
        if (permission == PackageManager.PERMISSION_GRANTED) {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            sLocationListener = listener;
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
        }
    }

    public static void unregisterLocationListener(Context context) {
        PackageManager pm = context.getPackageManager();
        int permission = pm.checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,
                context.getPackageName());
        if (permission == PackageManager.PERMISSION_GRANTED) {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (sLocationListener != null) {
                lm.removeUpdates(sLocationListener);
                sLocationListener = null;
            }
        }
    }
}
