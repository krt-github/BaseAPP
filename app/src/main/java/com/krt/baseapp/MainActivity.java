package com.krt.baseapp;

import android.os.Bundle;

import com.krt.basemodule.base.BaseActivity;

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init(){
        findViewById(R.id.hello).setOnClickListener(v -> {
            toast("Hello Android");
        });
    }

}
