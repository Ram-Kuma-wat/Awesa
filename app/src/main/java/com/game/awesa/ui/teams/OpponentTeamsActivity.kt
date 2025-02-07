package com.game.awesa.ui.teams

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codersworld.awesalibs.beans.matches.MatchesBean
import com.codersworld.awesalibs.beans.teams.TeamsBean
import com.codersworld.awesalibs.listeners.OnConfirmListener
import com.codersworld.awesalibs.listeners.OnResponse
import com.codersworld.awesalibs.listeners.OnTeamsListener
import com.codersworld.awesalibs.rest.ApiCall
import com.codersworld.awesalibs.rest.UniversalObject
import com.codersworld.awesalibs.storage.UserSessions
import com.codersworld.awesalibs.utils.CommonMethods
import com.codersworld.awesalibs.utils.Logs
import com.codersworld.awesalibs.utils.Tags
import com.game.awesa.R
import com.game.awesa.databinding.ActivityOpponentTeamBinding
import com.game.awesa.ui.BaseActivity
import com.game.awesa.ui.LoginActivity
import com.game.awesa.ui.dashboard.MainActivity
import com.game.awesa.ui.recorder.TutorialActivity
import com.game.awesa.utils.Global


class OpponentTeamsActivity : BaseActivity() ,OnClickListener, OnConfirmListener,
    OnResponse<UniversalObject>, OnTeamsListener {
        companion object {
            val TAG: String = OpponentTeamsActivity::class.java.simpleName
        }
    lateinit var binding: ActivityOpponentTeamBinding
    private var locationType = "1"
    private var mApiCall: ApiCall? = null
    private var gameCategory = ""
    private var county = ""
    private var league = ""
    private var teamId = ""
    private var opponentTeamId = ""
    private var mAdapter :TeamsAdapter?=null
    private var mListTeams: ArrayList<TeamsBean.InfoBean> = ArrayList()
    private var mMatchBean: MatchesBean.InfoBean? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_opponent_team)
        initApiCall()
        binding.imgHome.setOnClickListener { CommonMethods.moveWithClear(this@OpponentTeamsActivity,
            MainActivity::class.java) }

        binding.btnAway.setOnClickListener(this)
        binding.btnHome.setOnClickListener(this)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.btnStart.setOnClickListener(this)
        binding.rvTeams.layoutManager = LinearLayoutManager(this@OpponentTeamsActivity,RecyclerView.VERTICAL,false)
        setData(mListTeams)
        if (intent.hasExtra("county")) {
            county = intent.getStringExtra("county") as String
        }
        if (intent.hasExtra("league")) {
            league = intent.getStringExtra("league") as String
        }
        if (intent.hasExtra("team_id")) {
            teamId = intent.getStringExtra("team_id") as String
        }
        if (intent.hasExtra("game_category")) {
            gameCategory = intent.getStringExtra("game_category") as String
            getTeams("")
        }
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                mAdapter!!.filter(charSequence.toString().lowercase())
            }

            override fun afterTextChanged(editable: Editable) {}
        })    }

    private fun makeSelection(v1:Button, v2:Button){
        v1.setBackgroundResource(R.drawable.selected_bg)
        v2.setBackgroundResource(R.drawable.unselected_bg)
        v1.setTextColor(ContextCompat.getColor(this,R.color.white))
        v2.setTextColor(ContextCompat.getColor(this,R.color.black))
    }
    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.btnHome-> {
                locationType="1"
                makeSelection(binding.btnHome,binding.btnAway)
            }
            R.id.btnAway-> {
                locationType="2"
                makeSelection(binding.btnAway,binding.btnHome)
            }
            R.id.btnStart-> {
                if(CommonMethods.isValidString(opponentTeamId)){
                    if(hasStoragePermission()) {
                        createMatch("","","");
                    }
                }else{
                    errorMsg(getString(R.string.error_select_opponent_team))
                }
            }
        }
    }

    @Suppress("ComplexCondition")
    private fun hasStoragePermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this@OpponentTeamsActivity,Manifest.permission.READ_MEDIA_IMAGES
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    this@OpponentTeamsActivity, Manifest.permission.READ_MEDIA_VIDEO
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    this@OpponentTeamsActivity, Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    this@OpponentTeamsActivity, Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestMultiplePermissions.launch(
                    arrayOf(
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                    )
                )
                return false
            }
        } else {
            if (ActivityCompat.checkSelfPermission(
                    this@OpponentTeamsActivity, Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    this@OpponentTeamsActivity, Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    this@OpponentTeamsActivity, Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestMultiplePermissions.launch(
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                    )
                )
                return false
            }
        }
        return true
    }

    private val requestMultiplePermissions =  registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.entries.forEach {
            Log.d("DEBUG", "${it.key} = ${it.value}")
        }
    }

    private fun setData(mList:ArrayList<TeamsBean.InfoBean>) {
        mAdapter = TeamsAdapter(this@OpponentTeamsActivity,mList,this,1)
        binding.rvTeams.adapter = mAdapter
     }
    fun initApiCall() {
        if (mApiCall == null) {
            mApiCall = ApiCall(this@OpponentTeamsActivity)
        }
    }

    override fun onConfirm(isTrue: Boolean, type: String) {
        if (isTrue){
            if (type == "99") {
                UserSessions.clearUserInfo(this@OpponentTeamsActivity)
                val intent = Intent(this@OpponentTeamsActivity, LoginActivity::class.java)
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(intent)
                finishAffinity()
            }
        }
    }
    private fun hideShow(strMsg: String, type: Int){
        binding.txtError.text = strMsg
        if (type == 1) {
            binding.llNoData.visibility = View.VISIBLE
            binding.rvTeams.visibility = View.GONE
            binding.llOther.visibility = View.GONE
        } else {
            binding.llNoData.visibility = View.GONE
            binding.rvTeams.visibility = View.VISIBLE
            binding.llOther.visibility = View.VISIBLE
        }
    }

    override fun onSuccess(response: UniversalObject) {
        try {
            Logs.e(response.methodName)
            when (response.methodName) {
                Tags.SB_OPPONENT_TEAMS_API -> {
                    val mTeamsBean: TeamsBean = response.response as TeamsBean
                    if (mTeamsBean.status == 1) {
                        mListTeams = mTeamsBean.info
                        setData(mListTeams)
                        //  moveToNext(mLoginBean.info)
                        hideShow("",0)
                    } else if (mTeamsBean.status == 99) {
                        UserSessions.clearUserInfo(this@OpponentTeamsActivity)
                        Global().makeConfirmation(mTeamsBean.msg, this@OpponentTeamsActivity, this)
                    } else if (CommonMethods.isValidString(mTeamsBean.msg)) {
                        hideShow(mTeamsBean.msg,1)
                    } else {
                        hideShow(getResources().getString(R.string.something_wrong),1);
                    }
                }
                Tags.SB_CREATE_MATCH_API -> {
                    var mMatchesBean: MatchesBean = response.response as MatchesBean
                    if (mMatchesBean.status == 1 && CommonMethods.isValidArrayList(mMatchesBean.info)) {
                        this.mMatchBean = mMatchesBean.info[0]
                        val intent = Intent(this@OpponentTeamsActivity, TutorialActivity::class.java)
                        intent.putExtra(TutorialActivity.EXTRA_MATCH_BEAN, mMatchBean)
                        startActivity(intent)
                    }else if (mMatchesBean.status == 99) {
                        UserSessions.clearUserInfo(this@OpponentTeamsActivity)
                        Global().makeConfirmation(mMatchesBean.msg, this@OpponentTeamsActivity, this)
                    }else if (CommonMethods.isValidString(mMatchesBean.msg)) {
                        errorMsg(mMatchesBean.msg);
                    } else {
                        errorMsg(getResources().getString(R.string.something_wrong));
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e(TAG, ex.localizedMessage, ex)
            errorMsg(getResources().getString(R.string.something_wrong));
        }

    }

    fun errorMsg(strMsg: String) {
        CommonMethods.errorDialog(
            this@OpponentTeamsActivity,
            strMsg,
            getResources().getString(R.string.app_name),
            getResources().getString(R.string.lbl_ok))
    }

    override fun onError(type: String, error: String) {
        if (type == Tags.SB_OPPONENT_TEAMS_API){
            hideShow(getResources().getString(R.string.something_wrong),1);
        }else {
            errorMsg(error)
        }
    }

    private fun getTeams(vararg strParams: String) {
        if (CommonMethods.isNetworkAvailable(this@OpponentTeamsActivity)) {
            mApiCall!!.getOpponentTeams(this, true,strParams[0],
                UserSessions.getUserInfo(this@OpponentTeamsActivity).id.toString(), gameCategory, teamId, league)
        } else {
            CommonMethods.errorDialog(
                this@OpponentTeamsActivity,
                getResources().getString(R.string.error_internet),
                getResources().getString(R.string.app_name),
                getResources().getString(R.string.lbl_ok)
            );
        }
    }

    private fun createMatch(vararg strParams: String) {
        if(mMatchBean !=null && mMatchBean!!.id>0){
            val intent = Intent(this@OpponentTeamsActivity, TutorialActivity::class.java)
            intent.putExtra(TutorialActivity.EXTRA_MATCH_BEAN, mMatchBean)
            startActivity(intent)
        }else{
            if (CommonMethods.isNetworkAvailable(this@OpponentTeamsActivity)) {
                mApiCall!!.createMatch(this, true,
                    UserSessions.getUserInfo(
                        this@OpponentTeamsActivity).id.toString(),
                    gameCategory,
                    county,
                    if (locationType == "1") teamId else opponentTeamId,
                    league,
                    if (locationType == "1") opponentTeamId else teamId,
                    locationType,
                    strParams[0],
                    strParams[1],
                    strParams[2]
                )
            } else {
                CommonMethods.errorDialog(
                    this@OpponentTeamsActivity,
                    getResources().getString(R.string.error_internet),
                    getResources().getString(R.string.app_name),
                    getResources().getString(R.string.lbl_ok)
                );
            }
        }
    }

    override fun onTeamSelection(mBeanTeam: TeamsBean.InfoBean) {
        opponentTeamId = mBeanTeam.id.toString()
     }
}
