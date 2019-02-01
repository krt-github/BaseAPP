package com.krt.basemodule.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.yichat.base.R;

/**
 * LoadingDialog
 */

public class LoadingDialog extends Dialog {
    /**
     * 调用者可设置一个调用标识，用于 dismiss 时告知关闭的是哪一个调用<br>
     * 便于在 onDismiss 中处理不同的业务
     */
    private int mCallId = -1;
    private View mLoadingViewRoot;
    private TextView mMessage;
    private ProgressWheel mProgress;

    public LoadingDialog(@NonNull Context context) {
        this(context, R.style.fullScreenDialogStyle);
    }

    public LoadingDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        init(context);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mLoadingViewRoot);
    }

    private void init(Context context){
        mLoadingViewRoot = LayoutInflater.from(context).inflate(R.layout.layout_loading_dialog, null);
        mMessage = (TextView) mLoadingViewRoot.findViewById(R.id.loading_message);
        mProgress = (ProgressWheel) mLoadingViewRoot.findViewById(R.id.loading_progress);
    }

    public LoadingDialog setMessage(CharSequence msg){
        mMessage.setText(msg);
        return this;
    }

    public void resetCallId(){
        setCallId(-1);
    }

    public void setCallId(int callId){
        mCallId = callId;
    }

    public int getCallId(){
        return mCallId;
    }

    public LoadingDialog setImage(){
        return this;
    }

    public void show() {
        if(!mProgress.isSpinning()) {
            mProgress.spin();
        }
        super.show();
    }

    @Override
    public void dismiss() {
        if(mProgress.isSpinning()) {
            mProgress.stopSpinning();
        }
        super.dismiss();
    }
}
