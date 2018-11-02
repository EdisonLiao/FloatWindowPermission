package com.android.permission.views;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.android.floatwindowpermission.R;

/**
 * created by edison 2018/11/1
 */
public class QuickResponseView extends LinearLayout{

    private QuickResponseListener mListener;

    public interface QuickResponseListener{
        void onEmojiClick();
        void onQuickClick();
        void onCloseClick();
        void onBackMessengerClick();
    }

    public QuickResponseView(Context context,QuickResponseListener listener) {
        super(context);
        mListener = listener;
        initView();
    }

    public QuickResponseView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public QuickResponseView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public QuickResponseView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView(){
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View root = inflater.inflate(R.layout.quick_response_layout, null);
        root.findViewById(R.id.ll_emoji).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null){
                    mListener.onEmojiClick();
                }
            }
        });

        root.findViewById(R.id.ll_quick_response).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null){
                    mListener.onQuickClick();
                }
            }
        });

        root.findViewById(R.id.ll_back_messenger).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null){
                    mListener.onBackMessengerClick();
                }
            }
        });

        root.findViewById(R.id.iv_quick_close).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null){
                    mListener.onCloseClick();
                }
            }
        });

        addView(root);
    }



}
