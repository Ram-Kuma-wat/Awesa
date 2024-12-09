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
import com.codersworld.awesalibs.beans.matches.MatchesBean.VideosBean
import com.codersworld.awesalibs.listeners.OnDeleteVideoListener
import com.codersworld.awesalibs.listeners.OnMatchListener
import com.codersworld.awesalibs.utils.CommonMethods
import com.game.awesa.R
import com.game.awesa.databinding.ItemHeaderBinding
import com.game.awesa.databinding.ItemInterviewBinding
import com.game.awesa.databinding.ItemVideoBinding
import java.io.File

class VideoAdapter(onActonActionMatchListener: OnMatchListener, onDeleteVideoListener: OnDeleteVideoListener) :
    ListAdapter<VideosBean, VideoAdapter.VideoRecyclerViewHolder>(VideoDiffCallback()) {
    var context: Context? = null
    private var onClickListener: OnMatchListener? = onActonActionMatchListener
    private var onDeleteVideoListener: OnDeleteVideoListener? = onDeleteVideoListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoRecyclerViewHolder {
        return when(viewType) {
            R.layout.item_video -> VideoRecyclerViewHolder.GameHolder(
                ItemVideoBinding.inflate(LayoutInflater.from(parent.context),
                    parent, false),
                onClickListener = onClickListener,
                onDeleteVideoListener = onDeleteVideoListener,
                parent.context)
            R.layout.item_header -> VideoRecyclerViewHolder.HeaderHolder(
                ItemHeaderBinding.inflate(LayoutInflater.from(parent.context),
                    parent, false),
                parent.context)
            R.layout.item_interview -> VideoRecyclerViewHolder.InterviewHolder(
                ItemInterviewBinding.inflate(LayoutInflater.from(parent.context),
                    parent, false),
                onClickListener = onClickListener,
                onDeleteVideoListener = onDeleteVideoListener,
                parent.context)
            else -> throw IllegalArgumentException("Invalid ViewType Provided")
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)

        return when (item.local_id) {
            -1, -2, -3 -> R.layout.item_header
            Int.MAX_VALUE -> R.layout.item_interview
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
            private val context: Context) : VideoRecyclerViewHolder(binding) {
            init {}

            fun bind(video: VideosBean) {
                binding.header.text = when(video.local_id) {
                    -1 -> context.getString(R.string.lbl_first_half)
                    -2 -> context.getString(R.string.lbl_second_half)
                    -3 -> context.getString(R.string.lbl_interview1)
                    else -> null
                }
            }
        }

        class GameHolder(
            private val binding: ItemVideoBinding,
            private val onClickListener: OnMatchListener?,
            private val onDeleteVideoListener: OnDeleteVideoListener?,
            private val context: Context) : VideoRecyclerViewHolder(binding) {
            lateinit var video: VideosBean

            init {
                binding.txtHalf.visibility = View.GONE
                binding.root.setOnClickListener {
                    val mBean =  this.video
                    onClickListener?.onVideoClick(mBean)
                }
                binding.imgDelete.setOnClickListener {
                    val mBean =  this.video
                    mBean.isDelete = "1"
                    onDeleteVideoListener?.onActionDelete(bindingAdapterPosition, mBean)
                }
            }

            fun bind(video: VideosBean) {
                this.video = video
                binding.imgDelete.setVisibility(View.VISIBLE)

                CommonMethods.setTextWithHtml(video.title, binding.txtTeam)
                binding.txtTime.text = video.time
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
                        binding.pbLoading.visibility = View.VISIBLE
                    } else {
                        CommonMethods.loadImage(context, video.thumbnail, binding.imgThumbnail)
                        binding.imgDelete.setVisibility(View.VISIBLE)
                        binding.pbLoading.visibility = View.GONE
                    }
                } catch (ex: Exception) {
                    CommonMethods.loadImage(context, video.thumbnail, binding.imgThumbnail)
                    Log.e("VideosAdapter", ex.localizedMessage, ex)
                }

            }
        }

        class InterviewHolder(
            private val binding: ItemInterviewBinding,
            private val onClickListener: OnMatchListener?,
            private val onDeleteVideoListener: OnDeleteVideoListener?,
            private val context: Context) : VideoRecyclerViewHolder(binding) {
            lateinit var video: VideosBean

            init {
                binding.txtHalf.visibility = View.GONE
                binding.root.setOnClickListener {
                    val mBean =  this.video
                    onClickListener?.onVideoClick(mBean)
                }
                binding.imgDelete.setOnClickListener {
                    val mBean =  this.video
                    mBean.isDelete = "1"
                    onDeleteVideoListener?.onInterviewDelete(bindingAdapterPosition, mBean)
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
                        binding.pbLoading.visibility = View.VISIBLE
                    } else {
                        CommonMethods.loadImage(context, video.thumbnail, binding.imgThumbnail)
                        binding.imgDelete.setVisibility(View.VISIBLE)
                        binding.pbLoading.visibility = View.GONE
                    }
                } catch (ex: Exception) {
                    CommonMethods.loadImage(context, video.thumbnail, binding.imgThumbnail)
                    Log.e("VideosAdapter", ex.localizedMessage, ex)
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