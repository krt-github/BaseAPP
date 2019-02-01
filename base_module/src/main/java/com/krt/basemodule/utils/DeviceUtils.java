package com.krt.basemodule.utils;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.krt.basemodule.bean.DeviceInfo;
import com.krt.basemodule.debug.Debug;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.UUID;

/**
 * DeviceUtils
 */

public class DeviceUtils {

    public static String[] getUsePermissions(){
        return new String[]{
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.INTERNET
        };
    }

    public static DeviceInfo getDeviceInfo(Context context){
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setIP(getIP(context));
        deviceInfo.setMAC(getMAC(context));
        deviceInfo.setIMEI(getIMEI(context));
        deviceInfo.setUUID(getDeviceUUId(context));
        return deviceInfo;
    }

    public static String getIP(Context context){
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (Exception ex) {
            Debug.e("WifiPreference IpAddress", ex.toString());
        }
        return "IP";
    }

    public static String getIMEI(Context context){
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String imei = telephonyManager.getDeviceId();
            return null == imei ? "IMEI" : imei;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "IMEI";
    }

    public static String getMAC(Context context){
        try {
            return "mac";
        }catch(Exception e){
            e.printStackTrace();
        }
        return "MAC";
    }

    public static String getDeviceUUId(Context context){
        try {
            SharedPreferences preferences = context.getSharedPreferences("spForBaseModule", Context.MODE_PRIVATE);
            String deviceUUID = preferences.getString("deviceUUID", null);
            if(null == deviceUUID){
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                String deviceId;
                try{
                    deviceId = telephonyManager.getDeviceId();
                }catch(Exception e){
                    e.printStackTrace();
                    deviceId = "" + Math.random();
                }
                String simSerialNumber;
                try{
                    simSerialNumber = telephonyManager.getSimSerialNumber();
                }catch(Exception e){
                    e.printStackTrace();
                    simSerialNumber = "" + Math.random();
                }
                String androidId;
                try{
                    androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                }catch (Exception e){
                    e.printStackTrace();
                    androidId = "" + Math.random();
                }
                long time = System.nanoTime();
                deviceId = null != deviceId ? deviceId : time + "";
                simSerialNumber = null != simSerialNumber ? simSerialNumber : (time * 2) + "";
                androidId = null != androidId ? androidId : (time * 3) + "";
                deviceUUID = new UUID((long)Math.pow(androidId.hashCode(), 2),
                        ((long)simSerialNumber.hashCode()) << 32 | Math.abs(deviceId.hashCode())).toString();
                deviceUUID = deviceUUID.replaceAll("-", ""); //极光推送 alias 不支持 -
                preferences.edit().putString("deviceUUID", deviceUUID).apply();
            }
            return deviceUUID;
        }catch(Exception e){
            e.printStackTrace();
        }
        return "unknown ID";
    }
}
