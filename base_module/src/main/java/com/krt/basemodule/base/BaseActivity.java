package com.krt.basemodule.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.krt.basemodule.utils.ToastUtils;
import com.krt.basemodule.debug.Debug;
import com.krt.basemodule.error.ErrorToastHandler;
import com.krt.basemodule.error.ErrorType;
import com.krt.basemodule.error.IErrorHandler;
import com.krt.basemodule.utils.ImageUtils;
import com.krt.basemodule.utils.PermissionUtils;
import com.krt.basemodule.view.LoadingDialog;

/**
 * @author KRT
 * 2018/11/19
 */
@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();
    private LoadingDialog mLoadingDialog;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initNavigationBar();
        initScreenOrientation();
        initErrorHandler();
    }

    protected void initNavigationBar() {
        showBottomUIMenu();
    }

    protected void initScreenOrientation() {
        setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    protected void setOrientation(int orientation) {
        setRequestedOrientation(orientation);
    }

    public Context getContext() {
        return getApplicationContext();
    }

    public void toast(int stringId) {
        ToastUtils.show(stringId);
    }

    public void toast(CharSequence text) {
        ToastUtils.show(text);
    }

    public boolean isLoadingShow() {
        return null != mLoadingDialog && mLoadingDialog.isShowing();
    }

    public void showLoading() {
        showLoading(null);
    }

    public void showLoading(int resId) {
        showLoading(resId, true);
    }

    public void showLoading(int resId, boolean cancelable) {
        showLoading(resId, cancelable, -1);
    }

    public void showLoading(int resId, boolean cancelable, int callId) {
        showLoading(getString(resId), cancelable, callId);
    }

    public void showLoading(CharSequence msg) {
        showLoading(msg, true);
    }

    public void showLoading(CharSequence msg, boolean cancelable) {
        showLoading(msg, cancelable, -1);
    }

    public void showLoading(CharSequence msg, boolean cancelable, int callId) {
        if (null == mLoadingDialog) {
            mLoadingDialog = new LoadingDialog(this);
            mLoadingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    onLoadingDismiss(dialog, mLoadingDialog.getCallId());
                }
            });
        }
        mLoadingDialog.setMessage(msg);
        mLoadingDialog.setCallId(callId);
        mLoadingDialog.setCancelable(cancelable);
        mLoadingDialog.setCanceledOnTouchOutside(false);
        mLoadingDialog.show();
    }

    public void dismissLoading() {
        if (null != mLoadingDialog) {
            mLoadingDialog.dismiss();
            mLoadingDialog.resetCallId();
        }
    }

    protected void onLoadingDismiss(DialogInterface dialog, int callId) {
    }

    public void setStatusBarColor(int color) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(color);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void setNavigationBarColor(int color) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setNavigationBarColor(color);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ViewGroup contentRoot;

    protected ViewGroup getRootView() {
        if (null == contentRoot) {
            contentRoot = findViewById(android.R.id.content);
        }
        return contentRoot;
    }

    public void runDelay(Runnable runnable, int ms) {
        View rootView = getRootView();
        if (null != rootView) {
            rootView.postDelayed(runnable, ms);
        }
    }

    public void removeRunnable(Runnable runnable) {
        if (null != contentRoot) {
            contentRoot.removeCallbacks(runnable);
        }
    }

