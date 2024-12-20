package com.game.awesa.ui.matches;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.codersworld.awesalibs.beans.matches.MatchesBean;
import com.codersworld.awesalibs.beans.matches.ReactionsBean;
import com.codersworld.awesalibs.listeners.OnMatchListener;
import com.codersworld.awesalibs.utils.CommonMethods;
import com.game.awesa.R;
import com.game.awesa.databinding.ItemVideoBinding;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class VideosAdapter extends ListAdapter<MatchesBean.VideosBean, VideosAdapter.GameHolder> {
    public Context context;
    public List<MatchesBean.VideosBean> list;
    OnMatchListener mListener;
    String half = "";

    protected VideosAdapter(@NonNull DiffUtil.ItemCallback<MatchesBean.VideosBean> diffCallback) {
        super(diffCallback);
    }

//    public VideosAdapter(Context context, List<MatchesBean.VideosBean> list, OnMatchListener mListener) {
//        this.context = context;
//        this.mListener = mListener;
//        this.list = list;
//    }
//
//    public VideosAdapter(Context context, List<MatchesBean.VideosBean> list) {
//        this.context = context;
//        this.list = list;
//    }

    @NonNull
    @Override
    public GameHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View binding = LayoutInflater.from(this.context).inflate(R.layout.item_video, viewGroup, false);
        return new GameHolder(binding);
    }

    public void addAll(ArrayList<MatchesBean.VideosBean> mList) {
        list = mList;
        notifyItemRangeInserted(0, list.size());
    }

    public void addOne(ArrayList<MatchesBean.VideosBean> mList) {
        if (CommonMethods.isValidArrayList(mList)) {
            list.addAll(mList);
            notifyDataSetChanged();
        }
    }

    public void updateVideos(List<MatchesBean.VideosBean> newVideos) {
//        this.list.addAll(0, newVideos);
//        notifyItemRangeInserted(0, newVideos.size());

        final VideosCallback diffCallBack = new VideosCallback(this.list, newVideos);
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallBack);
//        if(this.list.isEmpty()) {
//            this.list = newVideos;
//        } else {
//            this.list.addAll(0, newVideos);
//        }

        this.list.clear();
        this.list.addAll(newVideos);

        diffResult.dispatchUpdatesTo(this);
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
            Log.e("VideosAdapter", ex.getLocalizedMessage(), ex);
        }
    }

    public void deleteVideo(int position) {
        if (position >= 0 && list.size() > position) {
            list.remove(position);
            notifyItemRemoved(position);
        }
    }

//    @Override
//    public int getItemViewType(int position) {
//        return super.getItemViewType(position);
//    }

//    public int getItemCount() {
//        return this.list.size();
//    }

    public class GameHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ItemVideoBinding binding;

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
//                        mListener.onVideoDelete(getBindingAdapterPosition(), mBean);
                    }else {
                        mListener.onVideoClick(mBean);
                    }
                }
            }
        }
    }
}

class VideosCallback extends DiffUtil.Callback {
    List<MatchesBean.VideosBean> oldList;
    List<MatchesBean.VideosBean> newList;

    public VideosCallback(List<MatchesBean.VideosBean> oldList, List<MatchesBean.VideosBean> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).getLocal_id() == newList.get(
                newItemPosition).getLocal_id();
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        final MatchesBean.VideosBean oldVideo = oldList.get(oldItemPosition);
        final MatchesBean.VideosBean newVideo = newList.get(newItemPosition);

        return Objects.equals(oldVideo.getVideo(), newVideo.getVideo()) &&
                oldVideo.getLocal_id() == (newVideo.getLocal_id()) &&
                oldVideo.getHalf() == (newVideo.getHalf()) &&
                oldVideo.getVideo().equals(newVideo.getVideo()) &&
                oldVideo.getMatch_id() == newVideo.getMatch_id() &&
                oldVideo.getReaction().equals(newVideo.getReaction()) &&
                oldVideo.getThumbnail().equals(newVideo.getThumbnail()) &&
                oldVideo.getTime().equals(newVideo.getTime());
    }

    @Nullable
    @Override
    public MatchesBean.VideosBean getChangePayload(int oldItemPosition, int newItemPosition) {
        return newList.get(newItemPosition);
    }
}
