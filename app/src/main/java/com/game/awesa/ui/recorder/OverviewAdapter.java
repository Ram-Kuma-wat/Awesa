package com.game.awesa.ui.recorder;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.codersworld.awesalibs.beans.matches.MatchesBean;
import com.codersworld.awesalibs.beans.matches.ReactionsBean;
import com.codersworld.awesalibs.listeners.OnMatchListener;
import com.codersworld.awesalibs.listeners.OnReactionListener;
import com.codersworld.awesalibs.utils.CommonMethods;
import com.game.awesa.R;
import com.game.awesa.databinding.VideosItemLayoutBinding;
import com.game.awesa.utils.DownloadImage;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OverviewAdapter extends RecyclerView.Adapter {
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

    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new GameHolder(LayoutInflater.from(this.context).inflate(R.layout.videos_item_layout, viewGroup, false));
    }

    public void addAll(ArrayList<ReactionsBean> mList){
        if (CommonMethods.isValidArrayList(mList)){
            list = mList;
            notifyDataSetChanged();
        }
    }
    public void delete(int position){
        if (CommonMethods.isValidList(list)){
            if (list.size()>position) {
                list.remove(position);
                notifyDataSetChanged();
            }
        }
    }
    String half="";
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        GameHolder mHolder = (GameHolder) viewHolder;
        ReactionsBean mBean = list.get(i);
        mHolder.binding.imgDelete.setVisibility(View.VISIBLE);
        mHolder.binding.txtHalf.setText((mBean.getHalf()==1)?context.getString(R.string.lbl_first_half):context.getString(R.string.lbl_second_half));
        if (half.equalsIgnoreCase(mBean.getHalf()+"")){
            mHolder.binding.txtHalf.setVisibility(View.GONE);
        }else{
            mHolder.binding.txtHalf.setVisibility(View.VISIBLE);
        }
        half = mBean.getHalf()+"";
        CommonMethods.setTextWithHtml(mBean.getTeam_name(),mHolder.binding.txtTeam);
        CommonMethods.setTextWithHtml(mBean.getReaction(),mHolder.binding.txtReaction);
//        mHolder.binding.txtTeam.setText(mBean.getTitle());
        mHolder.binding.txtTime.setText(mBean.getTime());
        try{
            if(CommonMethods.isValidString(mBean.getVideo())) {
                mHolder.binding.imgThumbnail.setImageBitmap(CommonMethods.createVideoThumb(context, Uri.fromFile(new File(mBean.getVideo()))));
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        //new DownloadImage(mHolder.binding.imgThumbnail).execute(mBean.getVideo());

 /*
        CommonMethods.loadImage(context, mBean.getTeam1_image(), mHolder.binding.imgTeam1);
        mHolder.binding.tvTeam2.setText(mBean.getTeam2());
        CommonMethods.loadImage(context, mBean.getTeam2_image(), mHolder.binding.imgTeam2);
        mHolder.binding.txtDate.setText( mBean.getCreated_date() );*/
    }

    public int getItemCount() {
        return this.list.size();
    }

    class GameHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        VideosItemLayoutBinding binding;

        public GameHolder(View view) {
            super(view);
            binding = DataBindingUtil.bind(view);
            binding.rlPlay.setOnClickListener(this);
            binding.imgDelete.setOnClickListener(this);
        }

        public void onClick(View view) {
            if (mListener !=null){
                if (view.getId()==R.id.rlPlay) {
                    mListener.OnReactionAction(list.get(getAdapterPosition()),1,getAdapterPosition());
                }else{
                    mListener.OnReactionAction(list.get(getAdapterPosition()),2,getAdapterPosition());
                }
            }
        }
    }
}
