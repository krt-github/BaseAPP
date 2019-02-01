package com.krt.basemodule.base;

import java.util.ArrayList;

/**
 * BaseRecyclerAdapter
 */

public abstract class BaseArrayListRecyclerAdapter<T> extends BaseRecyclerAdapter<T, ArrayList<T>> {

    @Override
    protected ArrayList<T> getList() {
        return new ArrayList<>();
    }

}
