package com.krt.basemodule.base;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.krt.base.R;

public abstract class BaseMainActivity extends BaseActivity {
    private final SparseArray<BaseFragment> mFragmentList = new SparseArray<>();

    protected abstract int getTabsLayoutId();

    protected abstract BaseFragment createTabFragment(@IdRes int tabViewId);

    protected abstract @IdRes int getDefaultTabId();

    protected abstract @IdRes int getRadioGroupId();

    protected abstract void initOther(@NonNull ViewGroup tabPanel);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_activity_main_layout);

        init();
    }

    private void init(){
        // initTabPanel
        ViewGroup tabsPanel = findViewById(R.id.tabs_panel);
        inflateTabViews(tabsPanel);
        RadioGroup radioGroup = tabsPanel.findViewById(getRadioGroupId());
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                onTabChanged(checkedId);
            }
        });
        radioGroup.check(getDefaultTabId());

        initOther(tabsPanel);
    }

    private void inflateTabViews(ViewGroup viewGroup) {
        LayoutInflater.from(getContext()).inflate(getTabsLayoutId(), viewGroup, true);
    }

    private void onTabChanged(int tabId) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        hideAllFragments(transaction);
        showSpecifyFragment(transaction, tabId);
        transaction.commitNow();
    }

    private void hideAllFragments(FragmentTransaction transaction) {
        Fragment fragment;
        for (int i = 0; i < mFragmentList.size(); i++) {
            fragment = mFragmentList.valueAt(i);
            if (null != fragment && !fragment.isHidden()) {
                transaction.hide(fragment);
            }
        }
    }

    private void showSpecifyFragment(FragmentTransaction transaction, int tabViewId) {
        BaseFragment fragment = mFragmentList.get(tabViewId);
        if (null == fragment) {
            fragment = createTabFragment(tabViewId);
            if (null != fragment) {
                mFragmentList.put(tabViewId, fragment);
                transaction.add(R.id.fragment_container, fragment);
            }
        } else {
            transaction.show(fragment);
        }
    }

}
