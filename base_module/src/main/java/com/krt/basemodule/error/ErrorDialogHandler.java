package com.krt.basemodule.error;

import android.app.AlertDialog;
import android.content.Context;

/**
 * @author KRT
 * 2018/11/22
 */
public class ErrorDialogHandler implements IErrorHandler {
    private AlertDialog alertDialog;

    public ErrorDialogHandler(Context context){
        alertDialog = new AlertDialog.Builder(context)
                .setTitle("提示")
                .setMessage("")
                .setPositiveButton("知道了", (dialog, which) -> {})
                .create();
    }

    @Override
    public int getErrorHandlerType() {
        return ERROR_DIALOG_HANDLER;
    }

    @Override
    public void onNetError(CharSequence msg) {
//        alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "去设置",
//                (dialog, which) -> {
//                    Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    alertDialog.getContext().startActivity(intent);
//                });
        onShow(msg);
    }

    @Override
    public void onServerError(CharSequence msg) {
        onShow(msg);
    }

    @Override
    public void onDataError(CharSequence msg) {
        onShow(msg);
    }

    @Override
    public void onErrorRemove() {
        alertDialog.dismiss();
    }

    private void onShow(CharSequence msg){
        alertDialog.setMessage(msg);
        if(!alertDialog.isShowing()) {
            alertDialog.show();
        }
    }

    @Override
    public void onDestroy() {
        alertDialog.dismiss();
        alertDialog = null;
    }
}
