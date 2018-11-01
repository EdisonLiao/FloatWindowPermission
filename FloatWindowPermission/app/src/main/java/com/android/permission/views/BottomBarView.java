package com.android.permission.views;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.android.floatwindowpermission.R;

/**
 * created by edison 2018/10/31
 */
public class BottomBarView extends FrameLayout{

    private static final String TAG = "BottomBarView";
    private View mRootView;
    private BottomBarListener mListener;

    public interface BottomBarListener{
        void onCloseIconLayouted(int x,int y);
    }

    public BottomBarView(@NonNull Context context,BottomBarListener listener) {
        super(context);
        initView();
        mListener = listener;
    }

    public BottomBarView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public BottomBarView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public BottomBarView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        mRootView = inflater.inflate(R.layout.bottom_bar_layout, null);
        addView(mRootView);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        final ImageView close = mRootView.findViewById(R.id.iv_close);
        close.post(new Runnable() {
            @Override
            public void run() {
                int[] location = new int[2];
                close.getLocationOnScreen(location);
                if (mListener != null){
                    mListener.onCloseIconLayouted(location[0],location[1]);
                }
            }
        });

    }
}
