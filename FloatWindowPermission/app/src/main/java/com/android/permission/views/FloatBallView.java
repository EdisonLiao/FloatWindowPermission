/*
 * Copyright (C) 2016 Facishare Technology Co., Ltd. All Rights Reserved.
 */
package com.android.permission.views;

import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

import com.android.floatwindowpermission.R;
import com.android.permission.DensityUtils;

/**
 * Description:
 *
 * @author zhaozp
 * @since 2016-05-19
 */
public class FloatBallView extends FrameLayout{
    private static final String TAG = "AVCallFloatView";

    /**
     * 记录手指按下时在小悬浮窗的View上的横坐标的值
     */
    private float xInView;

    /**
     * 记录手指按下时在小悬浮窗的View上的纵坐标的值
     */
    private float yInView;
    /**
     * 记录当前手指位置在屏幕上的横坐标值
     */
    private float xInScreen;

    /**
     * 记录当前手指位置在屏幕上的纵坐标值
     */
    private float yInScreen;

    /**
     * 记录手指按下时在屏幕上的横坐标的值
     */
    private float xDownInScreen;

    /**
     * 记录手指按下时在屏幕上的纵坐标的值
     */
    private float yDownInScreen;

    private boolean isAnchoring = false;
    private boolean isShowing = false;
    private WindowManager windowManager = null;
    private WindowManager.LayoutParams mParams = null;
    private FloatBallListener mListener;

    public interface FloatBallListener{
        void onFloatBallMoving(int x,int y);
        void onFloatBallStopMoving(int x,int y);
        void onFloatBallClicked();
    }

    public FloatBallView(Context context, FloatBallListener listener) {
        super(context);
        mListener = listener;
        initView();
    }

    private void initView() {
        windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View floatView = inflater.inflate(R.layout.float_window_layout, null);
        addView(floatView);

    }

    public void setParams(WindowManager.LayoutParams params) {
        mParams = params;
    }

    public void setIsShowing(boolean isShowing) {
        this.isShowing = isShowing;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isAnchoring) {
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xInView = event.getX();
                yInView = event.getY();
                xDownInScreen = event.getRawX();
                yDownInScreen = event.getRawY();
                xInScreen = event.getRawX();
                yInScreen = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                xInScreen = event.getRawX();
                yInScreen = event.getRawY();
                boolean realMoving = Math.abs(xInScreen - xDownInScreen) >= ViewConfiguration.get(getContext()).getScaledTouchSlop()
                        && Math.abs(yInScreen - yDownInScreen) >= ViewConfiguration.get(getContext()).getScaledTouchSlop();

                if (mListener != null && realMoving){
                    // 手指移动的时候更新小悬浮窗的位置
                    updateViewPosition();
                    mListener.onFloatBallMoving(mParams.x,mParams.y);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (Math.abs(xDownInScreen - xInScreen) <= ViewConfiguration.get(getContext()).getScaledTouchSlop()
                        && Math.abs(yDownInScreen - yInScreen) <= ViewConfiguration.get(getContext()).getScaledTouchSlop()) {
                    // 点击效果
                    if (mListener != null){
                        mListener.onFloatBallClicked();
                    }

                } else {
                    //吸附效果
                    anchorToSide();
                    if (mListener != null){
                        mListener.onFloatBallStopMoving(mParams.x,mParams.y);
                    }
                }
                break;
            default:
                break;
        }
        return true;
    }

    private void anchorToSide() {
        isAnchoring = true;
        Point size = new Point();
        windowManager.getDefaultDisplay().getSize(size);
        int screenWidth = size.x;
        int screenHeight = size.y;
        int middleX = mParams.x + getWidth() / 2;


        int animTime = 0;
        int xDistance = 0;
        int yDistance = 0;

        int dp_25 = DensityUtils.dp2px(getContext(),15);

        //1
        if (middleX <= dp_25 + getWidth() / 2) {
            xDistance = dp_25 - mParams.x;
        }
        //2
        else if (middleX <= screenWidth / 2) {
            xDistance = dp_25 - mParams.x;
        }
        //3
        else if (middleX >= screenWidth - getWidth() / 2 - dp_25) {
            xDistance = screenWidth - mParams.x - getWidth() - dp_25;
        }
        //4
        else {
            xDistance = screenWidth - mParams.x - getWidth() - dp_25;
        }

        //1
        if (mParams.y < dp_25) {
            yDistance = dp_25 - mParams.y;
        }
        //2
        else if (mParams.y + getHeight() + dp_25 >= screenHeight) {
            yDistance = screenHeight - dp_25 - mParams.y - getHeight();
        }
        Log.e(TAG, "xDistance  " + xDistance + "   yDistance" + yDistance);

        animTime = Math.abs(xDistance) > Math.abs(yDistance) ? (int) (((float) xDistance / (float) screenWidth) * 600f)
                : (int) (((float) yDistance / (float) screenHeight) * 900f);
        this.post(new AnchorAnimRunnable(Math.abs(animTime), xDistance, yDistance, System.currentTimeMillis()));
    }

    private class AnchorAnimRunnable implements Runnable {

        private int animTime;
        private long currentStartTime;
        private Interpolator interpolator;
        private int xDistance;
        private int yDistance;
        private int startX;
        private int startY;

        public AnchorAnimRunnable(int animTime, int xDistance, int yDistance, long currentStartTime) {
            this.animTime = animTime;
            this.currentStartTime = currentStartTime;
            interpolator = new AccelerateDecelerateInterpolator();
            this.xDistance = xDistance;
            this.yDistance = yDistance;
            startX = mParams.x;
            startY = mParams.y;
        }

        @Override
        public void run() {
            if (System.currentTimeMillis() >= currentStartTime + animTime) {
                if (mParams.x != (startX + xDistance) || mParams.y != (startY + yDistance)) {
                    mParams.x = startX + xDistance;
                    mParams.y = startY + yDistance;
                    windowManager.updateViewLayout(FloatBallView.this, mParams);
                }
                isAnchoring = false;
                return;
            }
            float delta = interpolator.getInterpolation((System.currentTimeMillis() - currentStartTime) / (float) animTime);
            int xMoveDistance = (int) (xDistance * delta);
            int yMoveDistance = (int) (yDistance * delta);
            Log.e(TAG, "delta:  " + delta + "  xMoveDistance  " + xMoveDistance + "   yMoveDistance  " + yMoveDistance);
            mParams.x = startX + xMoveDistance;
            mParams.y = startY + yMoveDistance;
            if (!isShowing) {
                return;
            }
            windowManager.updateViewLayout(FloatBallView.this, mParams);
            FloatBallView.this.postDelayed(this, 16);
        }
    }

    private void updateViewPosition() {
        //增加移动误差
        mParams.x = (int) (xInScreen - xInView);
        mParams.y = (int) (yInScreen - yInView);
        Log.e(TAG, "x  " + mParams.x + "   y  " + mParams.y);
        windowManager.updateViewLayout(this, mParams);
    }
}
