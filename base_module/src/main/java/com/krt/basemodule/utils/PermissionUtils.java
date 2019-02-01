package com.krt.basemodule.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;

import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.yichat.base.R;

/**
 * @author KRT
 * 2018/11/20
 */
public class PermissionUtils {

    public interface IPermissionResult{
        void onResult(boolean granted);
    }

    public interface IPermissionResultEach{
        void onGranted(String permission);
        void onDenied(String permission);
        void onDeniedWithNeverAsk(String permission);
    }

    private PermissionUtils() {}

    public static void request(@NonNull FragmentActivity activity, String[] permissions, final IPermissionResult callback) {
        //TODO clean warning
        new RxPermissions(activity).request(permissions)
                .subscribe(granted -> onResult(granted, callback));
    }

    public static void request(@NonNull Fragment fragment, String[] permissions, final IPermissionResult callback){
        //TODO clean warning
        new RxPermissions(fragment).request(permissions)
                .subscribe(granted -> onResult(granted, callback));
    }

    public static void requestEach(@NonNull FragmentActivity activity, String[] permissions, final IPermissionResultEach callback) {
        //TODO clean warning
        new RxPermissions(activity).requestEach(permissions)
                .subscribe(permission -> onResultEach(permission, callback));
    }

    public static void requestEach(@NonNull Fragment fragment, String[] permissions, final IPermissionResultEach callback){
        //TODO clean warning
        new RxPermissions(fragment).requestEach(permissions)
                .subscribe(permission -> onResultEach(permission, callback));
    }

    private static void onResult(boolean granted, IPermissionResult callback){
        if(null == callback)
            return;

        callback.onResult(granted);
    }

    private static void onResultEach(Permission permission, IPermissionResultEach callback){
        if(null == callback)
            return;

        if(permission.granted){
            callback.onGranted(permission.name);
        }else if(permission.shouldShowRequestPermissionRationale){
            callback.onDenied(permission.name);
        }else{
            callback.onDeniedWithNeverAsk(permission.name);
        }
    }

    public static Intent getAppDetailsSettingIntent(Context context){
        Intent intent;
        intent = handleThirdPartyManufacturer(context);
        if(null == intent){
            intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", context.getPackageName(), null));
        }
        return intent;
    }

    public static void tipPermissionDenied(Activity activity, String permissionString){
        new AlertDialog.Builder(activity)
                .setTitle(R.string.tip)
                .setMessage(activity.getResources()
                        .getString(R.string.tip_permission_denied, permissionString, permissionString))
                .setPositiveButton(R.string.confirm, (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    public static void jump2Setting(Context context){
        context.startActivity(getAppDetailsSettingIntent(context));
    }

    private static Intent handleThirdPartyManufacturer(Context context){
        Intent intent;
        intent = handleVivoIntent(context);
        if(null != intent)
            return intent;

        intent = handleOPPOIntent(context);
        if(null != intent)
            return intent;
        return null;
    }

    private static Intent handleVivoIntent(Context context){
        // 点击设置图标>加速白名单>我的app
        // 点击软件管理>软件管理权限>软件>我的app>信任该软件
        return context.getPackageManager().getLaunchIntentForPackage("com.iqoo.secure");
    }

    private static Intent handleOPPOIntent(Context context){
        // 点击设置图标>应用权限管理>按应用程序管理>我的app>我信任该应用
        // 点击权限隐私>自启动管理>我的app
        return context.getPackageManager().getLaunchIntentForPackage("com.oppo.safe");
    }

}
