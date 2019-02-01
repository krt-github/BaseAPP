package com.krt.basemodule.mvp;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.krt.basemodule.base.BaseFragment;


public abstract class BaseMVPFragment<T extends IBasePresenter> extends BaseFragment implements IBaseView{
    private T mPresenter;
    private BaseMVPActivity mMVPActivity;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = createPresenter();
    }

    @Override
    public void onDestroy() {
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

    private BaseMVPActivity getMVPActivity(){
        if(null == mMVPActivity){
            FragmentActivity activity = getActivity();
            if(activity instanceof BaseMVPActivity){
                mMVPActivity = (BaseMVPActivity) activity;
            }else{
                throw new IllegalArgumentException("MVPFragment must attach MVPActivity !!!");
            }
        }
        return mMVPActivity;
    }

    public void clearErrorState() {
        getMVPActivity().resetErrorState();
    }

    public void onUpdateViewFinally() {
        getMVPActivity().dismissLoading();
    }

    public void onHandle(CharSequence tip) {
        getMVPActivity().showLoading(tip);
    }

    public void onCancel(CharSequence tip) {
        getMVPActivity().dismissLoading();
        getMVPActivity().onServerError(tip);
    }
}
