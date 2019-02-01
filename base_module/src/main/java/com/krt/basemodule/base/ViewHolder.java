package com.krt.basemodule.base;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * ViewHolder
 */

public class ViewHolder extends RecyclerView.ViewHolder {
    private final SparseArray<View> mViewArray = new SparseArray<>();
    private View mRootView;

    private ViewHolder(View itemView) {
        super(itemView);
        mRootView = itemView;
    }

    public static ViewHolder createViewHolder(View itemView){
        return new ViewHolder(itemView);
    }

    public static ViewHolder createViewHolder(ViewGroup parent, int layoutId){
        View itemView = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new ViewHolder(itemView);
    }

    public <T extends View> T getView(@IdRes int viewId){
        View view = mViewArray.get(viewId);
        if(null == view){
            view = mRootView.findViewById(viewId);
            mViewArray.put(viewId, view);
        }
        return (T) view;
    }

    public ViewHolder setText(@IdRes int viewId, CharSequence text){
        TextView textView = getView(viewId);
        textView.setText(text);
        return this;
    }

    public ViewHolder setText(@IdRes int viewId, @StringRes int stringId){
        TextView textView = getView(viewId);
        textView.setText(stringId);
        return this;
    }

    public TextView getTextView(@IdRes int viewId){
        TextView textView = (TextView) mViewArray.get(viewId);
        if(null == textView){
            textView = mRootView.findViewById(viewId);
            mViewArray.put(viewId, textView);
        }
        return textView;
    }

    public ImageView getImageView(@IdRes int viewId){
        ImageView imageView = (ImageView) mViewArray.get(viewId);
        if(null == imageView){
            imageView = mRootView.findViewById(viewId);
            mViewArray.put(viewId, imageView);
        }
        return imageView;
    }

    public View getRootView(){
        return mRootView;
    }

    public Context getContext(){
        return mRootView.getContext();
    }
}
