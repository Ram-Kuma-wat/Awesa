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
import com.codersworld.awesalibs.database.DatabaseHelper
import com.codersworld.awesalibs.database.DatabaseManager
import com.codersworld.awesalibs.listeners.OnConfirmListener
import com.codersworld.awesalibs.listeners.OnResponse
import com.codersworld.awesalibs.listeners.OnTeamsListener
import com.codersworld.awesalibs.rest.ApiCall
import com.codersworld.awesalibs.rest.UniverSelObjct
import com.codersworld.awesalibs.storage.UserSessions
import com.codersworld.awesalibs.utils.CommonMethods
import com.codersworld.awesalibs.utils.Logs
import com.codersworld.awesalibs.utils.Tags
import com.game.awesa.R
import com.game.awesa.databinding.ActivityOpponentTeamBinding
import com.game.awesa.ui.BaseActivity
import com.game.awesa.ui.dashboard.MainActivity
import com.game.awesa.ui.recorder.CameraActivity
import com.game.awesa.ui.recorder.CameraActivityNew


class OpponentTeamsActivity : BaseActivity() ,OnClickListener, OnConfirmListener,
    OnResponse<UniverSelObjct>, OnTeamsListener {
    lateinit var binding: ActivityOpponentTeamBinding
    var location_type="1"
    var mApiCall: ApiCall? = null
    var game_category=""
    var county=""
    var league=""
    var team_id=""
    var opponent_team_id=""
    var mAdapter :TeamsAdapter?=null
    var mListTeams:ArrayList<TeamsBean.InfoBean> = ArrayList()
    var mMatchBean:MatchesBean.InfoBean? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_opponent_team)
        initApiCall()
        DatabaseManager.initializeInstance(DatabaseHelper(this@OpponentTeamsActivity))
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
            team_id = intent.getStringExtra("team_id") as String
        }
        if (intent.hasExtra("game_category")) {
            game_category = intent.getStringExtra("game_category") as String
            getTeams("")
        }
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
               /* var mListTeams1 : ArrayList<TeamsBean.InfoBean> =java.util.ArrayList()

                if (charSequence.length>0){
                    for (a in mListTeams.indices) {
                        if (mListTeams[a].title.lowercase().contains(charSequence.toString().lowercase())){
                            mListTeams1.add(mListTeams[a])
                        }
                    }
                }else{
                    mListTeams1 =mListTeams
                }*/
                mAdapter!!.filter(charSequence.toString().lowercase())
               // setData(mListTeams1)
            }

            override fun afterTextChanged(editable: Editable) {}
        })    }

    fun makeSelection(v1:Button,v2:Button){
        v1.setBackgroundResource(R.drawable.selected_bg)
        v2.setBackgroundResource(R.drawable.unselected_bg)
        v1.setTextColor(ContextCompat.getColor(this,R.color.white))
        v2.setTextColor(ContextCompat.getColor(this,R.color.black))
    }
    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.btnHome->{
                location_type="1"
                makeSelection(binding.btnHome,binding.btnAway)
            }
            R.id.btnAway->{
                location_type="2"
                makeSelection(binding.btnAway,binding.btnHome)
            }
            R.id.btnStart->{
                if(CommonMethods.isValidString(opponent_team_id)){
                    if(hasStoragePermission()!!) {
                        createMatch("","","");
                    }
                }else{
                    errorMsg(getString(R.string.error_select_opponent_team))
                }
            }
        }
    }

    private fun hasStoragePermission(): Boolean? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2) {
            if (//ActivityCompat.checkSelfPermission(this@OpponentTeamsActivity,Manifest.permission.MEDIA_CONTENT_CONTROL) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this@OpponentTeamsActivity,Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this@OpponentTeamsActivity, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this@OpponentTeamsActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this@OpponentTeamsActivity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
            ) {
                requestMultiplePermissions.launch(
                    arrayOf(
//                        Manifest.permission.MEDIA_CONTENT_CONTROL,
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                    )
                )
                //multiplePermissionLauncher.launch(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA});
                return false
            }
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(
                    this@OpponentTeamsActivity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    this@OpponentTeamsActivity, Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    this@OpponentTeamsActivity, Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    this@OpponentTeamsActivity, Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestMultiplePermissions.launch(
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                    )
                )
                //multiplePermissionLauncher.launch(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA});
                return false
            }
        }
        return true
    }
    val requestMultiplePermissions =  registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        permissions.entries.forEach {
            Log.d("DEBUG", "${it.key} = ${it.value}")
        }
    }

    fun setData(mList:ArrayList<TeamsBean.InfoBean>) {
        mAdapter = TeamsAdapter(this@OpponentTeamsActivity,mList,this,1)
        binding.rvTeams.adapter = mAdapter
     }
    fun initApiCall() {
        if (mApiCall == null) {
            mApiCall = ApiCall(this@OpponentTeamsActivity)
        }
    }
    override fun onConfirm(isTrue: Boolean, type: String) {
    }

    fun hideShow(strMsg:String,type:Int){
        binding.txtError.text = strMsg
        if (type==1) {
            binding.llNoData.visibility = View.VISIBLE
            binding.rvTeams.visibility = View.GONE
            binding.llOther.visibility = View.GONE
        }else{
            binding.llNoData.visibility = View.GONE
            binding.rvTeams.visibility = View.VISIBLE
            binding.llOther.visibility = View.VISIBLE
        }
    }
    override fun onSuccess(response: UniverSelObjct) {
        try {
            Logs.e(response.methodname.toString())
            when (response.methodname) {
                Tags.SB_OPPONENT_TEAMS_API -> {
                    var mTeamsBean: TeamsBean = response.response as TeamsBean
                    if (mTeamsBean.status == 1) {
                        mListTeams = mTeamsBean.info
                        setData(mListTeams)
                        //  moveToNext(mLoginBean.info)
                        hideShow("",0)
                    }else if (CommonMethods.isValidString(mTeamsBean.msg)) {
                        hideShow(mTeamsBean.msg,1)
                    } else {
                        hideShow(getResources().getString(R.string.something_wrong),1);
                    }
                }
                Tags.SB_CREATE_MATCH_API -> {
                    var mMatchesBean: MatchesBean = response.response as MatchesBean
                    if (mMatchesBean.status == 1 && CommonMethods.isValidArrayList(mMatchesBean.info)) {
                        this.mMatchBean = mMatchesBean.info[0]
                       // startActivity(Intent(this@OpponentTeamsActivity,CameraActivity::class.java).putExtra("MatchBean",mMatchBean))
                        startActivity(Intent(this@OpponentTeamsActivity,CameraActivityNew::class.java).putExtra("MatchBean",mMatchBean))
                    }else if (CommonMethods.isValidString(mMatchesBean.msg)) {
                        errorMsg(mMatchesBean.msg);
                    } else {
                        errorMsg(getResources().getString(R.string.something_wrong));
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            errorMsg(getResources().getString(R.string.something_wrong));
        }
        //method body
    }

    fun errorMsg(strMsg: String) {
        CommonMethods.errorDialog(this@OpponentTeamsActivity,strMsg,getResources().getString(R.string.app_name),getResources().getString(R.string.lbl_ok));
    }

    override fun onError(type: String, error: String) {
        if (type.equals(Tags.SB_OPPONENT_TEAMS_API)){
            hideShow(getResources().getString(R.string.something_wrong),1);
        }else {
            errorMsg(error)
        }
        //method body
    }

    fun getTeams(vararg strParams: String) {
        if (CommonMethods.isNetworkAvailable(this@OpponentTeamsActivity)) {
            mApiCall!!.getOpponentTeams(this, true,strParams[0],
                UserSessions.getUserInfo(this@OpponentTeamsActivity).id.toString(),game_category,team_id,league)
        } else {
            CommonMethods.errorDialog(
                this@OpponentTeamsActivity,
                getResources().getString(R.string.error_internet),
                getResources().getString(R.string.app_name),
                getResources().getString(R.string.lbl_ok)
            );
        }
    }

    fun createMatch(vararg strParams: String) {
        if(mMatchBean !=null && mMatchBean!!.id>0){
          //  startActivity(Intent(this@OpponentTeamsActivity,CameraActivity::class.java).putExtra("MatchBean",mMatchBean))
            startActivity(Intent(this@OpponentTeamsActivity,CameraActivityNew::class.java).putExtra("MatchBean",mMatchBean))
        }else{
            if (CommonMethods.isNetworkAvailable(this@OpponentTeamsActivity)) {
                mApiCall!!.createMatch(this, true,
                    UserSessions.getUserInfo(this@OpponentTeamsActivity).id.toString(),game_category,county,if(location_type.equals("1"))team_id else opponent_team_id,league,if(location_type.equals("1")) opponent_team_id else team_id,location_type,strParams[0],strParams[1],strParams[2])
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
        opponent_team_id = mBeanTeam.id.toString()
       // startActivity(Intent(this@OpponentTeamsActivity,LeagueActivity::class.java).putExtra("game_category",game_category).putExtra("county",county).putExtra("team_id",mBeanTeam.id.toString()))
    }
}