//    public void loadGif(SketchView sketchImageView, String url){
//        ImageUtils.loadGif(getContext(), sketchImageView, url);
//    }

    public void loadImage(ImageView imageView, String url) {
        ImageUtils.loadImage(imageView, url);
    }

    public void loadImage(ImageView imageView, String url, @DrawableRes int defaultRes) {
        ImageUtils.loadImage(imageView, url, defaultRes);
    }

    public void loadRadiusImage(ImageView imageView, String url, @DrawableRes int defaultRes, int radius) {
        ImageUtils.loadRadiusImage(imageView, url, defaultRes, radius);
    }

    public void loadCircleImage(ImageView imageView, String url) {
        ImageUtils.loadCircleImage(imageView, url);
    }

    public void loadImageAutoSize(ImageView imageView, String url,
                                  int maxWidth, int maxHeight, @DrawableRes int defaultRes) {
        ImageUtils.loadImageAutoSize(imageView, url, maxWidth, maxHeight, defaultRes);
    }

    protected void requestPermission(String[] permissions, PermissionUtils.IPermissionResult callback) {
        PermissionUtils.request(this, permissions, callback);
    }

    protected void requestPermissionEach(String[] permissions, PermissionUtils.IPermissionResultEach callback) {
        PermissionUtils.requestEach(this, permissions, callback);
    }

    protected int getColorCompat(@ColorRes int id) {
        return getResources().getColor(id);
    }

    protected void printi(String s) {
        Debug.i(TAG, wrapperPrintString(s));
    }

    protected void printe(String s) {
        Debug.e(TAG, wrapperPrintString(s));
    }

    protected int dp2px(int dp) {
        return (int) (getResources().getDisplayMetrics().density * dp + 0.5f);
    }

    private String wrapperPrintString(String s) {
        return "---[" + s + "]---";
    }

    private final SparseArray<IErrorHandler> mErrorHandlerMap = new SparseArray<>();
    private int mDefaultErrorHandlerType = 0;

    private void initErrorHandler() {
        IErrorHandler defaultErrorHandler = getDefaultErrorHandler();
        mDefaultErrorHandlerType = defaultErrorHandler.getErrorHandlerType();
        addErrorHandler(defaultErrorHandler);
    }

    protected final void addErrorHandler(IErrorHandler errorHandler) {
        mErrorHandlerMap.put(errorHandler.getErrorHandlerType(), errorHandler);
    }

    protected IErrorHandler getDefaultErrorHandler() {
        return new ErrorToastHandler();
    }

    public void onNetError(CharSequence msg) {
        errorHandler(ErrorType.NET_ERROR, msg);
    }

    public void onServerError(CharSequence msg) {
        errorHandler(ErrorType.SERVER_ERROR, msg);
    }

    public void onDataError(CharSequence msg) {
        errorHandler(ErrorType.DATA_ERROR, msg);
    }

    public void resetErrorState() {
        for (int i = 0; i < mErrorHandlerMap.size(); i++) {
            mErrorHandlerMap.valueAt(i).onErrorRemove();
        }
    }

    private void errorHandler(@ErrorType.ErrorTypeDef int errorType, CharSequence msg) {
        errorHandler(mDefaultErrorHandlerType, errorType, msg);
    }

    private void errorHandler(@IErrorHandler.ErrorHandlerDef int errorHandlerType,
                              @ErrorType.ErrorTypeDef int errorType, CharSequence msg) {
        IErrorHandler errorHandler = mErrorHandlerMap.get(errorHandlerType);
        if (null != errorHandler) {
            switch (errorType) {
                case ErrorType.NET_ERROR:
                    errorHandler.onNetError(msg);
                    break;
                case ErrorType.SERVER_ERROR:
                    errorHandler.onServerError(msg);
                    break;
                case ErrorType.DATA_ERROR:
                    errorHandler.onDataError(msg);
                    break;
            }
        }
    }

    protected void showBottomUIMenu() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    protected void hideBottomUIMenu() {
        View decorView = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 19) { // lower api
            decorView.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            // | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    public void hideImm(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void showKeyboard(boolean show) {
        Object service = getSystemService(Context.INPUT_METHOD_SERVICE);
        if (service instanceof InputMethodManager) {
            InputMethodManager imm = (InputMethodManager) service;
            View currentFocus = getCurrentFocus();
            if (show) {
                if (null == currentFocus) {//无焦点打开
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                } else {//有焦点打开
                    imm.showSoftInput(currentFocus, 0);
                }
            } else {
                if (null == currentFocus) {//无焦点关闭
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                } else {//有焦点关闭
                    imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
                }
            }
        }
    }

    /**
     * 设置Window 透明
     */
    public static void setTransparentForWindow(Activity activity) {
        Window window = activity.getWindow();
        window.setStatusBarColor(Color.TRANSPARENT);
        View decorView = window.getDecorView();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flags = decorView.getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            decorView.setSystemUiVisibility(flags);
        } else {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

    @Override
    protected void onDestroy() {
        dismissLoading();
        for (int i = 0; i < mErrorHandlerMap.size(); i++) {
            mErrorHandlerMap.valueAt(i).onDestroy();
        }
        mErrorHandlerMap.clear();
        super.onDestroy();
    }

    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isHideKeyBoardEvent()) {
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                View v = getCurrentFocus();
                if (isShouldHideKeyboard(v, ev)) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS
                    );
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    // Return whether touch the view.
    private boolean isShouldHideKeyboard(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],
                    top = l[1],
                    bottom = top + v.getHeight(),
                    right = left + v.getWidth();
            return !(event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom);
        }
        return false;
    }

    protected boolean isHideKeyBoardEvent() {
        return true;
    }
}
