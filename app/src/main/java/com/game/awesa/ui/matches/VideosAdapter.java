package com.game.awesa.ui.matches;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.codersworld.awesalibs.beans.matches.MatchesBean;
import com.codersworld.awesalibs.beans.matches.ReactionsBean;
import com.codersworld.awesalibs.listeners.OnMatchListener;
import com.codersworld.awesalibs.utils.CommonMethods;
import com.game.awesa.R;
import com.game.awesa.databinding.VideosItemLayoutBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.GameHolder> {
    public Context context;
    public List<MatchesBean.VideosBean> list;
    OnMatchListener mListener;
    String half = "";

    public VideosAdapter(Context context, List<MatchesBean.VideosBean> list, OnMatchListener mListener) {
        this.context = context;
        this.mListener = mListener;
        this.list = list;
    }

    public VideosAdapter(Context context, List<MatchesBean.VideosBean> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public GameHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
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
    public void updateVideo(ReactionsBean localBean, MatchesBean.VideosBean remoteBean) {
        if (localBean !=null && remoteBean !=null) {
            for (int index =0; index < list.size(); index++) {
                if (list.get(index).getLocal_id() == localBean.getId()) {
                    list.set(index, remoteBean);
                    notifyItemChanged(index);
                    break;
                }
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull GameHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public void onBindViewHolder(@NonNull VideosAdapter.GameHolder viewHolder, int position) {
        MatchesBean.VideosBean mBean = list.get(position);
        viewHolder.binding.txtHalf.setText((mBean.getHalf() == 1) ? context.getString(R.string.lbl_first_half) : context.getString(R.string.lbl_second_half));
        if (half.equalsIgnoreCase(mBean.getHalf() + "")) {
            viewHolder.binding.txtHalf.setVisibility(View.GONE);
        } else {
            viewHolder.binding.txtHalf.setVisibility(View.VISIBLE);
        }
        viewHolder.binding.imgDelete.setVisibility(View.VISIBLE);

        half = mBean.getHalf() + "";
        CommonMethods.setTextWithHtml(mBean.getTitle(), viewHolder.binding.txtTeam);
        viewHolder.binding.txtTime.setText(mBean.getTime());
        try {
            if(CommonMethods.isValidString(mBean.getLocal_video())) {
                viewHolder.binding.imgThumbnail.setImageBitmap(CommonMethods.createVideoThumb(context, Uri.fromFile(new File(mBean.getLocal_video()))));
                viewHolder.binding.imgDelete.setVisibility(View.GONE);
                viewHolder.binding.pbLoading.setVisibility(View.VISIBLE);
            } else {
                CommonMethods.loadImage(context, mBean.getThumbnail(), viewHolder.binding.imgThumbnail);
                viewHolder.binding.imgDelete.setVisibility(View.VISIBLE);
                viewHolder.binding.pbLoading.setVisibility(View.GONE);
            }
        } catch (Exception ex) {
            CommonMethods.loadImage(context, mBean.getThumbnail(), viewHolder.binding.imgThumbnail);
            ex.printStackTrace();
        }
    }

    public void deleteVideo(int position) {
        if (position >= 0 && list.size() > position) {
            list.remove(position);
            notifyItemRemoved(position);
        }
    }

    public int getItemCount() {
        return this.list.size();
    }

    public class GameHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        VideosItemLayoutBinding binding;

        public GameHolder(View view) {
            super(view);
            binding = DataBindingUtil.bind(view);
            view.setOnClickListener(this);
            binding.imgDelete.setOnClickListener(this);
        }

        public void onClick(View view) {
            if (mListener != null) {
                MatchesBean.VideosBean mBean = list.get(getBindingAdapterPosition());
                if (mBean != null) {
                    if (view.getId() == R.id.imgDelete) {
                        mBean.setIsDelete("1");
                        mListener.onVideoDelete(getBindingAdapterPosition(), mBean);
                    }else {
                        mListener.onVideoClick(mBean);
                    }
                }
            }
        }
    }
}
