package com.game.awesa.ui.matches

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.codersworld.awesalibs.beans.CommonBean
import com.codersworld.awesalibs.beans.matches.MatchesBean.VideosBean
import com.codersworld.awesalibs.beans.matches.ReactionsBean
import com.codersworld.awesalibs.listeners.OnDeleteVideoListener
import com.codersworld.awesalibs.listeners.OnMatchListener
import com.codersworld.awesalibs.utils.CommonMethods
import com.game.awesa.R
import com.game.awesa.databinding.ItemHeaderBinding
import com.game.awesa.databinding.ItemInterviewBinding
import com.game.awesa.databinding.ItemVideoBinding
import java.io.File

class VideoAdapter(
    onActonActionMatchListener: OnMatchListener,
    onDeleteVideoListener: OnDeleteVideoListener,
) : ListAdapter<VideosBean, VideoAdapter.VideoRecyclerViewHolder>(VideoDiffCallback()) {

    companion object {
        val TAG: String = VideoAdapter::class.java.simpleName
    }

    var context: Context? = null
    private var onClickListener: OnMatchListener? = onActonActionMatchListener
    private var onDeleteVideoListener: OnDeleteVideoListener? = onDeleteVideoListener

    public fun onUpdateItem(position: Int, video: CommonBean) {
        getItem(position).video=video.videos.video
        getItem(position).thumbnail=video.videos.thumbnail
        getItem(position).id=video.videos.id
        getItem(position).local_id=video.videos.local_id
        getItem(position).isUploading="0"
        notifyItemChanged(position)
    }
    public fun hideProgress(position: Int) {
        getItem(position).isUploading="0"
        notifyItemChanged(position)
    }
    public fun onActionUpdate(position: Int) {
        notifyItemChanged(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoRecyclerViewHolder {
        return when (viewType) {
            R.layout.item_video -> VideoRecyclerViewHolder.GameHolder(
                ItemVideoBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent, false
                ),
                onClickListener = onClickListener,
                onDeleteVideoListener = onDeleteVideoListener,
                parent.context, this
            )

            R.layout.item_header -> VideoRecyclerViewHolder.HeaderHolder(
                ItemHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent, false
                ),
                parent.context
            )

            R.layout.item_interview -> VideoRecyclerViewHolder.InterviewHolder(
                ItemInterviewBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent, false
                ),
                onClickListener = onClickListener,
                onDeleteVideoListener = onDeleteVideoListener,
                parent.context, this
            )

            else -> throw IllegalArgumentException("Invalid ViewType Provided")
        }
    }

    @Suppress("MagicNumber")
    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)

        return when (item.local_id) {
            "-1", "-2", "-3", "-4" -> R.layout.item_header
            Int.MAX_VALUE.toString() -> R.layout.item_interview
            else -> R.layout.item_video
        }
    }

    override fun onBindViewHolder(holder: VideoRecyclerViewHolder, position: Int) {
        when (holder) {
            is VideoRecyclerViewHolder.GameHolder -> holder.bind(getItem(position))
            is VideoRecyclerViewHolder.HeaderHolder -> holder.bind(getItem(position))
            is VideoRecyclerViewHolder.InterviewHolder -> holder.bind(getItem(position))
        }
    }

    sealed class VideoRecyclerViewHolder(binding: ViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        class HeaderHolder(
            private val binding: ItemHeaderBinding,
            private val context: Context
        ) : VideoRecyclerViewHolder(binding) {

            @Suppress("MagicNumber")
            fun bind(video: VideosBean) {
                binding.header.text = when (video.local_id) {
                    "-1" -> context.getString(R.string.lbl_first_half)
                    "-2" -> context.getString(R.string.lbl_second_half)
                    "-3" -> context.getString(R.string.lbl_extratime)
                    "-4" -> context.getString(R.string.lbl_interview1)
                    else -> null
                }
            }
        }

        class GameHolder(
            private val binding: ItemVideoBinding,
            private val onClickListener: OnMatchListener?,
            private val onDeleteVideoListener: OnDeleteVideoListener?,
            private val context: Context,
            private val adapter: VideoAdapter // 👈 added this
        ) : VideoRecyclerViewHolder(binding) {
            lateinit var video: VideosBean

            init {
                binding.txtHalf.visibility = View.GONE
                binding.root.setOnClickListener {
                    val mBean = this.video
                    onClickListener?.onVideoClick(mBean)
                }
                binding.imgDelete.setOnClickListener {
                    val mBean = this.video
                    mBean.isDelete = "1"
                    onDeleteVideoListener?.onActionDelete(bindingAdapterPosition, mBean)
                }
                //
                binding.imgUpload.setOnClickListener {
                    val mBean = this.video
                    mBean.isUploading = "1"
                    adapter.onActionUpdate(bindingAdapterPosition)
                    onDeleteVideoListener?.onActionUpload(bindingAdapterPosition, mBean)
                }
            }

            fun bind(video: VideosBean) {
                this.video = video
                binding.imgDelete.setVisibility(View.VISIBLE)

                CommonMethods.setTextWithHtml(video.title, binding.txtTeam)
                binding.txtTime.text = video.time
                try {
                    if (video.video.isNullOrEmpty()) {
                        binding.imgThumbnail.setImageBitmap(
                            CommonMethods.createVideoThumb(
                                context, Uri.fromFile(
                                    File(video.local_video)
                                )
                            )
                        )
                        binding.imgDelete.setVisibility(View.GONE)
                        binding.imgUpload.setVisibility(View.GONE)
                        binding.pbLoading.visibility = View.VISIBLE
                        if (video.upload_type==1){
                            binding.pbLoading.visibility = View.GONE
                            binding.imgUpload.visibility = View.VISIBLE
                        }else{
                            if (CommonMethods.isValidString(video.created_date) && CommonMethods.getTimeDifferenceInHours(
                                    video.created_date,"yyyy-MM-dd HH:mm:ss") > 0.5) {
                                binding.pbLoading.visibility = View.GONE
                                binding.imgUpload.visibility = View.VISIBLE
                            }
                        }
                    } else {
                        CommonMethods.loadImage(context, video.thumbnail, binding.imgThumbnail)
                        binding.imgDelete.setVisibility(View.VISIBLE)
                        binding.imgUpload.setVisibility(View.GONE)
                        binding.pbLoading.visibility = View.GONE
                    }
                } catch (ex: Exception) {
                    CommonMethods.loadImage(context, video.thumbnail, binding.imgThumbnail)
                    Log.e(TAG, ex.localizedMessage, ex)
                }
                if (CommonMethods.isValidString(video.isUploading) && video.isUploading.equals("1")) {
                    binding.imgDelete.setVisibility(View.GONE)
                    binding.imgUpload.setVisibility(View.GONE)
                    binding.pbLoading.visibility = View.VISIBLE
                }

            }
        }

        class InterviewHolder(
            private val binding: ItemInterviewBinding,
            private val onClickListener: OnMatchListener?,
            private val onDeleteVideoListener: OnDeleteVideoListener?,
            private val context: Context,
            private val adapter: VideoAdapter // 👈 added this
        ) : VideoRecyclerViewHolder(binding) {
            lateinit var video: VideosBean

            init {
                binding.txtHalf.visibility = View.GONE
                binding.root.setOnClickListener {
                    val mBean = this.video
                    onClickListener?.onVideoClick(mBean)
                }
                binding.imgDelete.setOnClickListener {
                    val mBean = this.video
                    mBean.isDelete = "1"
                    onDeleteVideoListener?.onInterviewDelete(bindingAdapterPosition, mBean)
                }
                binding.imgUpload.setOnClickListener {
                    val mBean = this.video
                    mBean.isUploading = "1"
                    adapter.onActionUpdate(bindingAdapterPosition)
                    onDeleteVideoListener?.onInterviewUpload(bindingAdapterPosition, mBean)
                }
            }

            fun bind(video: VideosBean) {
                this.video = video
                binding.imgDelete.setVisibility(View.VISIBLE)
                binding.txtTeam.text = context.getString(R.string.lbl_interview1)
                try {
                    if (CommonMethods.isValidString(video.local_video)) {
                        binding.imgThumbnail.setImageBitmap(
                            CommonMethods.createVideoThumb(
                                context, Uri.fromFile(
                                    File(video.local_video)
                                )
                            )
                        )
                        binding.imgDelete.setVisibility(View.GONE)
                        binding.imgUpload.setVisibility(View.GONE)
                        binding.pbLoading.visibility = View.VISIBLE
                        if (video.upload_type==1){
                            binding.pbLoading.visibility = View.GONE
                            binding.imgUpload.visibility = View.VISIBLE
                        }else{
                            if (CommonMethods.isValidString(video.created_date) && CommonMethods.getTimeDifferenceInHours(
                                    video.created_date,"yyyy-MM-dd HH:mm:ss") > 0.5) {
                                binding.pbLoading.visibility = View.GONE
                                binding.imgUpload.visibility = View.VISIBLE
                            }
                        }
/*
                        if (CommonMethods.isValidString(video.created_date) && CommonMethods.getTimeDifferenceInHours(
                                video.created_date
                            ,"yyyy/MM/dd HH:mm:ss") > 3
                        ) {
                            binding.pbLoading.visibility = View.GONE
                            binding.imgUpload.visibility = View.VISIBLE
                        }
*/
                    } else {
                        CommonMethods.loadImage(context, video.thumbnail, binding.imgThumbnail)
                        binding.imgDelete.setVisibility(View.VISIBLE)
                        binding.imgUpload.setVisibility(View.GONE)
                        binding.pbLoading.visibility = View.GONE
                    }
                    if (CommonMethods.isValidString(video.isUploading) && video.isUploading.equals("1")) {
                        binding.imgDelete.setVisibility(View.GONE)
                        binding.imgUpload.setVisibility(View.GONE)
                        binding.pbLoading.visibility = View.VISIBLE
                    }
                } catch (ex: Exception) {
                    CommonMethods.loadImage(context, video.thumbnail, binding.imgThumbnail)
                    Log.e(TAG, ex.localizedMessage, ex)
                }

            }
        }

    }
}

class VideoDiffCallback : DiffUtil.ItemCallback<VideosBean?>() {
    override fun areItemsTheSame(oldItem: VideosBean, newItem: VideosBean): Boolean {
        return oldItem.local_id == newItem.local_id
    }

    override fun areContentsTheSame(oldItem: VideosBean, newItem: VideosBean): Boolean {
        return oldItem.local_id == newItem.local_id &&
                oldItem.half == newItem.half &&
                oldItem.match_id == newItem.match_id &&
                oldItem.reaction == newItem.reaction &&
                oldItem.time == newItem.time &&
                oldItem.thumbnail == newItem.thumbnail &&
                oldItem.video == newItem.video
    }

    override fun getChangePayload(oldItem: VideosBean, newItem: VideosBean): VideosBean {
        return newItem
    }
}