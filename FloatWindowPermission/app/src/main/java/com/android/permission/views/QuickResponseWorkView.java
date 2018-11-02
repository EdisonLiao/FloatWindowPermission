package com.android.permission.views;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.android.floatwindowpermission.R;
import com.android.permission.DensityUtils;
import com.android.permission.adapters.QuickResponseAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * created by edison 2018/11/1
 */
public class QuickResponseWorkView extends FrameLayout{

    private QuickWorkListener mListener;
    private boolean mIsEmoji = false;
    private RecyclerView mRv;
    private QuickResponseAdapter mAdapter;

    public interface QuickWorkListener{
        void onWorkClose();
    }

    public QuickResponseWorkView(@NonNull Context context,QuickWorkListener listener,boolean isEmoji) {
        super(context);
        mIsEmoji = isEmoji;
        mListener = listener;
        initView();
    }

    public QuickResponseWorkView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public QuickResponseWorkView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public QuickResponseWorkView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView(){
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View root = inflater.inflate(R.layout.quick_response_word_layout, null);
        mRv = root.findViewById(R.id.rv_word);
        root.findViewById(R.id.iv_back).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null){
                    mListener.onWorkClose();
                }
            }
        });

        root.findViewById(R.id.iv_close).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null){
                    mListener.onWorkClose();
                }
            }
        });

        addView(root);
        initRv();

        if (!mIsEmoji){
            root.findViewById(R.id.fl_edit).setVisibility(VISIBLE);
        }
    }

    private void initRv(){
        List<String> wordList = new ArrayList<>();
        if (mIsEmoji){
            String[] list = getContext().getResources().getStringArray(R.array.emoji_array);
            wordList.addAll(Arrays.asList(list));
        }else {
            String[] list = getContext().getResources().getStringArray(R.array.response_array);
            wordList.addAll(Arrays.asList(list));
        }

        mAdapter = new QuickResponseAdapter();
        mRv.setLayoutManager(new GridLayoutManager(getContext(),3));
        mRv.setHasFixedSize(true);
        mRv.setAdapter(mAdapter);
        mAdapter.addAll(wordList);
    }




}
