package com.game.awesa.ui.recorder


import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.MediaController
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.codersworld.awesalibs.beans.matches.MatchesBean
import com.codersworld.awesalibs.database.DatabaseHelper
import com.codersworld.awesalibs.database.DatabaseManager
import com.codersworld.awesalibs.utils.CommonMethods
import com.game.awesa.R
import com.game.awesa.databinding.ActivityVideoPreviewBinding
import com.game.awesa.ui.MyApp
import com.google.android.exoplayer2.MediaItem
import java.io.File
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.CacheKeyFactory
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util

class VideoPreviewActivity : AppCompatActivity() {
    lateinit var binding: ActivityVideoPreviewBinding

    var strPath = "";

    // private val videoView: VideoView by lazy { findViewById<VideoView>(R.id.videoView) }
    lateinit var mBeanVideo: MatchesBean.VideosBean;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_preview)
        //  setContentView(R.layout.activity_video_preview)
        DatabaseManager.initializeInstance(DatabaseHelper(applicationContext))
        if (intent.hasExtra("mBeanVideo")) {
            mBeanVideo = CommonMethods.getSerializable(
                intent,
                "mBeanVideo",
                MatchesBean.VideosBean::class.java
            ) as MatchesBean.VideosBean
            videoUrl = mBeanVideo.video
            binding.videoView.visibility = View.GONE
            binding.mPlayer.visibility = View.VISIBLE
            initPlayer()
            /*
              val uri: Uri = Uri.parse(mBeanVideo.video)
              binding.videoView.setVideoURI(uri)
              val mediaController = MediaController(this)
              mediaController.setAnchorView(binding.videoView)
              mediaController.setMediaPlayer(binding.videoView)
              binding.videoView.setMediaController(mediaController)
              binding.videoView.start()
  */
        } else if (intent.hasExtra("strPath")) {
            strPath = intent.getStringExtra("strPath") as String
            binding.videoView.visibility = View.VISIBLE
            binding.mPlayer.visibility = View.GONE
            initVideoView(strPath)
        } else {
            finish()
        }
        binding.imgBack.setOnClickListener {
            finish()
        }
    }

    override fun onBackPressed() {
        try {
            if (binding.videoView != null && binding.videoView.isPlaying) {
                binding.videoView.pause()
                binding.videoView.stopPlayback()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        try {
            if (simpleExoPlayer != null && simpleExoPlayer.isPlaying) {
                simpleExoPlayer.stop()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        super.onBackPressed()
    }

    fun initVideoView(video_path: String) {
        val controller = MediaController(this)
        controller.setAnchorView(binding.videoView)
        controller.setMediaPlayer(binding.videoView)
        binding.videoView.setMediaController(controller)
        binding.videoView.setVideoURI(Uri.fromFile(File(video_path)))
        binding.videoView.setOnPreparedListener { mp ->
            val lp = binding.videoView.layoutParams
            val videoWidth = mp.videoWidth.toFloat()
            val videoHeight = mp.videoHeight.toFloat()
            val viewWidth = binding.videoView.width.toFloat()
            lp.height = (viewWidth * (videoHeight / videoWidth)).toInt()
            binding.videoView.layoutParams = lp
            playVideo()
            /*  if (result.isSnapshot) {
                  // Log the real size for debugging reason.
                  Log.e("VideoPreview", "The video full size is " + videoWidth + "x" + videoHeight)
              }*/
        }
    }

    private var videoUrl: String? = null
    private lateinit var httpDataSourceFactory: HttpDataSource.Factory
    private lateinit var defaultDataSourceFactory: DefaultDataSourceFactory
    private lateinit var cacheDataSourceFactory: DataSource.Factory
    private lateinit var simpleExoPlayer: SimpleExoPlayer
    private val simpleCache: SimpleCache = MyApp.simpleCache
    private fun initPlayer() {
        httpDataSourceFactory = DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true)
        defaultDataSourceFactory =
            DefaultDataSourceFactory(this@VideoPreviewActivity, httpDataSourceFactory)
        cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(simpleCache)
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        simpleExoPlayer = SimpleExoPlayer.Builder(this@VideoPreviewActivity)
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
    }

    fun playVideo() {
        if (!binding.videoView.isPlaying) {
            binding.videoView.start()
        }
    }

    companion object {
        private const val URL =
            "https://sportapp.boonoserver.de/public/uploads/videos/22Aug2023/m_50_a_1_h_5.mp4"
    }
}