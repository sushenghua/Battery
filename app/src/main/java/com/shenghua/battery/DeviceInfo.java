package com.shenghua.battery;

import android.content.Context;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by shenghua on 12/21/15.
 */
public class DeviceInfo {

    private static final int DEVICE_INFO_DATA_TYPE = 1;

    public static final int DEVICE_INFO_NOTHING = 0;
    public static final int DEVICE_BASE_INFO = 1;
    public static final int DEVICE_LOCATION = 2;
    public static final int DEVICE_FULL_INFO = 3;

    public static final String DEVICE_INFO_FILTER_JSON_KEY = "filter";

    private static LocationTracker locationTracker = null;
    private static String geoInfo = "";

    private static String macAddress = null;
    private static boolean macAddressHashedEver = false;
    private static int macAddressHash = 0;

    public static JSONObject getDeviceInfo(Context context, int filter) {

        if (filter == DEVICE_INFO_NOTHING)
            return null;

        JSONObject jo = new JSONObject();
        try {
            int resultFilter = DEVICE_INFO_NOTHING;

            String macAddress = getMacAddress();
            if (macAddress == null)
                macAddress = getMacAddress(context);
            jo.put("type", DEVICE_INFO_DATA_TYPE);
            jo.put("did", getMacAddressHash());

            if ( (filter & DEVICE_BASE_INFO) == DEVICE_BASE_INFO ) {
                jo.put("cpu", getCpuInfo());
                jo.put("manuf", getManufacturerInfo());
                jo.put("model", getModelInfo());
                jo.put("build", getBuildInfo());
                jo.put("mac", macAddress);
                jo.put("sptsc", supportSpeedyCharge(context.getResources()));
                jo.put("android", getAndroidVersion());

                resultFilter |= DEVICE_BASE_INFO;
            }

            if ( (filter & DEVICE_LOCATION) == DEVICE_LOCATION ) {
                Location location = getLocation();
                if (location != null) {
                    jo.put("lat", ""+location.getLatitude());
                    jo.put("lon", ""+location.getLongitude());
                    jo.put("geo", getGeoInfo());
                    resultFilter |= DEVICE_LOCATION;
                }
            }

            jo.put(DEVICE_INFO_FILTER_JSON_KEY, resultFilter);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jo;
    }

    public static void init(Context context) {
        if (locationTracker == null) {
            locationTracker = new LocationTracker(context);
        }
        updateGeoInfo(context, getLocation());
    }

    public static boolean supportSpeedyCharge(Resources res) {

        String[] chipList = res.getStringArray(R.array.support_speedy_charge_chip_list);
        boolean chipSupported = false;
        String thisDeviceChipModel = getCpuInfo().toLowerCase();
        for (String chipModel : chipList) {
            if (thisDeviceChipModel.contains(chipModel.toLowerCase())) {
                chipSupported = true;
                break;
            }
        }

        if (chipSupported) {
            String[] brandList = res.getStringArray(R.array.support_speedy_charge_brand_list);
            for (String brand : brandList) {
                String brandLowerCase = brand.toLowerCase();
                if (getModelInfo().contains(brandLowerCase)
                        || getManufacturerInfo().contains(brandLowerCase)
                        || getBuildInfo().contains(brandLowerCase))
                    return true;
            }
        }

        return false;
    }

    public static int getMacAddressHash() {
        if (!macAddressHashedEver) {
            if (getMacAddress() != null) {
                macAddressHash = hashMacAddress(getMacAddress());
                macAddressHashedEver = true;
            }
        }
        return macAddressHash;
    }

    private static String getGeoInfo() {
        return geoInfo;
    }

    private static void updateGeoInfo(Context context, Location location) {
        if (location != null) {
            Geocoder geoCoder = new Geocoder(context, Locale.getDefault());
            if (geoCoder.isPresent()) {
                try {
                    List<Address> addresses = geoCoder.getFromLocation( location.getLatitude(),
                                                                        location.getLongitude(), 1);
                    if (addresses.size() > 0) {
                        geoInfo = addresses.get(0).getLocality() + "," + addresses.get(0).getCountryName();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static int hashMacAddress(String macAddress) {
        //String[] parts = macAddress.split(":");
        String hexString = macAddress.replaceAll(":", "");
        long tmp = Long.parseLong(hexString, 16);
        return (int)(tmp ^ (tmp >>> 32));
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

    private static Location getLocation() {
        return locationTracker.getLocation();
    }

    private static String getMacAddress(Context context) {
        if (macAddress == null) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            macAddress = wifiManager.getConnectionInfo().getMacAddress();
        }
        return macAddress;
    }

    private static String getMacAddress() {
        if (macAddress == null) {
            macAddress = getMacAddress("wlan0");
        }
        return macAddress;
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
        return null;
        /*try {
            // this is so Linux hack
            return loadFileAsString("/sys/class/net/" +interfaceName + "/address").toUpperCase().trim();
        } catch (IOException ex) {
            return null;
        }*/
    }
}
