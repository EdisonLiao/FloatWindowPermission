/*
 * Copyright (C) 2016 Facishare Technology Co., Ltd. All Rights Reserved.
 */
package com.example.floatwindow.permission;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.floatwindow.R;
import com.example.floatwindow.permission.views.BottomBarView;
import com.example.floatwindow.permission.views.FloatBallView;
import com.example.floatwindow.permission.views.QuickResponseView;
import com.example.floatwindow.permission.views.QuickResponseWorkView;


/**
 * Description:
 *
 * @author zhaozp
 * @since 2016-10-17
 */

public class FloatWindowManager {
    private static final String TAG = "FloatWindowManager";

    private static volatile FloatWindowManager instance;

    private boolean isWindowDismiss = true;
    private WindowManager windowManager = null;
    private WindowManager.LayoutParams mParams = null;
    private FloatBallView floatView = null;
    private boolean isBottomBarDismiss = true;
    private BottomBarView bottomBar;
    private QuickResponseView quickResponseView;
    private QuickResponseWorkView quickWordView;
    private IFloatWindowCallback mCallback;
    private PermissionMgr mPermissionMgr;
    private IUsageRecord mUsageRecord;
    private boolean isFloatChangeIcon = false;
    private Context mContext;
    private int mBottomCloseCenterX = -1;
    private int mBottomCloseCenterY = -1;
    private int mFloatBallRadiu;
    private int mCloseBallRadiu;
    private boolean isBottomBarRed = false;

    public static FloatWindowManager getInstance() {
        if (instance == null) {
            synchronized (FloatWindowManager.class) {
                if (instance == null) {
                    instance = new FloatWindowManager();
                }
            }
        }
        return instance;
    }

    public FloatWindowManager(){
        mPermissionMgr = new PermissionMgr();
    }

    private FloatBallView.FloatBallListener listener = new FloatBallView.FloatBallListener() {
        @Override
        public void onFloatBallMoving(int x, int y) {
            handleFloatBallIcon(true);
            showBottomBar(mContext);
            handleBottomBarBg(x,y);
        }

        @Override
        public void onFloatBallStopMoving(int x, int y) {
            handleFloatBallIcon(false);
            dismissBottomBar();
            if (isBottomBarRed){
                dismissWindow();
                mUsageRecord.pv(IUsageRecord.FLOATBALL_CLOSE);
            }
        }

        @Override
        public void onFloatBallClicked() {
            dismissWindow();
            showQuickResponse();
            mUsageRecord.pv(IUsageRecord.FLOATBALL_CLICK);
        }
    };

    private BottomBarView.BottomBarListener bottomBarListener = new BottomBarView.BottomBarListener() {
        @Override
        public void onCloseIconLayouted(int x, int y) {
            if (mBottomCloseCenterX == -1 || mBottomCloseCenterY == -1){
                int size = DensityUtils.dp2px(mContext,32);
                mBottomCloseCenterX = x + size;
                mBottomCloseCenterY = y + size;
                mCloseBallRadiu = size / 2;
                mFloatBallRadiu = DensityUtils.dp2px(mContext,48) / 2;
            }
        }
    };

    private QuickResponseView.QuickResponseListener quickResponseListener = new QuickResponseView.QuickResponseListener() {
        @Override
        public void onEmojiClick() {
            dismissQuickResponse();
            showQuickWord(true);
            mUsageRecord.pv(IUsageRecord.FLOATBALL_EMOJI);
        }

        @Override
        public void onQuickClick() {
            dismissQuickResponse();
            showQuickWord(false);
            mUsageRecord.pv(IUsageRecord.FLOATBALL_RESPONSE);
        }

        @Override
        public void onCloseClick() {
            dismissQuickResponse();
            showWindow(mContext);
        }

        @Override
        public void onBackMessengerClick() {
            if (mCallback != null){
                mCallback.backToMessenger();
                mUsageRecord.pv(IUsageRecord.FLOATBALL_BACK_CLICK);
            }
        }
    };

    private QuickResponseWorkView.QuickWorkViewListener quickWorkListener = new QuickResponseWorkView.QuickWorkViewListener() {
        @Override
        public void onWorkClose() {
            dismissQuickWork();
            showQuickResponse();
        }
    };

    public void applyOrShowFloatWindow(Context context) {
        if (mPermissionMgr.checkPermission(context)) {
            showWindow(context);
        } else {
            mPermissionMgr.applyPermission(context);
        }
    }

    public boolean checkFloatWindowPermission(Context context){
        if (mPermissionMgr != null){
            return mPermissionMgr.checkPermission(context);
        }else {
            return false;
        }
    }

    public void applyFloatWindowPermission(Context context){
        if (mPermissionMgr != null){
            mPermissionMgr.applyPermission(context);
        }
    }

    private void showWindow(Context context) {
        if (!isWindowDismiss) {
            Log.e(TAG, "view is already added here");
            return;
        }

        isWindowDismiss = false;
        if (windowManager == null) {
            windowManager = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        }

        Point size = new Point();
        windowManager.getDefaultDisplay().getSize(size);
        int screenWidth = size.x;
        int screenHeight = size.y;

        mParams = new WindowManager.LayoutParams();
        mParams.packageName = context.getPackageName();
        mParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        int mType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mType = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        mParams.type = mType;
        mParams.format = PixelFormat.RGBA_8888;
        mParams.gravity = Gravity.LEFT | Gravity.TOP;
        mParams.x = screenWidth;
        mParams.y = screenHeight / 2;


        floatView = new FloatBallView(context,listener);
        floatView.setParams(mParams);
        floatView.setIsShowing(true);
        windowManager.addView(floatView, mParams);
        mUsageRecord.pv(IUsageRecord.FLOATBALL_SHOW);
    }

