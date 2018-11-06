package com.example.floatwindow.permission.views;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.floatwindow.R;
import com.example.floatwindow.permission.IUsageRecord;
import com.example.floatwindow.permission.SharePreMgr;
import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * created by edison 2018/11/1
 */
public class QuickResponseWorkView extends FrameLayout implements View.OnClickListener{

    private QuickWorkViewListener mListener;
    private FlexboxLayout mFlexLayout;
    private boolean mIsEmoji = false;
    private EditText et;
    private LayoutInflater mInflater;
    private static final String WORD_DIVIDER = "<";
    private IUsageRecord mUsageRecord;
    private boolean isShowing = false;

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.tv_word){
            String word = (String) view.getTag();
            onWorkClick(word);
            if (mIsEmoji) {
                mUsageRecord.pv(IUsageRecord.FLOATBALL_EMOJI_CLICK, word);
            }else {
                mUsageRecord.pv(IUsageRecord.FLOATBALL_RESPONSE_CLICK,word);
            }
        }
    }

    public boolean isShowing() {
        return isShowing;
    }

    public void setShowing(boolean showing) {
        isShowing = showing;
    }

    public interface QuickWorkViewListener {
        void onWorkClose();
        void onWorkBack();
    }

    public QuickResponseWorkView(@NonNull Context context, QuickWorkViewListener listener, boolean isEmoji, IUsageRecord record) {
        super(context);
        mIsEmoji = isEmoji;
        mListener = listener;
        mUsageRecord = record;
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
        mInflater = LayoutInflater.from(getContext());
        View root = mInflater.inflate(R.layout.quick_response_word_layout, null);
        mFlexLayout = root.findViewById(R.id.flex_layout);
        TextView tvTitle = root.findViewById(R.id.tv_title);
        if (mIsEmoji){
            tvTitle.setText(getResources().getString(R.string.emoji));
        }else {
            tvTitle.setText(getResources().getString(R.string.quick_response));
        }

        root.findViewById(R.id.iv_back).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null){
                    mListener.onWorkBack();
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
        initFlexLayout();

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
                    if (!TextUtils.isEmpty(word.trim()) && mFlexLayout != null){
                        mFlexLayout.addView(addWord(word.trim()),0);
                        ivAdd.setImageResource(R.mipmap.ic_add);
                        et.setCursorVisible(false);
                        et.setText("");
                        hideKeyBoard();
                        saveWord(word.trim());
                        mUsageRecord.pv(IUsageRecord.FLOATBALL_RESPONSE_ADD);
                    }
                }
            });

        }
    }

    private void initFlexLayout(){
        List<String> wordList = new ArrayList<>();
        String addedWord = SharePreMgr.getAddedQuickWork();
        if (!TextUtils.isEmpty(addedWord) && !mIsEmoji){
            String[] addWords = addedWord.split(WORD_DIVIDER);
            wordList.addAll(Arrays.asList(addWords));
        }

        if (mIsEmoji){
            String[] list = getContext().getResources().getStringArray(R.array.emoji_array);
            wordList.addAll(Arrays.asList(list));
        }else {
            String[] list = getContext().getResources().getStringArray(R.array.response_array);
            wordList.addAll(Arrays.asList(list));
        }

        for (String word: wordList){
            mFlexLayout.addView(addWord(word));
        }

    }

    private View addWord(String word){
        View contentView = mInflater.inflate(R.layout.item_word,null);
        TextView tvWord = contentView.findViewById(R.id.tv_word);
        tvWord.setTag(word);
        tvWord.setText(word);
        tvWord.setOnClickListener(this);
        return contentView;
    }

    private void saveWord(String addWord) {
        if (!TextUtils.isEmpty(addWord)) {
            String words = SharePreMgr.getAddedQuickWork();
            if (TextUtils.isEmpty(words)){
                words = addWord + WORD_DIVIDER;
            }else {
                words = words + addWord + WORD_DIVIDER;
            }

            SharePreMgr.addQuickWork(words);
        }
    }

    private void onWorkClick(String word){
        Toast.makeText(getContext(),getResources().getString(R.string.copy_success),Toast.LENGTH_LONG).show();
        ClipboardManager cm = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData mClipData = ClipData.newPlainText("Messenger", word);
        if (cm != null) {
            cm.setPrimaryClip(mClipData);
        }
    }

    private void hideKeyBoard(){
        InputMethodManager manager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager != null && et != null && manager.isActive()){
            manager.hideSoftInputFromWindow(et.getWindowToken(),0);
        }
    }



}
