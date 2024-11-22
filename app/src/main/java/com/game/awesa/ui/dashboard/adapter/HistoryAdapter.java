package com.game.awesa.ui.dashboard.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

 import com.codersworld.awesalibs.beans.matches.MatchesBean;
 import com.codersworld.awesalibs.listeners.OnMatchListener;
import com.codersworld.awesalibs.utils.CommonMethods;
import com.game.awesa.R;
import com.game.awesa.databinding.HistoryItemLayoutBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter {
     public Context context;
     public List<MatchesBean.InfoBean> list;
    OnMatchListener mListener;
    public HistoryAdapter(Context context, List<MatchesBean.InfoBean> list, OnMatchListener mListener) {
        this.context = context;
        this.mListener = mListener;
        this.list = list;
    }

    public HistoryAdapter(Context context, List<MatchesBean.InfoBean> list ) {
        this.context = context;
         this.list = list;
    }

    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new GameHolder(LayoutInflater.from(this.context).inflate(R.layout.history_item_layout, viewGroup, false));
    }

    public void addAll(ArrayList<MatchesBean.InfoBean> mList) {
        if (CommonMethods.isValidArrayList(mList)){
            list = mList;
            notifyDataSetChanged();
        }
    }

    public int checkSize() {
            return list.size();
    }

    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        GameHolder mHolder = (GameHolder) viewHolder;
        MatchesBean.InfoBean mBean = list.get(i);
        mHolder.binding.tvTeam1.setText(mBean.getTeam1());
        CommonMethods.loadImage(context, mBean.getTeam1_image(), mHolder.binding.imgTeam1);
        mHolder.binding.tvTeam2.setText(mBean.getTeam2());
        CommonMethods.loadImage(context, mBean.getTeam2_image(), mHolder.binding.imgTeam2);
        mHolder.binding.txtDate.setText( mBean.getCreated_date() );
        mHolder.binding.txtId.setText(String.format(Locale.US, "#%d", mBean.getId()));
    }

    public int getItemCount() {
        return this.list.size();
    }

    class GameHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        HistoryItemLayoutBinding binding;

        public GameHolder(View view) {
            super(view);
            binding = DataBindingUtil.bind(view);
            view.setOnClickListener(this);
        }

        public void onClick(View view) {
            if (mListener != null){
                mListener.onMatchClick(list.get(getAdapterPosition()));
            }
        }
    }
}
