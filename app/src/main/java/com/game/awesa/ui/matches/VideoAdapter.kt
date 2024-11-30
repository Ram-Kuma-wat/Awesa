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
import com.codersworld.awesalibs.beans.matches.MatchesBean.VideosBean
import com.codersworld.awesalibs.listeners.OnMatchListener
import com.codersworld.awesalibs.utils.CommonMethods
import com.game.awesa.R
import com.game.awesa.databinding.ItemVideoBinding
import java.io.File

class VideoAdapter(onActonActionMatchListener: OnMatchListener) :
    ListAdapter<VideosBean, VideoAdapter.GameHolder>(VideoDiffCallback()) {
    var context: Context? = null
    var mListener: OnMatchListener? = onActonActionMatchListener
    var half: String = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameHolder {
        val binding = ItemVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GameHolder(binding, parent.context)
    }

    override fun onBindViewHolder(holder: GameHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class GameHolder(
        private val binding: ItemVideoBinding,
        private val context: Context) : RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {
        lateinit var video: VideosBean

        init {
            binding.root.setOnClickListener(this)
            binding.imgDelete.setOnClickListener(this)
        }

        fun bind(video: VideosBean) {
            this.video = video
            binding.txtHalf.text = if ((video.half == 1)) {
                context.getString(R.string.lbl_first_half)
            } else {
                context.getString(
                    R.string.lbl_second_half
                )
            }

            if (half.equals(video.half.toString(), ignoreCase = true)) {
                binding.txtHalf.visibility = View.GONE
            } else {
                binding.txtHalf.visibility = View.VISIBLE
            }
            binding.imgDelete.setVisibility(View.VISIBLE)

            half = video.half.toString()
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

        override fun onClick(view: View) {
            if (mListener != null) {
                val mBean = getItem(bindingAdapterPosition)
                if (view.id == R.id.imgDelete) {
                    mBean.isDelete = "1"
                    mListener!!.onVideoDelete(bindingAdapterPosition, mBean)
                } else {
                    mListener!!.onVideoClick(mBean)
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
        return oldItem.local_id == (newItem.local_id) &&
                oldItem.half == (newItem.half) &&
                oldItem.match_id == newItem.match_id &&
                oldItem.reaction == newItem.reaction &&
                oldItem.time == newItem.time &&
                oldItem.video == newItem.video
    }

    override fun getChangePayload(oldItem: VideosBean, newItem: VideosBean): VideosBean {
        return newItem
    }
}