/*
 * Copyright (C) 2016 Facishare Technology Co., Ltd. All Rights Reserved.
 */
package com.android.permission;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.ImageView;

import com.android.floatwindowpermission.R;
import com.android.permission.rom.HuaweiUtils;
import com.android.permission.rom.MeizuUtils;
import com.android.permission.rom.MiuiUtils;
import com.android.permission.rom.OppoUtils;
import com.android.permission.rom.QikuUtils;
import com.android.permission.rom.RomUtils;
import com.android.permission.views.BottomBarView;
import com.android.permission.views.FloatBallView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
    private Dialog dialog;
    private boolean isBottomBarDismiss = true;
    private WindowManager.LayoutParams bottomBarParams;
    private BottomBarView bottomBar;
    private boolean isFloatChangeIcon = false;
    private Context mContext;
    private int mCloseCenterX = -1;
    private int mCloseCenterY = -1;
    private int mFloatBallRadiu;
    private int mCloseBallRadiu;

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
        }
    };

    private BottomBarView.BottomBarListener bottomBarListener = new BottomBarView.BottomBarListener() {
        @Override
        public void onCloseIconLayouted(int x, int y) {
            if (mCloseCenterX == -1 || mCloseCenterY == -1){
                int size = DensityUtils.dp2px(mContext,32);
                mCloseCenterX = x + size;
                mCloseCenterY = y + size;
                mCloseBallRadiu = size / 2;
                mFloatBallRadiu = DensityUtils.dp2px(mContext,48) / 2;
            }
        }
    };

    public void applyOrShowFloatWindow(Context context) {
        if (checkPermission(context)) {
            showWindow(context);
        } else {
            applyPermission(context);
        }
    }

    private boolean checkPermission(Context context) {
        //6.0 版本之后由于 google 增加了对悬浮窗权限的管理，所以方式就统一了
        if (Build.VERSION.SDK_INT < 23) {
            if (RomUtils.checkIsMiuiRom()) {
                return miuiPermissionCheck(context);
            } else if (RomUtils.checkIsMeizuRom()) {
                return meizuPermissionCheck(context);
            } else if (RomUtils.checkIsHuaweiRom()) {
                return huaweiPermissionCheck(context);
            } else if (RomUtils.checkIs360Rom()) {
                return qikuPermissionCheck(context);
            } else if (RomUtils.checkIsOppoRom()) {
                return oppoROMPermissionCheck(context);
            }
        }
        return commonROMPermissionCheck(context);
    }

    private boolean huaweiPermissionCheck(Context context) {
        return HuaweiUtils.checkFloatWindowPermission(context);
    }

    private boolean miuiPermissionCheck(Context context) {
        return MiuiUtils.checkFloatWindowPermission(context);
    }

    private boolean meizuPermissionCheck(Context context) {
        return MeizuUtils.checkFloatWindowPermission(context);
    }

    private boolean qikuPermissionCheck(Context context) {
        return QikuUtils.checkFloatWindowPermission(context);
    }

    private boolean oppoROMPermissionCheck(Context context) {
        return OppoUtils.checkFloatWindowPermission(context);
    }

    private boolean commonROMPermissionCheck(Context context) {
        //最新发现魅族6.0的系统这种方式不好用，天杀的，只有你是奇葩，没办法，单独适配一下
        if (RomUtils.checkIsMeizuRom()) {
            return meizuPermissionCheck(context);
        } else {
            Boolean result = true;
            if (Build.VERSION.SDK_INT >= 23) {
                try {
                    Class clazz = Settings.class;
                    Method canDrawOverlays = clazz.getDeclaredMethod("canDrawOverlays", Context.class);
                    result = (Boolean) canDrawOverlays.invoke(null, context);
                } catch (Exception e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
            }
            return result;
        }
    }

    private void applyPermission(Context context) {
        if (Build.VERSION.SDK_INT < 23) {
            if (RomUtils.checkIsMiuiRom()) {
                miuiROMPermissionApply(context);
            } else if (RomUtils.checkIsMeizuRom()) {
                meizuROMPermissionApply(context);
            } else if (RomUtils.checkIsHuaweiRom()) {
                huaweiROMPermissionApply(context);
            } else if (RomUtils.checkIs360Rom()) {
                ROM360PermissionApply(context);
            } else if (RomUtils.checkIsOppoRom()) {
                oppoROMPermissionApply(context);
            }
        } else {
            commonROMPermissionApply(context);
        }
    }

    private void ROM360PermissionApply(final Context context) {
        showConfirmDialog(context, new OnConfirmResult() {
            @Override
            public void confirmResult(boolean confirm) {
                if (confirm) {
                    QikuUtils.applyPermission(context);
                } else {
                    Log.e(TAG, "ROM:360, user manually refuse OVERLAY_PERMISSION");
                }
            }
        });
    }

    private void huaweiROMPermissionApply(final Context context) {
        showConfirmDialog(context, new OnConfirmResult() {
            @Override
            public void confirmResult(boolean confirm) {
                if (confirm) {
                    HuaweiUtils.applyPermission(context);
                } else {
                    Log.e(TAG, "ROM:huawei, user manually refuse OVERLAY_PERMISSION");
                }
            }
        });
    }

    private void meizuROMPermissionApply(final Context context) {
        showConfirmDialog(context, new OnConfirmResult() {
            @Override
            public void confirmResult(boolean confirm) {
                if (confirm) {
                    MeizuUtils.applyPermission(context);
                } else {
                    Log.e(TAG, "ROM:meizu, user manually refuse OVERLAY_PERMISSION");
                }
            }
        });
    }

    private void miuiROMPermissionApply(final Context context) {
        showConfirmDialog(context, new OnConfirmResult() {
            @Override
            public void confirmResult(boolean confirm) {
                if (confirm) {
                    MiuiUtils.applyMiuiPermission(context);
                } else {
                    Log.e(TAG, "ROM:miui, user manually refuse OVERLAY_PERMISSION");
                }
            }
        });
    }

    private void oppoROMPermissionApply(final Context context) {
        showConfirmDialog(context, new OnConfirmResult() {
            @Override
            public void confirmResult(boolean confirm) {
                if (confirm) {
                    OppoUtils.applyOppoPermission(context);
                } else {
                    Log.e(TAG, "ROM:miui, user manually refuse OVERLAY_PERMISSION");
                }
            }
        });
    }

    /**
     * 通用 rom 权限申请
     */
    private void commonROMPermissionApply(final Context context) {
        //这里也一样，魅族系统需要单独适配
        if (RomUtils.checkIsMeizuRom()) {
            meizuROMPermissionApply(context);
        } else {
            if (Build.VERSION.SDK_INT >= 23) {
                showConfirmDialog(context, new OnConfirmResult() {
                    @Override
                    public void confirmResult(boolean confirm) {
                        if (confirm) {
                            try {
                                commonROMPermissionApplyInternal(context);
                            } catch (Exception e) {
                                Log.e(TAG, Log.getStackTraceString(e));
                            }
                        } else {
                            Log.d(TAG, "user manually refuse OVERLAY_PERMISSION");
                            //需要做统计效果
                        }
                    }
                });
            }
        }
    }

    public static void commonROMPermissionApplyInternal(Context context) throws NoSuchFieldException, IllegalAccessException {
        Class clazz = Settings.class;
        Field field = clazz.getDeclaredField("ACTION_MANAGE_OVERLAY_PERMISSION");

        Intent intent = new Intent(field.get(null).toString());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        context.startActivity(intent);
    }

    private void showConfirmDialog(Context context, OnConfirmResult result) {
        showConfirmDialog(context, "您的手机没有授予悬浮窗权限，请开启后再试", result);
    }

    private void showConfirmDialog(Context context, String message, final OnConfirmResult result) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

        dialog = new AlertDialog.Builder(context).setCancelable(true).setTitle("")
                .setMessage(message)
                .setPositiveButton("现在去开启",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirmResult(true);
                                dialog.dismiss();
                            }
                        }).setNegativeButton("暂不开启",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirmResult(false);
                                dialog.dismiss();
                            }
                        }).create();
        dialog.show();
    }

    private interface OnConfirmResult {
        void confirmResult(boolean confirm);
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
        mParams.x = screenWidth - DensityUtils.dp2px(context, 100);
        mParams.y = screenHeight / 2;


        floatView = new FloatBallView(context,listener);
        floatView.setParams(mParams);
        floatView.setIsShowing(true);
        windowManager.addView(floatView, mParams);
    }

    private void handleFloatBallIcon(boolean isMoving) {
        ImageView ivBall = floatView.findViewById(R.id.iv_ball);
        if (isMoving && !isFloatChangeIcon) {
            isFloatChangeIcon = true;
            ivBall.setImageResource(R.mipmap.ic_ball_launcher_press);
        } else if (!isMoving) {
            isFloatChangeIcon = false;
            ivBall.setImageResource(R.mipmap.ic_ball_launcher_normal);
        }
    }

    private void handleBottomBarBg(int x, int y){
        if (bottomBar != null){
            ImageView ivBg = bottomBar.findViewById(R.id.iv_bottom_bg);
            int floatBallCenterX = x + mFloatBallRadiu;
            int floatBallCenterY = y + mFloatBallRadiu;

            if (mCloseCenterX != -1) {
                double x2 = Math.pow((mCloseCenterX - floatBallCenterX),2);
                double y2 = Math.pow((mCloseCenterY - floatBallCenterY),2);
                double d = Math.sqrt((x2 + y2));
                //悬浮球跟关闭按钮圆心距离
                double distance = mFloatBallRadiu + mCloseBallRadiu;
                if (d <= distance){
                    ivBg.setImageResource(R.mipmap.bg_bottom_red);
                }else {
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

    public void showBottomBar(Context context) {

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

        bottomBarParams = new WindowManager.LayoutParams();
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

    public void dismissBottomBar() {
        if (isBottomBarDismiss) {
            Log.e(TAG, "window can not be dismiss cause it has not been added");
            return;
        }
        isBottomBarDismiss = true;
        if (windowManager != null && bottomBar != null) {
            windowManager.removeViewImmediate(bottomBar);
        }
    }

    public void setContext(Context mContext) {
        this.mContext = mContext;
    }


}
