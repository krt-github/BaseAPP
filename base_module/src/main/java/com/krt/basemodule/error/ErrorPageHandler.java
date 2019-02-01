package com.krt.basemodule.error;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yichat.base.R;

/**
 * @author KRT
 * 2018/11/22
 */
public class ErrorPageHandler implements IErrorHandler {
    private View mErrorView;
    private ViewGroup mAttachRoot;
    private TextView mErrorTip;
    private View mRetry;

    public ErrorPageHandler(Context context, ViewGroup root){
        mAttachRoot = root;
        mErrorView = LayoutInflater.from(context).inflate(R.layout.layout_error_page, root, false);
        mErrorView.setOnClickListener(v -> {});
        mErrorTip = mErrorView.findViewById(R.id.error_msg);
        mRetry = mErrorView.findViewById(R.id.retry_btn);
    }

    @Override
    public int getErrorHandlerType() {
        return ERROR_PAGE_HANDLER;
    }

    @Override
    public void onNetError(CharSequence msg) {
        onShow(R.mipmap.net_error, msg);
    }

    @Override
    public void onServerError(CharSequence msg) {
        onShow(R.mipmap.server_error, msg);
    }

    @Override
    public void onDataError(CharSequence msg) {
        onShow(R.mipmap.data_error, msg);
    }

    @Override
    public void onErrorRemove() {
        dismiss();
    }

    public void onShow(@DrawableRes int icon, CharSequence msg){
        if(null != mAttachRoot) {
            mErrorTip.setCompoundDrawablesWithIntrinsicBounds(0, icon, 0, 0);
            mErrorTip.setText(msg);
            mRetry.setEnabled(true);
            if(null == mErrorView.getParent()) {
                mAttachRoot.addView(mErrorView);
            }
        }
    }

    public void dismiss(){
        if(null != mAttachRoot){
            mAttachRoot.removeView(mErrorView);
        }
    }

    public void setBackgroundColor(@ColorInt int color){
        mErrorView.setBackgroundColor(color);
    }

    public void setOnRetryClickListener(View.OnClickListener listener){
        mRetry.setOnClickListener(v -> {
            v.setEnabled(false);
            listener.onClick(v);
        });
    }

    @Override
    public void onDestroy() {
        dismiss();
    }
}
