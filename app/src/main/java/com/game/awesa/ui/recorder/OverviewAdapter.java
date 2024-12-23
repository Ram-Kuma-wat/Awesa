package com.game.awesa.ui.recorder;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.codersworld.awesalibs.beans.matches.ReactionsBean;
import com.codersworld.awesalibs.listeners.OnReactionListener;
import com.codersworld.awesalibs.utils.CommonMethods;
import com.game.awesa.R;
import com.game.awesa.databinding.ItemVideoBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OverviewAdapter extends RecyclerView.Adapter<OverviewAdapter.GameHolder> {
     public Context context;
     public List<ReactionsBean> list;
    OnReactionListener mListener;
    public OverviewAdapter(Context context2, List<ReactionsBean> list, OnReactionListener mListener) {
        this.context = context2;
        this.mListener = mListener;
        this.list = list;
    }
    public OverviewAdapter(Context context2, List<ReactionsBean> list ) {
        this.context = context2;
         this.list = list;
    }

    @NonNull
    public OverviewAdapter.GameHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = this.context;
        LayoutInflater inflater
                = LayoutInflater.from(context);

        View gameItemView
                = inflater
                .inflate(R.layout.item_video,
                        parent, false);

        return new GameHolder(gameItemView);
    }

    public void addAll(ArrayList<ReactionsBean> mList){
        if (CommonMethods.isValidArrayList(mList)) {
            list = mList;
            notifyItemRangeInserted(0, list.size());
        }
    }
    public void delete(int position){
        if (CommonMethods.isValidList(list)){
            if (list.size() > position) {
                list.remove(position);
                notifyItemRemoved(position);
            }
        }
    }
    public void update(ReactionsBean mBeanReaction,int position){
        if (CommonMethods.isValidList(list) && mBeanReaction !=null){
            list.set(position, mBeanReaction);
            notifyItemChanged(position, mBeanReaction);
        }
    }

    Integer half = null;
    public void onBindViewHolder(@NonNull OverviewAdapter.GameHolder viewHolder, int position) {
        ReactionsBean mBean = list.get(position);
        viewHolder.binding.imgDelete.setVisibility(View.VISIBLE);
        viewHolder.binding.imgEdit.setVisibility(View.VISIBLE);

        switch (mBean.getHalf()) {
            case 1:
                viewHolder.binding.txtHalf.setText(context.getString(R.string.lbl_first_half));
                break;
            case 2:
                viewHolder.binding.txtHalf.setText(context.getString(R.string.lbl_second_half));
                break;
            case 3:
                viewHolder.binding.txtHalf.setText(context.getString(R.string.lbl_extratime));
                break;
        }

        if (half == mBean.getHalf()) {
            viewHolder.binding.txtHalf.setVisibility(View.GONE);
        } else {
            viewHolder.binding.txtHalf.setVisibility(View.VISIBLE);
        }
        half = mBean.getHalf();
        CommonMethods.setTextWithHtml(mBean.getTeam_name(), viewHolder.binding.txtTeam);
        CommonMethods.setTextWithHtml(mBean.getReaction(), viewHolder.binding.txtReaction);
        viewHolder.binding.txtTime.setText(mBean.getTime());
        try{
            if(CommonMethods.isValidString(mBean.getVideo())) {
                viewHolder.binding.imgThumbnail.setImageBitmap(CommonMethods.createVideoThumb(context, Uri.fromFile(new File(mBean.getVideo()))));
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public int getItemCount() {
        return this.list.size();
    }

    public class GameHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ItemVideoBinding binding;

        public GameHolder(View view) {
            super(view);
            binding = DataBindingUtil.bind(view);
            binding.rlPlay.setOnClickListener(this);
            binding.imgDelete.setOnClickListener(this);
            binding.imgEdit.setOnClickListener(this);
        }

        public void onClick(View view) {
            if (mListener !=null){
                if (view.getId()==R.id.rlPlay) {
                    mListener.OnReactionAction(list.get(getBindingAdapterPosition()),1, getBindingAdapterPosition());
                }else if(view.getId()==R.id.imgDelete){
                    mListener.OnReactionAction(list.get(getBindingAdapterPosition()),2, getBindingAdapterPosition());
                }else{
                    mListener.OnReactionAction(list.get(getBindingAdapterPosition()),3, getBindingAdapterPosition());
                }
            }
        }
    }
}
