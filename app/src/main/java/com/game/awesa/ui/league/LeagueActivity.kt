package com.game.awesa.ui.league

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codersworld.awesalibs.beans.leagues.LeagueBean
import com.codersworld.awesalibs.database.DatabaseHelper
import com.codersworld.awesalibs.database.DatabaseManager
import com.codersworld.awesalibs.listeners.OnConfirmListener
import com.codersworld.awesalibs.listeners.OnLeagueListener
import com.codersworld.awesalibs.listeners.OnResponse
import com.codersworld.awesalibs.rest.ApiCall
import com.codersworld.awesalibs.rest.UniversalObject
import com.codersworld.awesalibs.storage.UserSessions
import com.codersworld.awesalibs.utils.CommonMethods
import com.codersworld.awesalibs.utils.Logs
import com.codersworld.awesalibs.utils.Tags
import com.game.awesa.R
 import com.game.awesa.databinding.ActivityLeagueBinding
 import com.game.awesa.ui.BaseActivity
import com.game.awesa.ui.LoginActivity
import com.game.awesa.ui.dashboard.MainActivity
import com.game.awesa.ui.league.adapter.LeagueAdapter
import com.game.awesa.ui.teams.TeamsActivity
import com.game.awesa.utils.Global


class LeagueActivity : BaseActivity(), OnConfirmListener,OnResponse<UniversalObject>,
    OnLeagueListener {
    lateinit var binding: ActivityLeagueBinding
    var mApiCall: ApiCall? = null
    var game_category=""
    var team_id=""
    var county=""
    var mAdapter : LeagueAdapter?=null
    var mListTeams:ArrayList<LeagueBean.InfoBean> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_league)
        initApiCall()
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.imgHome.setOnClickListener { CommonMethods.moveWithClear(this@LeagueActivity,MainActivity::class.java) }
        binding.rvLeagues.layoutManager = LinearLayoutManager(this@LeagueActivity,RecyclerView.VERTICAL,false)
         setData(mListTeams)
        if (intent.hasExtra("team_id")) {
            team_id = intent.getStringExtra("team_id") as String
        }
        if (intent.hasExtra("county")) {
            county = intent.getStringExtra("county") as String
        }
        if (intent.hasExtra("game_category")) {
            game_category = intent.getStringExtra("game_category") as String
            getLeagues("")
        }
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                var mListTeams1 : ArrayList<LeagueBean.InfoBean> =java.util.ArrayList()

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

    fun setData(mList:ArrayList<LeagueBean.InfoBean>) {
        mAdapter = LeagueAdapter(this@LeagueActivity,mList,this)
        binding.rvLeagues.adapter = mAdapter
     }
    fun initApiCall() {
        if (mApiCall == null) {
            mApiCall = ApiCall(this@LeagueActivity)
        }
    }
    override fun onConfirm(isTrue: Boolean, type: String) {
        if (isTrue){
            if (type.equals("99")){
                UserSessions.clearUserInfo(this@LeagueActivity)
                startActivity(Intent(this@LeagueActivity, LoginActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                finishAffinity()
            }
        }
    }
    fun hideShow(strMsg:String,type:Int){
        binding.txtError.text = strMsg
        if (type==1) {
            binding.llNoData.visibility = View.VISIBLE
            binding.rvLeagues.visibility = View.GONE
         }else{
            binding.llNoData.visibility = View.GONE
            binding.rvLeagues.visibility = View.VISIBLE
         }
    }
    override fun onSuccess(response: UniversalObject) {
        try {
            Logs.e(response.methodName.toString())
            when (response.methodName) {
                Tags.SB_LEAGUE_API -> {
                    var mLeagueBean: LeagueBean = response.response as LeagueBean
                    if (mLeagueBean.status == 1) {
                        mListTeams = mLeagueBean.info
                        setData(mListTeams)
                        //  moveToNext(mLoginBean.info)
                        hideShow("",0)
                    }else if (mLeagueBean.status == 99) {
                        UserSessions.clearUserInfo(this@LeagueActivity)
                        Global().makeConfirmation(mLeagueBean.msg, this@LeagueActivity, this)
                    }else if (CommonMethods.isValidString(mLeagueBean.msg)) {
                        hideShow(mLeagueBean.msg,1);
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
        CommonMethods.errorDialog(this@LeagueActivity,strMsg,getResources().getString(R.string.app_name),getResources().getString(R.string.lbl_ok));
    }

    override fun onError(type: String, error: String) {
        hideShow(getResources().getString(R.string.something_wrong),1);
        //errorMsg(error)
        //method body
    }

    fun getLeagues(vararg strParams: String) {
        if (CommonMethods.isNetworkAvailable(this@LeagueActivity)) {
            mApiCall!!.getLeagues(this, true,strParams[0],UserSessions.getUserInfo(this@LeagueActivity).id.toString(),game_category,county,team_id)
        } else {
            CommonMethods.errorDialog(
                this@LeagueActivity,
                getResources().getString(R.string.error_internet),
                getResources().getString(R.string.app_name),
                getResources().getString(R.string.lbl_ok)
            );
        }
    }

    override fun onLeagueSelection(mBeanLeague: LeagueBean.InfoBean) {
        startActivity(Intent(this@LeagueActivity, TeamsActivity::class.java).putExtra("game_category",game_category).putExtra("county",county).putExtra("team_id",team_id).putExtra("league",mBeanLeague.id.toString()))
    }
}