    private void handleFloatBallIcon(boolean isMoving) {
        ImageView ivBallNormal = floatView.findViewById(R.id.iv_ball_normal);
        ImageView ivBallPress = floatView.findViewById(R.id.iv_ball_press);
        if (isMoving && !isFloatChangeIcon) {
            isFloatChangeIcon = true;
            ivBallNormal.setVisibility(View.INVISIBLE);
            ivBallPress.setVisibility(View.VISIBLE);

        } else if (!isMoving) {
            isFloatChangeIcon = false;
            ivBallNormal.setVisibility(View.VISIBLE);
            ivBallPress.setVisibility(View.INVISIBLE);
        }
    }

    private void handleBottomBarBg(int x, int y){
        if (bottomBar != null){
            ImageView ivBg = bottomBar.findViewById(R.id.iv_bottom_bg);
            int floatBallCenterX = x + mFloatBallRadiu;
            int floatBallCenterY = y + mFloatBallRadiu;

            if (mBottomCloseCenterX != -1) {
                double x2 = Math.pow((mBottomCloseCenterX - floatBallCenterX),2);
                double y2 = Math.pow((mBottomCloseCenterY - floatBallCenterY),2);
                double d = Math.sqrt((x2 + y2));
                //悬浮球跟关闭按钮圆心距离
                double distance = mFloatBallRadiu + mCloseBallRadiu;
                if (d <= distance){
                    isBottomBarRed = true;
                    ivBg.setImageResource(R.mipmap.bg_bottom_red);
                }else {
                    isBottomBarRed = false;
                    ivBg.setImageResource(R.mipmap.bg_bottom_gray);
                }
            }
        }
    }

    public void dismissWindow() {
        if (isWindowDismiss) {
            Log.e(TAG, "window can not be dismiss cause it has not been added");
            return;
        }

        isWindowDismiss = true;
        floatView.setIsShowing(false);
        if (windowManager != null && floatView != null) {
            windowManager.removeViewImmediate(floatView);
        }
    }

    private void showBottomBar(Context context) {

        if (!isBottomBarDismiss) {
            return;
        }
        Log.e("handle","showBottom");
        isBottomBarDismiss = false;
        if (windowManager == null) {
            windowManager =
                    (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        }

        Point size = new Point();
        windowManager.getDefaultDisplay().getSize(size);
        int screenWidth = size.x;
        int screenHeight = size.y;

        WindowManager.LayoutParams bottomBarParams = new WindowManager.LayoutParams();
        bottomBarParams.packageName = context.getPackageName();
        bottomBarParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        bottomBarParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        bottomBarParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            bottomBarParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY - 1;
        } else {
            bottomBarParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        bottomBarParams.format = PixelFormat.RGBA_8888;
        bottomBarParams.gravity = Gravity.LEFT | Gravity.TOP;
        bottomBarParams.x = screenWidth;
        bottomBarParams.y = screenHeight;

        bottomBar = new BottomBarView(context, bottomBarListener);
        windowManager.addView(bottomBar, bottomBarParams);
    }

    private void showQuickResponse(){
        if (windowManager == null) {
            windowManager =
                    (WindowManager) mContext.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        }

        WindowManager.LayoutParams quickResponseParams = new WindowManager.LayoutParams();
        quickResponseParams.packageName = mContext.getPackageName();
        quickResponseParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        quickResponseParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        quickResponseParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            quickResponseParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            quickResponseParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        }
        quickResponseParams.format = PixelFormat.RGBA_8888;
        quickResponseParams.gravity = Gravity.CENTER;

        quickResponseView = new QuickResponseView(mContext, quickResponseListener);
        windowManager.addView(quickResponseView, quickResponseParams);
    }

    private void showQuickWord(boolean isEmoji){
        if (windowManager == null) {
            windowManager =
                    (WindowManager) mContext.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        }

        WindowManager.LayoutParams quickWordParmas = new WindowManager.LayoutParams();
        quickWordParmas.packageName = mContext.getPackageName();
        quickWordParmas.width = WindowManager.LayoutParams.WRAP_CONTENT;
        quickWordParmas.height = WindowManager.LayoutParams.WRAP_CONTENT;
        quickWordParmas.flags = WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            quickWordParmas.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            quickWordParmas.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        }
        quickWordParmas.format = PixelFormat.RGBA_8888;
        quickWordParmas.gravity = Gravity. CENTER;

        quickWordView = new QuickResponseWorkView(mContext,quickWorkListener,isEmoji,mUsageRecord);
        windowManager.addView(quickWordView, quickWordParmas);
    }

    private void dismissBottomBar() {
        if (isBottomBarDismiss) {
            Log.e(TAG, "window can not be dismiss cause it has not been added");
            return;
        }
        isBottomBarDismiss = true;
        if (windowManager != null && bottomBar != null) {
            windowManager.removeViewImmediate(bottomBar);
        }
    }

    private void dismissQuickResponse(){
        if (windowManager != null && quickResponseView != null) {
            windowManager.removeViewImmediate(quickResponseView);
        }
    }

    private void dismissQuickWork(){
        if (windowManager != null && quickWordView != null) {
            windowManager.removeViewImmediate(quickWordView);
        }
    }

    public void setContext(Context mContext) {
        this.mContext = mContext;
        PreferenceMgr.getIns().init(mContext);
    }

    public void setUsageRecord(IUsageRecord mUsageRecord) {
        this.mUsageRecord = mUsageRecord;
    }

    public void setCallback(IFloatWindowCallback mCallback) {
        this.mCallback = mCallback;
    }
}
