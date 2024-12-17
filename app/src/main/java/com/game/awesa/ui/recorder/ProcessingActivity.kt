package com.game.awesa.ui.recorder

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.media3.common.util.UnstableApi
import com.codersworld.awesalibs.beans.matches.InterviewBean
import com.codersworld.awesalibs.beans.matches.MatchesBean
import com.codersworld.awesalibs.database.DatabaseManager
import com.codersworld.awesalibs.database.dao.InterviewsDAO
import com.codersworld.awesalibs.utils.CommonMethods
import com.game.awesa.R
import com.game.awesa.databinding.ActivitySplashBinding
import com.game.awesa.services.TrimService
import com.game.awesa.utils.Media3Transformer
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class ProcessingActivity : AppCompatActivity() {

    @Inject
    lateinit var media3Transformer: Media3Transformer

    companion object {
        const val EXTRA_MATCH_BEAN = "mMatchBean"
        const val SECOND_IN_MS = 1000L
        val TAG: String = ProcessingActivity::class.java.simpleName
    }

    @Inject lateinit var databaseManager: DatabaseManager
    lateinit var binding: ActivitySplashBinding
    var mTimer: CountDownTimer? = null

    var mMatchBean: MatchesBean.InfoBean? = null

    @Suppress("MagicNumber")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash)
        binding.img.visibility = View.GONE
        binding.llProgress.visibility = View.VISIBLE

        if (intent.hasExtra(EXTRA_MATCH_BEAN)) {
            mMatchBean = CommonMethods.getSerializable(intent, EXTRA_MATCH_BEAN, MatchesBean.InfoBean::class.java)
            CommonMethods.checkTrimServiceWithData(
                this,
                TrimService::class.java,
                mMatchBean!!.id.toString()
            )

            databaseManager.executeQuery {
                val mInterviewsDAO = InterviewsDAO(it, this)
                val mList =
                    mInterviewsDAO.selectAll(mMatchBean!!.id.toString()) as ArrayList<InterviewBean>
                databaseManager.closeDatabase()
                if (CommonMethods.isValidArrayList(mList)) {
                    val timeStamp =
                        SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date())
                    var mediaStorageDir: File? = null
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        mediaStorageDir = File(
                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                            "Awesa/interview/$timeStamp"
                        )
                        if (!mediaStorageDir.exists()) {
                            if (!mediaStorageDir.mkdirs()) {}
                        }
                    } else {
                        mediaStorageDir = getExternalFilesDir("Awesa/interview/$timeStamp")
                        mediaStorageDir!!.mkdirs()
                    }
                    if (!mediaStorageDir.exists()) {
                        if (!mediaStorageDir.mkdirs()) {
                            Toast.makeText(
                                applicationContext,
                                "Please Allow Storage Permission.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    // Locate the source file
                    val sourceFile: File = File(mList[0].video)
                    // Ensure the destination directory exists
                    val destDir: File = File(mediaStorageDir.absolutePath)
                    if (!destDir.exists()) {
                        destDir.mkdirs() // Create the directory if it does not exist
                    }

                    // Define the destination file path
                    val destFile: File = File(destDir, sourceFile.getName())

                    // Move the file
                    val success: Boolean = sourceFile.renameTo(destFile)

                    val fileName = destFile.toString()
                        .substring(
                            destFile.toString().lastIndexOf('/') + 1,
                            destFile.toString().length
                        )
                    mInterviewsDAO.updateVideo(fileName, destFile.toString(), mList[0].id)
                }
            }
        }

        mTimer = object : CountDownTimer(2500, SECOND_IN_MS) { // 2.5 seconds
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                mTimer!!.cancel()
                val intent = Intent(this@ProcessingActivity, MatchOverviewActivity::class.java)
                intent.putExtra(MatchOverviewActivity.EXTRA_MATCH_BEAN, mMatchBean)
                startActivity(intent)
                finish()

            }
        }.start()
    }
}
