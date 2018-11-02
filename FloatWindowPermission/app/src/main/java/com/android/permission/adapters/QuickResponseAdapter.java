package com.android.permission.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.floatwindowpermission.R;

import java.util.ArrayList;
import java.util.List;

/**
 * created by edison 2018/11/1
 */
public class QuickResponseAdapter extends RecyclerView.Adapter<QuickResponseAdapter.QuickVH>{

    private List<String> dataList = new ArrayList<>();

    @Override
    public QuickVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_word,parent,false);
        return new QuickVH(root);
    }

    @Override
    public void onBindViewHolder(QuickVH holder, int position) {
        holder.tvWord.setText(dataList.get(position));
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void addAll(List<String> list){
        dataList.addAll(list);
        notifyDataSetChanged();
    }

    public void addLast(String word){
        int pos = dataList.size();
        dataList.add(word);
        notifyItemInserted(pos);
    }

    public class QuickVH extends RecyclerView.ViewHolder{

        TextView tvWord;

        public QuickVH(View itemView) {
            super(itemView);
            tvWord = itemView.findViewById(R.id.tv_word);
        }
    }


}
