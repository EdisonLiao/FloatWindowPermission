package com.android.permission.views;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.floatwindowpermission.R;
import com.android.permission.adapters.QuickResponseAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * created by edison 2018/11/1
 */
public class QuickResponseWorkView extends FrameLayout implements QuickResponseAdapter.QuickWorkListener{

    private QuickWorkViewListener mListener;
    private boolean mIsEmoji = false;
    private RecyclerView mRv;
    private QuickResponseAdapter mAdapter;
    private EditText et;

    @Override
    public void onWorkClicked(int pos, String word) {
        Toast.makeText(getContext(),getResources().getString(R.string.copy_success),Toast.LENGTH_LONG).show();
        ClipboardManager cm = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData mClipData = ClipData.newPlainText("Messenger", word);
        if (cm != null) {
            cm.setPrimaryClip(mClipData);
        }
    }

    public interface QuickWorkViewListener {
        void onWorkClose();
    }

    public QuickResponseWorkView(@NonNull Context context, QuickWorkViewListener listener, boolean isEmoji) {
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
            final ImageView ivAdd = root.findViewById(R.id.iv_add);
            et = root.findViewById(R.id.et_edit);
            et.setCursorVisible(false);
            et.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    et.setCursorVisible(true);
                    ivAdd.setImageResource(R.mipmap.ic_add_blue);
                }
            });

            ivAdd.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    String word = et.getText().toString();
                    if (!TextUtils.isEmpty(word.trim()) && mAdapter != null){
                        mAdapter.addLast(word);
                        ivAdd.setImageResource(R.mipmap.ic_add);
                        et.setCursorVisible(false);
                        et.setText("");
                        hideKeyBoard();
                    }
                }
            });

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

        mAdapter = new QuickResponseAdapter(this);
        mRv.setLayoutManager(new GridLayoutManager(getContext(),3));
        mRv.setHasFixedSize(true);
        mRv.setAdapter(mAdapter);
        mAdapter.addAll(wordList);
    }

    private void hideKeyBoard(){
        InputMethodManager manager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager != null && et != null && manager.isActive()){
            manager.hideSoftInputFromWindow(et.getWindowToken(),0);
        }
    }



}
