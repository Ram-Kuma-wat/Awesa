package com.game.awesa.ui.recorder

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.MediaController
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.FileDataSource
import androidx.media3.datasource.HttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.extractor.Extractor
import com.codersworld.awesalibs.beans.matches.MatchesBean
import com.codersworld.awesalibs.utils.CommonMethods
import com.game.awesa.R
import com.game.awesa.databinding.ActivityVideoPreviewBinding
import com.game.awesa.ui.Awesa
import java.io.File


@UnstableApi
class VideoPreviewActivity : AppCompatActivity() {
    companion object {
        private const val URL =
            "https://sportapp.boonoserver.de/public/uploads/videos/22Aug2023/m_50_a_1_h_5.mp4"
        const val EXTRA_BEAN_VIDEO = "mBeanVideo"
        const val EXTRA_VIDEO_PATH = "strPath"
        var TAG: String = VideoPreviewActivity::class.java.simpleName
    }

    lateinit var binding: ActivityVideoPreviewBinding

    private var videoUrl: String? = null
    private lateinit var httpDataSourceFactory: HttpDataSource.Factory
    private lateinit var defaultDataSourceFactory: DefaultDataSource.Factory
    private lateinit var cacheDataSourceFactory: DataSource.Factory
    private lateinit var simpleExoPlayer: ExoPlayer
    private val simpleCache: SimpleCache = Awesa.simpleCache

    private lateinit var mBeanVideo: MatchesBean.VideosBean;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_preview)
        if (intent != null) {
            initPlayer()
        } else {
            finish()
        }
        binding.imgBack.setOnClickListener {
            finish()
        }

        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                try {
                    if (simpleExoPlayer.isPlaying) {
                        simpleExoPlayer.stop()
                    } else {
                        finish()
                    }
                } catch (ex: IllegalStateException) {
                    Log.e(TAG, ex.localizedMessage, ex)
                } catch (ex: UninitializedPropertyAccessException) {
                    Log.e(TAG, ex.localizedMessage, ex)
                }
            }
        })
    }

    private fun initPlayer() {
        if (intent.hasExtra(EXTRA_BEAN_VIDEO)) {
            mBeanVideo = CommonMethods.getSerializable(
                intent,
                EXTRA_BEAN_VIDEO,
                MatchesBean.VideosBean::class.java
            ) as MatchesBean.VideosBean
            videoUrl = mBeanVideo.video
            binding.videoView.visibility = View.GONE
            binding.mPlayer.visibility = View.VISIBLE

            httpDataSourceFactory = DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true)
            defaultDataSourceFactory =
                DefaultDataSource.Factory(this@VideoPreviewActivity, httpDataSourceFactory)
            cacheDataSourceFactory = CacheDataSource.Factory()
                .setCache(simpleCache)
                .setUpstreamDataSourceFactory(httpDataSourceFactory)
                .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

            simpleExoPlayer = ExoPlayer.Builder(this@VideoPreviewActivity)
                .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory)).build()

            val videoUri = Uri.parse(videoUrl)
            val mediaItem = MediaItem.fromUri(videoUri)
            val mediaSource =
                ProgressiveMediaSource.Factory(cacheDataSourceFactory).createMediaSource(mediaItem)

            binding.mPlayer.player = simpleExoPlayer
            simpleExoPlayer.playWhenReady = true
            simpleExoPlayer.seekTo(0, 0)
            simpleExoPlayer.repeatMode = Player.REPEAT_MODE_OFF
            simpleExoPlayer.setMediaSource(mediaSource, true)
            simpleExoPlayer.prepare()

        } else if (intent.hasExtra(EXTRA_VIDEO_PATH)) {
            val videoPath: String = intent.getStringExtra(EXTRA_VIDEO_PATH) as String
            val file = File(videoPath)
            val dataSpec = DataSpec(file.toUri())

            simpleExoPlayer = ExoPlayer.Builder(this@VideoPreviewActivity).build()

            try {
                val fileDataSource = FileDataSource()
                fileDataSource.open(dataSpec)

                val factory = DataSource.Factory { fileDataSource }
                val mediaitem = MediaItem.fromUri(fileDataSource.uri!!)
                val mediaSource: MediaSource = ProgressiveMediaSource.Factory(factory).createMediaSource(mediaitem)

                binding.mPlayer.player = simpleExoPlayer
                simpleExoPlayer.playWhenReady = true
                simpleExoPlayer.seekTo(0, 0)
                simpleExoPlayer.repeatMode = Player.REPEAT_MODE_OFF
                simpleExoPlayer.setMediaSource(mediaSource, true)
                simpleExoPlayer.setMediaSource(mediaSource)
                simpleExoPlayer.prepare()
            } catch (error: Exception) {
                Log.e(TAG, error.localizedMessage, error)
            }
        }
    }

    private fun releasePlayer() {
        if (binding.mPlayer.player != null) {
            binding.mPlayer.player?.release()
            binding.mPlayer.player = null
            binding.mPlayer.setPlayer(null)
        }
    }

    override fun onPause() {
        super.onPause()
        binding.mPlayer.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (binding.mPlayer.player == null) {
            initPlayer()
            binding.mPlayer.onResume()
        }
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    private fun playVideo() {
        if (!binding.videoView.isPlaying) {
            binding.videoView.start()
        }
    }
}
