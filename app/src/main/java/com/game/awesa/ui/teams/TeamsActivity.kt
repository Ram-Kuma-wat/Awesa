package com.game.awesa.ui.teams

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.game.awesa.databinding.ActivityTeamBinding
import com.game.awesa.ui.BaseActivity
import com.game.awesa.ui.SupportActivity
import com.game.awesa.ui.dashboard.MainActivity
import com.game.awesa.ui.league.LeagueActivity

class TeamsActivity : BaseActivity(), OnConfirmListener,OnResponse<UniverSelObjct>,OnTeamsListener {
    lateinit var binding: ActivityTeamBinding
    var mApiCall: ApiCall? = null
    var league=""
    var game_category=""
    var county=""
    var mAdapter :TeamsAdapter?=null
    var mListTeams:ArrayList<TeamsBean.InfoBean> = ArrayList()
    var mListTeamsFavourite:ArrayList<TeamsBean.InfoBean> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_team)
        initApiCall()
        DatabaseManager.initializeInstance(DatabaseHelper(this@TeamsActivity))
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.imgHome.setOnClickListener { CommonMethods.moveWithClear(this@TeamsActivity,
            MainActivity::class.java) }
        binding.imgStar.setOnClickListener{
            CommonMethods.moveToNext(this@TeamsActivity,LeagueActivity::class.java)
        }
        binding.rvTeams.layoutManager = LinearLayoutManager(this@TeamsActivity,RecyclerView.VERTICAL,false)
        binding.rvTeams1.layoutManager = LinearLayoutManager(this@TeamsActivity,RecyclerView.VERTICAL,false)
        setData(mListTeams)
        if (intent.hasExtra("county")) {
            county = intent.getStringExtra("county") as String
         }
        if (intent.hasExtra("league")) {
            league = intent.getStringExtra("league") as String
         }
        if (intent.hasExtra("game_category")) {
            game_category = intent.getStringExtra("game_category") as String
            getTeams("")
        }
        if (binding.txtAddTeam.getText().toString().contains(getString(R.string.lbl_contact_us) + ".")) {
            CommonMethods.setClickableHighLightedText(binding.txtAddTeam,getString(R.string.lbl_contact_us) + ".") { CommonMethods.moveToNext(this@TeamsActivity, SupportActivity::class.java) }
        }
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                var mListTeams1 : ArrayList<TeamsBean.InfoBean> =java.util.ArrayList()

                if (charSequence.length>0){
                    for (a in mListTeams.indices) {
                        if (mListTeams[a].title.lowercase().contains(charSequence.toString().lowercase())){
                            mListTeams1.add(mListTeams[a])
                        }
                    }
                }else{
                     mListTeams1 =mListTeams
                }
                setData(mListTeams1)
            }

            override fun afterTextChanged(editable: Editable) {}
        })

    }

    fun setData(mList:ArrayList<TeamsBean.InfoBean>) {
        mAdapter = TeamsAdapter(this@TeamsActivity,mList,this,0)
        binding.rvTeams.adapter = mAdapter
    }
    fun initApiCall() {
        if (mApiCall == null) {
            mApiCall = ApiCall(this@TeamsActivity)
        }
    }
    override fun onConfirm(isTrue: Boolean, type: String) {
    }
    fun hideShow(strMsg:String,type:Int){
        binding.txtError.text = strMsg
        if (type==1) {
            binding.llNoData.visibility = View.VISIBLE
            binding.rvTeams.visibility = View.GONE
        }else{
            binding.llNoData.visibility = View.GONE
            binding.rvTeams.visibility = View.VISIBLE
        }
    }

    override fun onSuccess(response: UniverSelObjct) {
        try {
            Logs.e(response.methodname.toString())
            when (response.methodname) {
                Tags.SB_TEAMS_API -> {
                    var mTeamsBean: TeamsBean = response.response as TeamsBean
                    if (mTeamsBean.status == 1) {
                        mListTeamsFavourite = mTeamsBean.favourite
                        mListTeams = mTeamsBean.info
                        setData(mListTeams)
                        var mAdapter1 : TeamsAdapter = TeamsAdapter(this@TeamsActivity,mListTeamsFavourite,this,0)
                        binding.rvTeams1.adapter = mAdapter1
                        hideShow("",0)
                      //  moveToNext(mLoginBean.info)
                    }else if (CommonMethods.isValidString(mTeamsBean.msg)) {
                        hideShow(mTeamsBean.msg,1);
                    } else {
                        hideShow(getResources().getString(R.string.something_wrong),1);
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
        CommonMethods.errorDialog(this@TeamsActivity,strMsg,getResources().getString(R.string.app_name),getResources().getString(R.string.lbl_ok));
    }

    override fun onError(type: String, error: String) {
        hideShow(getResources().getString(R.string.something_wrong),1);
        //errorMsg(error)
        //method body
    }

    fun getTeams(vararg strParams: String) {
        if (CommonMethods.isNetworkAvailable(this@TeamsActivity)) {
            mApiCall!!.getTeams(this, true,strParams[0],UserSessions.getUserInfo(this@TeamsActivity).id.toString(),game_category,league)
        } else {
            CommonMethods.errorDialog(
                this@TeamsActivity,
                getResources().getString(R.string.error_internet),
                getResources().getString(R.string.app_name),
                getResources().getString(R.string.lbl_ok)
            );
        }
    }

    override fun onTeamSelection(mBeanTeam: TeamsBean.InfoBean) {
        startActivity(Intent(this@TeamsActivity,OpponentTeamsActivity::class.java).putExtra("game_category",game_category).putExtra("county",county).putExtra("league",league).putExtra("team_id",mBeanTeam.id.toString()))
    }
}