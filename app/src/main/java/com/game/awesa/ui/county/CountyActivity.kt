package com.game.awesa.ui.county

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codersworld.awesalibs.beans.county.CountyBean
import com.codersworld.awesalibs.beans.teams.TeamsBean
import com.codersworld.awesalibs.database.DatabaseHelper
import com.codersworld.awesalibs.database.DatabaseManager
import com.codersworld.awesalibs.listeners.OnConfirmListener
import com.codersworld.awesalibs.listeners.OnCountyListener
import com.codersworld.awesalibs.listeners.OnResponse
import com.codersworld.awesalibs.rest.ApiCall
import com.codersworld.awesalibs.rest.UniverSelObjct
import com.codersworld.awesalibs.storage.UserSessions
import com.codersworld.awesalibs.utils.CommonMethods
import com.codersworld.awesalibs.utils.Logs
import com.codersworld.awesalibs.utils.Tags
import com.game.awesa.R
import com.game.awesa.databinding.ActivityCountyBinding
 import com.game.awesa.ui.BaseActivity
import com.game.awesa.ui.county.adapter.CountyAdapter
import com.game.awesa.ui.dashboard.MainActivity
import com.game.awesa.ui.league.LeagueActivity
import com.game.awesa.ui.teams.TeamsActivity

class CountyActivity : BaseActivity(), OnConfirmListener,OnResponse<UniverSelObjct>,OnCountyListener {
    lateinit var binding: ActivityCountyBinding
    var mApiCall: ApiCall? = null
    var game_category=""
    var county=""
    var mAdapter :CountyAdapter?=null
    var mListCounty:ArrayList<CountyBean.InfoBean> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_county)
        initApiCall()
        DatabaseManager.initializeInstance(DatabaseHelper(this@CountyActivity))
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.imgHome.setOnClickListener { CommonMethods.moveWithClear(this@CountyActivity,
            MainActivity::class.java) }
        binding.rvCounty.layoutManager = LinearLayoutManager(this@CountyActivity,RecyclerView.VERTICAL,false)
        setData(mListCounty)
        if (intent.hasExtra("game_category")) {
            game_category = intent.getStringExtra("game_category") as String
            getCounty("")
        }
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                var mListCounty1 : ArrayList<CountyBean.InfoBean> =java.util.ArrayList()

                if (charSequence.length>0){
                    for (a in mListCounty.indices) {
                        if (mListCounty[a].title.lowercase().contains(charSequence.toString().lowercase())){
                            mListCounty1.add(mListCounty[a])
                        }
                    }
                }else{
                     mListCounty1 =mListCounty
                }
                setData(mListCounty1)
            }

            override fun afterTextChanged(editable: Editable) {}
        })
    }

    fun setData(list:ArrayList<CountyBean.InfoBean>){
        mAdapter = CountyAdapter(this@CountyActivity,list,this)
        binding.rvCounty.adapter = mAdapter

    }
    fun initApiCall() {
        if (mApiCall == null) {
            mApiCall = ApiCall(this@CountyActivity)
        }
    }
    override fun onConfirm(isTrue: Boolean, type: String) {
    }

    override fun onSuccess(response: UniverSelObjct) {
        try {
            Logs.e(response.methodname.toString())
            when (response.methodname) {
                Tags.SB_COUNTY_API -> {
                    var mCountyBean: CountyBean = response.response as CountyBean
                    if (mCountyBean.status == 1) {
                        mListCounty = mCountyBean.info
                        setData(mListCounty)
                      //  moveToNext(mLoginBean.info)
                    }else if (CommonMethods.isValidString(mCountyBean.msg)) {
                        errorMsg(mCountyBean.msg);
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
        CommonMethods.errorDialog(this@CountyActivity,strMsg,getResources().getString(R.string.app_name),getResources().getString(R.string.lbl_ok));
    }

    override fun onError(type: String, error: String) {
        errorMsg(error)
        //method body
    }

    fun getCounty(vararg strParams: String) {
        if (CommonMethods.isNetworkAvailable(this@CountyActivity)) {
            mApiCall!!.getCounty(this, true,strParams[0],UserSessions.getUserInfo(this@CountyActivity).id.toString(),game_category)
        } else {
            CommonMethods.errorDialog(
                this@CountyActivity,
                getResources().getString(R.string.error_internet),
                getResources().getString(R.string.app_name),
                getResources().getString(R.string.lbl_ok)
            );
        }
    }

    override fun onCountySelection(mBeanCounty: CountyBean.InfoBean) {
        startActivity(Intent(this@CountyActivity,LeagueActivity::class.java).putExtra("game_category",game_category).putExtra("county",mBeanCounty.id.toString()))
    }

}