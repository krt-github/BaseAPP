package com.krt.basemodule.view;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.krt.base.R;
import com.krt.basemodule.debug.Debug;
import com.krt.basemodule.utils.PermissionUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class FloatWindow {
    public static final int DRAG_STYLE_SIMPLE_DRAG = 1;
    public static final int DRAG_STYLE_LONG_PRESS = 2;
    public static final int DRAG_STYLE_DOUBLE_CLICK = 3;

    public static final int RESIZE_WITH_TOP_LEFT = 0x01;
    public static final int RESIZE_WITH_TOP_RIGHT = 0x02;
    public static final int RESIZE_WITH_BOTTOM_LEFT = 0x04;
    public static final int RESIZE_WITH_BOTTOM_RIGHT = 0x08;
    public static final int RESIZE_WITH_ALL = 0x0F;

    @IntDef(value = {
            DRAG_STYLE_SIMPLE_DRAG,
            DRAG_STYLE_LONG_PRESS,
            DRAG_STYLE_DOUBLE_CLICK
    })
    @Retention(value = RetentionPolicy.SOURCE)
    public @interface DragStyle{}

    private final int TOUCH_SLOP;
    private final float DENSITY;
    private int mSettleMargin;
    private boolean mEnableAutoSettle = true;

    private View mFloatView;
    private final RootViewGroup mContentRootView;
    private final Point mSize = new Point();
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private OnFloatWindowVisibilityChangeListener mOnFloatWindowVisibilityChangeListener;

    public interface OnFloatWindowVisibilityChangeListener{
        /**
         * @return True if the listener has consumed the event, false otherwise.
         */
        boolean onFloatWindowVisibilityChange(View floatView, boolean visible);
    }

    public void setOnFloatWindowVisibilityChangeListener(OnFloatWindowVisibilityChangeListener l){
        mOnFloatWindowVisibilityChangeListener = l;
    }

    public static boolean hasFloatPermission(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        }
        return true;
    }

    public static void requestFloatPermission(Activity activity, int requestCode){
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + activity.getPackageName()));
                activity.startActivityForResult(intent, requestCode);
            }
        }catch(Exception e){
            e.printStackTrace();
            PermissionUtils.jump2Setting(activity.getApplicationContext());
        }
    }

    public static void requestFloatPermission(Context context){
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + context.getPackageName()));
                context.startActivity(intent);
            }
        }catch(Exception e){
            e.printStackTrace();
            PermissionUtils.jump2Setting(context);
        }
    }

    public FloatWindow(Context context){
        DENSITY = context.getResources().getDisplayMetrics().density;
        TOUCH_SLOP = ViewConfiguration.get(context).getScaledTouchSlop();
        mSettleMargin = TOUCH_SLOP;

        mContentRootView = new RootViewGroup(context);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display defaultDisplay = mWindowManager.getDefaultDisplay();
        defaultDisplay.getSize(mSize);
        int size = dp2px(30);
        mContentRootView.setAnchorSize(size, size);

        initWindowLayout();
    }

    private int dp2px(int dp) {
        return (int) (DENSITY * dp + 0.5f);
    }

    private void initWindowLayout(){
        mLayoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        mLayoutParams.format = PixelFormat.RGBA_8888;
        mLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
    }

    private void fadeInOutFloatWindow(boolean visible){
        if(null == mFloatView) {
            return;
        }

        mFloatView.animate().alpha(visible ? 1f : 0f).setDuration(300)
                .withStartAction(() -> {
                    if(visible){
                        mFloatView.setVisibility(View.VISIBLE);
                    }
                })
                .withEndAction(() -> {
                    if(!visible) {
                        mFloatView.setVisibility(View.INVISIBLE);
                    }
            }).start();
    }

    public void setFloatWindowVisibility(boolean visible){
        if(null == mOnFloatWindowVisibilityChangeListener
                || !mOnFloatWindowVisibilityChangeListener.onFloatWindowVisibilityChange(mFloatView, visible)){
            fadeInOutFloatWindow(visible);
        }
    }

    public void setEnableAutoSettle(boolean canAutoSettle){
        mEnableAutoSettle = canAutoSettle;
    }

    public void setSettleMargin(int margin){
        mSettleMargin = margin;
    }

    public void setEnableDrag(boolean canDrag){
        mContentRootView.setEnableDrag(canDrag);
    }

    public void setResizeMode(int mode){
        mContentRootView.setResizeMode(mode);
    }

    public void setDragStyle(@DragStyle int dragStyle){
        mContentRootView.setDragStyle(dragStyle);
    }

    public void setEnableResize(boolean canResize){
        mContentRootView.setEnableResize(canResize);
    }

    public void showResizePanel(){
        mContentRootView.setEnableResize(true);
    }

    public void dismissResizePanel(){
        mContentRootView.setEnableResize(false);
    }

    public void setResizeDraggerColor(@ColorInt int color){
        mContentRootView.getAnchorPaint().setColor(color);
    }

    public void setResizeAnchorSize(int widthDP, int heightDP){
        mContentRootView.setAnchorSize(dp2px(widthDP), dp2px(heightDP));
    }

    public void setResizeFrameStrokeWidth(int widthDP){
        mContentRootView.getAnchorPaint().setStrokeWidth(dp2px(widthDP));
    }

    public void setResizeAnchorResources(@DrawableRes int resIds){
        mContentRootView.setAnchorBitmaps(resIds);
    }

    /**
     * Resize anchor drawable
     * @param resIds top_left, top_right, bottom_right, bottom_left
     */
    public void setResizeAnchorResources(@DrawableRes int ... resIds){
        mContentRootView.setAnchorBitmaps(resIds);
    }

    @RequiresPermission(value = "android.permission.SYSTEM_ALERT_WINDOW")
    public void showFloatView(View floatView, int x, int y){
        showFloatView(floatView, x, y,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @RequiresPermission(value = "android.permission.SYSTEM_ALERT_WINDOW")
    public void showFloatView(View floatView, int x, int y, int w, int h){
        if(null == floatView){
            throw new IllegalArgumentException("Float view is NULL !!!");
        }

        addView(wrapperCustomerView(floatView, w, h), x, y, w, h);
    }

    @RequiresPermission(value = "android.permission.SYSTEM_ALERT_WINDOW")
    public void showFloatViewHandlePermission(View floatView, int x, int y){
        showFloatViewHandlePermission(floatView, x, y,
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @RequiresPermission(value = "android.permission.SYSTEM_ALERT_WINDOW")
    public void showFloatViewHandlePermission(View floatView, int x, int y, int w, int h){
        if(null == floatView){
            throw new IllegalArgumentException("Float view is NULL !!!");
        }

        if(hasFloatPermission(floatView.getContext())){
            showFloatView(floatView, x, y, w, h);
        }else{
            requestFloatPermission(floatView.getContext());
        }
    }

    public void closeFloatWindow(){
        if(null != mFloatView) {
            mWindowManager.removeView(mFloatView);
        }
    }

    public View getContentView(){
        return mContentRootView.getChildAt(0);
    }

    private void addView(View floatView, int x, int y, int w, int h){
        mFloatView = floatView;
        mLayoutParams.x = x;
        mLayoutParams.y = y;
        mLayoutParams.width = w;
        mLayoutParams.height = h;
        mLayoutParams.gravity = Gravity.START | Gravity.TOP;
        mWindowManager.addView(mFloatView, mLayoutParams);
    }

    /**
     * 拖拽的方式改变 window 的大小时，window 会有很严重的抖动，暂没有好的解决办法
     *         现采用如下方式：
     *         1.改变大小时(action down)，将 window 最大化
     *         2.重定位 content view 的 x, y，使用 window 最大化前的 x, y，使之保持在原来的位置
     *         3.拖动完毕(action up)，修改 window 的 x, y, width, height 为 content view 的值
     *         4.归零 content view 的 x, y
     *         5.重定位、归零 content view 时，会有一帧抖动，采用先隐藏再淡出的方式过渡
     * @param customerView customerView
     * @param w w
     * @param h h
     * @return view
     */
    private View wrapperCustomerView(View customerView, int w, int h){
        mContentRootView.addView(customerView);

        FrameLayout forResize = new FrameLayout(customerView.getContext());
        forResize.addView(mContentRootView, new FrameLayout.LayoutParams(w, h));

        return forResize;
    }

    private ValueAnimator mSettleAnimator;
    private void autoSettle(){
        int startH, endH, startV, endV;
        if((mLayoutParams.x + (mFloatView.getWidth() / 2)) > (mSize.x / 2)){
            startH = mLayoutParams.x;
            endH = mSize.x - mFloatView.getWidth() - mSettleMargin;
        }else{
            startH = mLayoutParams.x;
            endH = mSettleMargin;
        }

        if(mLayoutParams.y + (mFloatView.getHeight() / 2) > (mSize.y / 2)){
            startV = mLayoutParams.y;
            endV = mSize.y - mFloatView.getHeight() - mSettleMargin;
        }else{
            startV = mLayoutParams.y;
            endV= mSettleMargin;
        }

        boolean isHorizontalSettle = Math.abs(startH - endH) < Math.abs(startV - endV);
        int start = isHorizontalSettle ? startH : startV;
        int end = isHorizontalSettle ? endH : endV;

        mSettleAnimator = ValueAnimator.ofInt(start, end).setDuration(300);
        mSettleAnimator.setInterpolator(new DecelerateInterpolator());
        mSettleAnimator.addUpdateListener(animation -> {
            int pos = (int) animation.getAnimatedValue();
            if (isHorizontalSettle) {
                mLayoutParams.x = pos;
            } else {
                mLayoutParams.y = pos;
            }
            updateWindow();
        });
        mSettleAnimator.start();
    }

    private void updateWindow(){
        if(null != mFloatView) {
            mWindowManager.updateViewLayout(mFloatView, mLayoutParams);
        }
    }

    public interface OnLongPressListener{
        void onLongPress();
    }

    public void setOnLongPressListener(OnLongPressListener l){
        mContentRootView.setOnLongPressListener(l);
    }

    private class RootViewGroup extends FrameLayout{
        private boolean isDrag = false;
        private boolean enableDrag = true;
        private int dragStyle = DRAG_STYLE_SIMPLE_DRAG;
        private float downX, downY;
        private float lastX, lastY;

        private int currentResizeMode = 0;
        private boolean isLongPress = false;
        private CheckLongPressRunnable checkLongPressRunnable;
        private static final int LONG_PRESS_THRESHOLD = 500; //ms

        private boolean enableResize = false;
        private int resizeMode = RESIZE_WITH_ALL;
        private final Paint anchorPaint = new Paint();
        private Bitmap[] anchorBitmaps;
        private int anchorTouchWidth;
        private int anchorTouchHeight;
        private int anchorShowWidth;
        private int anchorShowHeight;
        private int minWidth;
        private int minHeight;
        private final Rect newSizeAfterDrag = new Rect();

        public RootViewGroup(@NonNull Context context) {
            super(context);
            anchorPaint.setColor(getResources().getColor(R.color.colorPrimary));
            anchorPaint.setStrokeWidth(dp2px(10));

            anchorTouchWidth = dp2px(30);
            anchorTouchHeight = anchorTouchWidth;
            anchorShowWidth = anchorTouchWidth / 2;
            anchorShowHeight = anchorTouchWidth / 2;

            initMinSize();
        }

        private void initMinSize(){
            minWidth = anchorShowWidth;
            minHeight = anchorShowHeight;
        }

        public void setAnchorSize(int showWidth, int showHeight) {
            anchorShowWidth = showWidth;
            anchorShowHeight = showHeight;
            initMinSize();
        }

        public void setResizeMode(int mode){
            resizeMode = mode;
        }

        public Paint getAnchorPaint(){
            return anchorPaint;
        }

        private View getContentRootView(){
            return this;
        }

        public void setEnableResize(boolean enable){
            enableResize = enable;
            setWillNotDraw(!enable);
        }

        public void setEnableDrag(boolean canDrag){
            enableDrag = canDrag;
        }

        public void setDragStyle(@DragStyle int dragStyle){
            dragStyle = dragStyle;
        }

        private Bitmap decodeBitmap(@DrawableRes int resId){
            if(0 == resId){
                return null;
            }

            Drawable drawable = getResources().getDrawable(resId);
            if(null == drawable){
                return null;
            }

            final int intrinsicWidth = drawable.getIntrinsicWidth();
            final int intrinsicHeight = drawable.getIntrinsicHeight();

            Bitmap bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, intrinsicWidth, intrinsicHeight);
            drawable.draw(canvas);
            return bitmap;
        }

        public void setAnchorBitmaps(@DrawableRes int bitmapId){
            Bitmap[] bitmaps = new Bitmap[4];
            Bitmap bitmap = decodeBitmap(bitmapId);
            if(null != bitmap){
                bitmaps[0] = bitmap;
                Matrix matrix = new Matrix();
                for(int i = 1; i < bitmaps.length; i++){
                    matrix.setRotate(i * 90);
                    bitmaps[i] = Bitmap.createBitmap(bitmap, 0, 0,
                            bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                }
            }
            setAnchorBitmaps(bitmaps);
        }

        public void setAnchorBitmaps(@DrawableRes int ... bitmapIds){
            Bitmap[] bitmaps = new Bitmap[4];
            if(null != bitmapIds){
                Matrix matrix = new Matrix();
                Bitmap bitmap;
                for(int i = 0; i < bitmapIds.length; i++){
                    bitmap = decodeBitmap(bitmapIds[i]);
                    if(null != bitmap) {
                        matrix.setRotate(i * 90);
                        bitmaps[i] = Bitmap.createBitmap(bitmap, 0, 0,
                                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    }
                }
            }
            setAnchorBitmaps(bitmaps);
        }

        public void setAnchorBitmaps(Bitmap... bitmaps){
            if(null != bitmaps){
                anchorBitmaps = new Bitmap[4];
                for(int i = 0; i < bitmaps.length; i++){
                    anchorBitmaps[i] = bitmaps[i];
                }
            }
        }

        private boolean isValidPosition(int position){
            return 0 != (resizeMode & position);
        }

        private void drawResizeAnchorBitmaps(final Canvas canvas, final Paint paint, final Bitmap[] bitmaps,
                                       final int canvasW, final int canvasH){
            Bitmap bitmap = bitmaps[0];
            if(null != bitmap && isValidPosition(RESIZE_WITH_TOP_LEFT)) {
                canvas.drawBitmap(bitmap, 0, 0, paint);
            }

            bitmap = bitmaps[1];
            if(null != bitmap && isValidPosition(RESIZE_WITH_TOP_RIGHT)) {
                canvas.drawBitmap(bitmap, canvasW - bitmap.getWidth(), 0, paint);
            }

            bitmap = bitmaps[3];
            if(null != bitmap && isValidPosition(RESIZE_WITH_BOTTOM_LEFT)) {
                canvas.drawBitmap(bitmap, 0, canvasH - bitmap.getHeight(), paint);
            }

            bitmap = bitmaps[2];
            if(null != bitmap && isValidPosition(RESIZE_WITH_BOTTOM_RIGHT)) {
                canvas.drawBitmap(bitmap, canvasW - bitmap.getWidth(), canvasH - bitmap.getHeight(), paint);
            }
        }

        private void drawResizeAnchors(final Canvas canvas, final Paint paint){
            final int canvasW = getContentRootView().getWidth();
            final int canvasH = getContentRootView().getHeight();

            if(null != anchorBitmaps){
                drawResizeAnchorBitmaps(canvas, paint, anchorBitmaps, canvasW, canvasH);
            }else{
                final int lineWidth = anchorShowWidth;
                final int lineHeight = anchorShowHeight;
                if(isValidPosition(RESIZE_WITH_TOP_LEFT)) {
                    canvas.drawLine(0, 0, lineWidth, 0, paint);
                    canvas.drawLine(0, 0, 0, lineHeight, paint);
                }

                if(isValidPosition(RESIZE_WITH_TOP_RIGHT)) {
                    canvas.drawLine(canvasW - lineWidth, 0, canvasW, 0, paint);
                    canvas.drawLine(canvasW, 0, canvasW, lineHeight, paint);
                }

                if(isValidPosition(RESIZE_WITH_BOTTOM_LEFT)) {
                    canvas.drawLine(0, canvasH, lineWidth, canvasH, paint);
                    canvas.drawLine(0, canvasH - lineHeight, 0, canvasH, paint);
                }

                if(isValidPosition(RESIZE_WITH_BOTTOM_RIGHT)) {
                    canvas.drawLine(canvasW - lineWidth, canvasH, canvasW, canvasH, paint);
                    canvas.drawLine(canvasW, canvasH - lineHeight, canvasW, canvasH, paint);
                }
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            if(enableResize){
                drawResizeAnchors(canvas, anchorPaint);
            }
        }

        private void reviseLayoutParams(final View floatView, final WindowManager.LayoutParams layoutParams,
                                        final Point size){
            if(layoutParams.x < 0){
                layoutParams.x = 0;
            }else if(layoutParams.x + floatView.getWidth() > size.x){
                layoutParams.x = size.x - floatView.getWidth();
            }

            if(layoutParams.y < 0){
                layoutParams.y = 0;
            }else if(layoutParams.y + floatView.getHeight() > size.y){
                layoutParams.y = size.y - floatView.getHeight();
            }
        }

        /**
         * 说明 {@link FloatWindow#wrapperCustomerView}
         * @param layoutParams
         */
        private void prepareForResize(final Rect size, final WindowManager.LayoutParams layoutParams){
            size.left = layoutParams.x;
            size.top = layoutParams.y;
            size.right = size.left + getContentRootView().getWidth();
            size.bottom = size.top + getContentRootView().getHeight();

            layoutParams.x = 0;
            layoutParams.y = 0;
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;

            getContentRootView().setX(size.left);
            getContentRootView().setY(size.top);

            // Evade twinkle
            getContentRootView().setAlpha(0);
            post(() -> {
                updateWindow();
                getContentRootView().animate().alpha(1).setDuration(230).start();
            });
        }

        private void calcSize(final Rect size, int mode, final float offsetX, final float offsetY){
            switch(mode){
                case RESIZE_WITH_TOP_LEFT:
                    size.left += offsetX;
                    size.top += offsetY;
                    break;
                case RESIZE_WITH_TOP_RIGHT:
                    size.right += offsetX;
                    size.top += offsetY;
                    break;
                case RESIZE_WITH_BOTTOM_LEFT:
                    size.left += offsetX;
                    size.bottom += offsetY;
                    break;
                case RESIZE_WITH_BOTTOM_RIGHT:
                    size.right += offsetX;
                    size.bottom += offsetY;
                    break;
            }
        }

        /**
         * 说明 {@link FloatWindow#wrapperCustomerView}
         * @param layoutParams
         */
        private void onResizeComplete(WindowManager.LayoutParams layoutParams){
            layoutParams.x = (int) getContentRootView().getX();
            layoutParams.y = (int) getContentRootView().getY();
            layoutParams.width = getContentRootView().getWidth();
            layoutParams.height = getContentRootView().getHeight();

            getContentRootView().setX(0);
            getContentRootView().setY(0);

            // Evade twinkle
            getContentRootView().setAlpha(0);
            post(() -> {
                updateWindow();
                getContentRootView().animate().alpha(1).setDuration(230).start();
            });
        }

        private void applyNewSize(Rect newSize, final int maxX, final int maxY){
            final int minW = minWidth;
            final int minH = minHeight;
            final int maxLeft = maxX - minW;
            final int maxTop = maxY - minH;
            Rect rect = new Rect(newSize);

            if(rect.left < 0){
                rect.left = 0;
            }else if(rect.left > maxLeft){
                rect.left = maxLeft;
            }

            if(rect.top < 0){
                rect.top = 0;
            }else if(rect.top > maxTop){
                rect.top = maxTop;
            }

            if(rect.right > maxX){
                rect.right = maxX;
            }else if(rect.right < rect.left + minW){
                rect.right = rect.left + minW;
            }

            if(rect.bottom > maxY){
                rect.bottom = maxY;
            }else if(rect.bottom < rect.top + minH){
                rect.bottom = rect.top + minH;
            }

            final View contentRootView = getContentRootView();
            ViewGroup.LayoutParams layoutParams = contentRootView.getLayoutParams();
            layoutParams.width = rect.width();
            layoutParams.height = rect.height();

            contentRootView.setX(rect.left);
            contentRootView.setY(rect.top);
            contentRootView.setLayoutParams(layoutParams);
        }

        private int judgeResizeMode(final float downX, final float downY){
            int position = judgeResizePosition(downX, downY);
            if(isValidPosition(position)){
                return position;
            }
            return 0;
        }

        private Bitmap getAnchorBitmap(int pos){
            if(null != anchorBitmaps && pos >= 0 && pos < anchorBitmaps.length){
                return anchorBitmaps[pos];
            }
            return null;
        }

        private int judgeResizePosition(final float downX, final float downY){
            final int width = getContentRootView().getWidth();
            final int height = getContentRootView().getHeight();

            int touchSizeW = anchorTouchWidth;
            int touchSizeH = anchorTouchHeight;

            Bitmap bitmap = getAnchorBitmap(0);
            if (null != bitmap) {
                touchSizeW = bitmap.getWidth();
                touchSizeH = bitmap.getHeight();
            }
            if(downX >= 0 && downX < touchSizeW && downY >= 0 && downY < touchSizeH){
                return RESIZE_WITH_TOP_LEFT;
            }

            bitmap = getAnchorBitmap(1);
            if (null != bitmap) {
                touchSizeW = bitmap.getWidth();
                touchSizeH = bitmap.getHeight();
            }
            if(downX > width - touchSizeW && downX <= width && downY >= 0 && downY < touchSizeH){
                return RESIZE_WITH_TOP_RIGHT;
            }

            bitmap = getAnchorBitmap(3);
            if (null != bitmap) {
                touchSizeW = bitmap.getWidth();
                touchSizeH = bitmap.getHeight();
            }
            if(downX >= 0 && downX < touchSizeW && downY > height - touchSizeH && downY <= height){
                return RESIZE_WITH_BOTTOM_LEFT;
            }

            bitmap = getAnchorBitmap(2);
            if (null != bitmap) {
                touchSizeW = bitmap.getWidth();
                touchSizeH = bitmap.getHeight();
            }
            if(downX > width - touchSizeW && downX <= width && downY > height - touchSizeH && downY <= height){
                return RESIZE_WITH_BOTTOM_RIGHT;
            }
            return 0;
        }

        private boolean isHandlingResize(){
            return 0 != currentResizeMode;
        }

        private boolean handleResizeTouchEvent(MotionEvent event){
            boolean isHandlingResize;
            switch(event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    downX = event.getRawX();
                    downY = event.getRawY();
                    lastX = downX;
                    lastY = downY;

                    currentResizeMode = judgeResizeMode(event.getX(), event.getY());
                    isHandlingResize = isHandlingResize();
                    if(isHandlingResize) {
                        prepareForResize(newSizeAfterDrag, mLayoutParams);
                    }
                    return isHandlingResize;

                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    isHandlingResize = isHandlingResize();
                    if(isHandlingResize){
                        onResizeComplete(mLayoutParams);
                    }
                    currentResizeMode = 0;
                    return isHandlingResize;

                case MotionEvent.ACTION_MOVE:
                    isHandlingResize = isHandlingResize();
                    if(isHandlingResize){
                        float tempX = event.getRawX();
                        float tempY = event.getRawY();
                        calcSize(newSizeAfterDrag, currentResizeMode, tempX - lastX, tempY - lastY);
                        applyNewSize(newSizeAfterDrag, mSize.x, mSize.y);
                        lastX = tempX;
                        lastY = tempY;
                    }
                    return isHandlingResize;
            }
            return false;
        }

        private void judgeLongPress(){
            if(null == checkLongPressRunnable){
                checkLongPressRunnable = new CheckLongPressRunnable();
            }
            postDelayed(checkLongPressRunnable, LONG_PRESS_THRESHOLD);
        }

        private void removeCheckLongPressTask(){
            removeCallbacks(checkLongPressRunnable);
        }

        @Override
        public boolean dispatchTouchEvent(MotionEvent event) {
            if(enableResize && handleResizeTouchEvent(event)){
                return true;
            }

            if(!enableDrag){
                return super.dispatchTouchEvent(event);
            }

            switch(dragStyle){
                case DRAG_STYLE_SIMPLE_DRAG:
                    if(handleTouchEventInSimpleDragStyle(event)){
                        return true;
                    }
                    break;
                case DRAG_STYLE_LONG_PRESS:
                    if(handleTouchEventInLongPressStyle(event)){
                        return true;
                    }
                    break;
                case DRAG_STYLE_DOUBLE_CLICK:
                    if(handleTouchEventInDoubleClickStyle(event)){
                        return true;
                    }
                    break;
            }
            super.dispatchTouchEvent(event);
            return true;
        }

        private boolean handleActionDown(MotionEvent event){
            if(null != mSettleAnimator){
                mSettleAnimator.cancel();
            }
            isDrag = false;
            downX = event.getRawX();
            downY = event.getRawY();
            lastX = downX;
            lastY = downY;
            return false;
        }

        private boolean handleActionUp(MotionEvent event){
            if(isDrag){
                if(mEnableAutoSettle){
                    autoSettle();
                }
                return true;
            }else {
                return false;
            }
        }

        private boolean handleActionMove(MotionEvent event){
            if(!enableDrag){
                return false;
            }

            final float tempX = event.getRawX();
            final float tempY = event.getRawY();
            final float offsetX = (int)(tempX - lastX);
            final float offsetY = (int)(tempY - lastY);
            lastX = tempX;
            lastY = tempY;

            if(!isDrag && isMoveOverThreshold(lastX, lastY)){
                isDrag = true;
            }

            if(isDrag){
                mLayoutParams.x += offsetX;
                mLayoutParams.y += offsetY;
                reviseLayoutParams(mFloatView, mLayoutParams, mSize);
                updateWindow();
            }

            return true;
        }

        private boolean isMoveOverThreshold(final float curX, final float curY){
            return Math.abs(curX - downX) >= TOUCH_SLOP || Math.abs(curY - downY) >= TOUCH_SLOP;
        }

        private boolean handleTouchEventInSimpleDragStyle(MotionEvent event){
            switch(event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    if(handleActionDown(event)){
                        return true;
                    }
                    break;

                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    if(handleActionUp(event)){
                        return true;
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    if(handleActionMove(event)){
                        return true;
                    }
                    break;

            }
            return false;
        }

        private boolean handleTouchEventInLongPressStyle(MotionEvent event){
            switch(event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    isLongPress = false;
                    judgeLongPress();
                    if(handleActionDown(event)){
                        return true;
                    }
                    break;

                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    removeCheckLongPressTask();
                    if(handleActionUp(event)){
                        return true;
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    if(!isDrag && isMoveOverThreshold(event.getRawX(), event.getRawY())){
                        isDrag = true;
                        removeCheckLongPressTask();
                    }

                    if(isLongPress){
                        return handleActionMove(event);
                    }
                    break;
            }
            return false;
        }

        private boolean handleTouchEventInDoubleClickStyle(MotionEvent event){
            return super.dispatchTouchEvent(event);
        }

        private OnLongPressListener onLongPressListener;
        public void setOnLongPressListener(OnLongPressListener l){
            onLongPressListener = l;
        }

        private class CheckLongPressRunnable implements Runnable{

            public void run() {
                isLongPress = true;
                if(null != onLongPressListener){
                    onLongPressListener.onLongPress();
                }
            }
        }

    }

    private void print(String s){
        Debug.e("FloatWindow", s);
    }

}
