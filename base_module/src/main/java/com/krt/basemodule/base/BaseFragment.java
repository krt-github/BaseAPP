package com.krt.basemodule.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.krt.basemodule.debug.Debug;
import com.krt.basemodule.utils.ToastUtils;

/**
 * @author KRT
 * 2018/11/20
 */
public abstract class BaseFragment extends Fragment {
    private final String TAG = this.getClass().getSimpleName();

    public Context getContext() {
        return getActivity();
    }

    public void toast(int stringId) {
        ToastUtils.show(stringId);
    }

    public void toast(CharSequence text) {
        ToastUtils.show(text);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getLayoutId(), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
    }

    protected <T extends View> T findViewById(@IdRes int id) {
        View root = getView();
        if (null != root) {
            return root.findViewById(id);
        }
        return null;
    }

    public abstract @LayoutRes int getLayoutId();

    public abstract void initView(View root);

    protected void printi(String s) {
        Debug.i(TAG, wrapperPrintString(s));
    }

    protected void printe(String s) {
        Debug.e(TAG, wrapperPrintString(s));
    }

    private String wrapperPrintString(String s) {
        return "---[" + s + "]---";
    }

    protected int dp2px(int dp){
        return (int)(getResources().getDisplayMetrics().density * dp + 0.5f);
    }
}
