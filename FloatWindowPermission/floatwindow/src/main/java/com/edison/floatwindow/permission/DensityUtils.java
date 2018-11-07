package com.example.floatwindow.permission;

import android.content.Context;
import android.graphics.RectF;
import android.view.View;

/**
 * created by edison 2018/10/31
 */
public class DensityUtils {

    public static int dp2px(Context context, float dp){
        if (context == null){
            return 0;
        }
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    /**
     * 计算指定的 View 在屏幕中的坐标。
     */
    public static RectF calcViewScreenLocation(View view,int viewWidth,int viewHeight) {
        int[] location = new int[2];
        // 获取控件在屏幕中的位置，返回的数组分别为控件左顶点的 x、y 的值
        view.getLocationOnScreen(location);
        return new RectF(location[0], location[1], location[0] + viewWidth,
                location[1] + viewHeight);
    }


}
