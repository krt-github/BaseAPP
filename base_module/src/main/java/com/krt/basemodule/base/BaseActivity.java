package com.krt.basemodule.base;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;

/**
 * @author KRT
 * 2018/11/20
 */
public class BaseActivity extends AppCompatActivity {

    public Context getContext(){
        return getApplicationContext();
    }

}
