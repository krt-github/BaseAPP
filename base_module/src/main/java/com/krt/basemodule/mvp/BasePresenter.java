package com.krt.basemodule.mvp;

import com.krt.basemodule.debug.Debug;

/**
 * @author KRT
 * 2018/12/5
 */
public abstract class BasePresenter<M extends IBaseModel, V extends IBaseView> implements IBasePresenter {
    private final String TAG = this.getClass().getSimpleName();
    private M mModel;
    private V mView;

    public BasePresenter(M model, V view){
        mModel = model;
        mView = view;
    }

    public void onDestroy() {
        if(null != mModel){
            mModel.onDestroy();
        }
        mModel = null;
        mView = null;
    }

    protected M getModel(){
        return mModel;
    }

    protected V getView(){
        return mView;
    }

    protected void printi(String msg){
        Debug.i(TAG, msg);
    }

    protected void printe(String msg){
        Debug.e(TAG, msg);
    }
}
