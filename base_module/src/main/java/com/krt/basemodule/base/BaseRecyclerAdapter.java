package com.krt.basemodule.base;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * BaseRecyclerAdapter
 */

public abstract class BaseRecyclerAdapter<T, L extends List<T>> extends RecyclerView.Adapter<ViewHolder> {
    protected final L mData;
    private boolean mClickable = false;
    private boolean mAscend = true;

    public BaseRecyclerAdapter(){
        mData = getList();
    }

    public BaseRecyclerAdapter(L data){
        this();
        setData(data);
    }

    protected abstract L getList();

    public void setData(L data){
        clearData();
        addData(data);
    }

    public void addData(L data){
        if(null == data || data.size() <= 0)
            return;
        mData.addAll(data);
    }

    public void addData(T oneData){
        if(null != oneData){
            mData.add(oneData);
        }
    }

    public void clearData(){
        mData.clear();
    }

    public L getData(){
        return mData;
    }

    public int getItemCount() {
        return mData.size();
    }

    public T getDataAt(int position){
        if(position >= 0 && position < mData.size()){
            return getItemData(position);
        }
        return null;
    }

    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewHolder holder;
        View view = manualCreateView(parent.getContext(), parent, viewType);
        if(null == view){
            holder = ViewHolder.createViewHolder(parent, getLayoutId(viewType));
        }else{
            holder = ViewHolder.createViewHolder(view);
        }
        if(isClickable()) {
            setClickListener(holder);
        }
        return holder;
    }

    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        onBindData(holder, position, getItemData(position));
    }

    public int getItemViewType(int position) {
        return getViewType(position, getItemData(position));
    }

    protected abstract void onBindData(ViewHolder holder, int position, T data);

    protected abstract @LayoutRes
    int getLayoutId(int viewType);

    protected View manualCreateView(Context context, ViewGroup parent, int viewType){
        return null;
    }

    protected int getViewType(int position, T data){
        return 0;
    }

    protected T getItemData(int position){
        return mData.get(mAscend ? position : mData.size() - position - 1);
    }

    protected void onItemClick(int position, T data, ViewHolder viewHolder){}

    protected boolean onItemLongClick(int position, T data, ViewHolder viewHolder){return false;}

    public void setClickable(boolean clickable){
        mClickable = clickable;
    }

    public boolean isClickable(){
        return mClickable;
    }

    public void setAscend(boolean ascend){
        mAscend = ascend;
    }

    private void setClickListener(final ViewHolder viewHolder){
        viewHolder.getRootView().setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(isClickable()){
                    int position = viewHolder.getAdapterPosition();
                    if(position >= 0 && position < getItemCount()) {
                        onItemClick(position, getItemData(position), viewHolder);
                    }
                }
            }
        });
        viewHolder.getRootView().setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View v) {
                if(isClickable()){
                    int position = viewHolder.getAdapterPosition();
                    if(position >= 0 && position < getItemCount()) {
                        return onItemLongClick(position, getItemData(position), viewHolder);
                    }
                }
                return false;
            }
        });
    }

}
