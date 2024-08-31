package com.game.awesa.ui.matches;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.codersworld.awesalibs.beans.matches.MatchesBean;
import com.codersworld.awesalibs.beans.matches.ReactionsBean;
import com.codersworld.awesalibs.listeners.OnMatchListener;
import com.codersworld.awesalibs.utils.CommonMethods;
import com.game.awesa.R;
import com.game.awesa.databinding.HistoryItemLayoutBinding;
import com.game.awesa.databinding.VideosItemLayoutBinding;
import com.game.awesa.utils.DownloadImage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VideosAdapter extends RecyclerView.Adapter {
    public Context context;
    public List<MatchesBean.VideosBean> list;
    OnMatchListener mListener;

    public VideosAdapter(Context context2, List<MatchesBean.VideosBean> list, OnMatchListener mListener) {
        this.context = context2;
        this.mListener = mListener;
        this.list = list;
    }

    public VideosAdapter(Context context2, List<MatchesBean.VideosBean> list) {
        this.context = context2;
        this.list = list;
    }

    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new GameHolder(LayoutInflater.from(this.context).inflate(R.layout.videos_item_layout, viewGroup, false));
    }

    public void addAll(ArrayList<MatchesBean.VideosBean> mList) {
        if (CommonMethods.isValidArrayList(mList)) {
            list = mList;
            notifyDataSetChanged();
        }
    }

    public void addOne(ArrayList<MatchesBean.VideosBean> mList) {
        if (CommonMethods.isValidArrayList(mList)) {
            list.addAll(mList);
            notifyDataSetChanged();
        }
    }
    public void updateVideo(ReactionsBean mBean1, MatchesBean.VideosBean mBean2) {
        if (mBean1 !=null && mBean2 !=null) {
            for (int a =0;a<list.size();a++) {
                if (list.get(a).getLocal_id()==mBean1.getId()){
                    list.set(a,mBean2);
                }
            }
            notifyDataSetChanged();
        }
    }

    String half = "";

    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        GameHolder mHolder = (GameHolder) viewHolder;
        MatchesBean.VideosBean mBean = list.get(i);
        mHolder.binding.txtHalf.setText((mBean.getHalf() == 1) ? context.getString(R.string.lbl_first_half) : context.getString(R.string.lbl_second_half));
        if (half.equalsIgnoreCase(mBean.getHalf() + "")) {
            mHolder.binding.txtHalf.setVisibility(View.GONE);
        } else {
            mHolder.binding.txtHalf.setVisibility(View.VISIBLE);
        }
        mHolder.binding.imgDelete.setVisibility(View.VISIBLE);

        half = mBean.getHalf() + "";
        CommonMethods.setTextWithHtml(mBean.getTitle(), mHolder.binding.txtTeam);
//        mHolder.binding.txtTeam.setText(mBean.getTitle());
        mHolder.binding.txtTime.setText(mBean.getTime());
        try{
            if(CommonMethods.isValidString(mBean.getLocal_video())) {
                mHolder.binding.imgThumbnail.setImageBitmap(CommonMethods.createVideoThumb(context, Uri.fromFile(new File(mBean.getLocal_video()))));
                mHolder.binding.imgDelete.setVisibility(View.GONE);
                mHolder.binding.pbLoading.setVisibility(View.VISIBLE);
            }else{
                CommonMethods.loadImage(context, mBean.getThumbnail(), mHolder.binding.imgThumbnail);
                mHolder.binding.imgDelete.setVisibility(View.VISIBLE);
                mHolder.binding.pbLoading.setVisibility(View.GONE);
            }
        }catch (Exception ex){
            CommonMethods.loadImage(context, mBean.getThumbnail(), mHolder.binding.imgThumbnail);
            ex.printStackTrace();
        }
        //new DownloadImage(mHolder.binding.imgThumbnail).execute(mBean.getVideo());

 /*
        CommonMethods.loadImage(context, mBean.getTeam1_image(), mHolder.binding.imgTeam1);
        mHolder.binding.tvTeam2.setText(mBean.getTeam2());
        CommonMethods.loadImage(context, mBean.getTeam2_image(), mHolder.binding.imgTeam2);
        mHolder.binding.txtDate.setText( mBean.getCreated_date() );*/
    }

    public void deleteVideo(int position){
        if (position>=0 && list.size()>position){
            list.remove(position);
            notifyDataSetChanged();
        }
    }
    public int getItemCount() {
        return this.list.size();
    }

    class GameHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        VideosItemLayoutBinding binding;

        public GameHolder(View view) {
            super(view);
            binding = DataBindingUtil.bind(view);
            view.setOnClickListener(this);
            binding.imgDelete.setOnClickListener(this);
        }

        public void onClick(View view) {
            if (mListener != null) {
                MatchesBean.VideosBean mBean = list.get(getAdapterPosition());
                if (mBean != null) {
                    if (view.getId() == R.id.imgDelete) {
                        mBean.setIsDelete("1");
                        mListener.onVideoDelete(getAdapterPosition(),mBean);
                    }else {
                        mListener.onVideoClick(mBean);
                    }
                }
            }
        }
    }
}
