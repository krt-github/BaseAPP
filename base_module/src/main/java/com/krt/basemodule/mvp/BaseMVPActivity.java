package com.krt.basemodule.mvp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.krt.basemodule.base.BaseActivity;


/**
 * @author KRT
 * 2018/11/29
 */
public abstract class BaseMVPActivity<T extends IBasePresenter> extends BaseActivity implements IBaseView {
    private T mPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = createPresenter();
    }

    @Override
    protected void onDestroy() {
        if(null != mPresenter){
            mPresenter.onDestroy();
        }
        mPresenter = null;

        super.onDestroy();
    }

    protected abstract T createPresenter();

    protected T getPresenter(){
        return mPresenter;
    }

    public Context getActivity(){
        return this;
    }

    public void clearErrorState() {
        resetErrorState();
    }

    public void onUpdateViewFinally() {
        dismissLoading();
    }

    public void onHandle(CharSequence tip) {
        showLoading(tip);
    }

    public void onCancel(CharSequence tip) {
        dismissLoading();
        onServerError(tip);
    }

}
