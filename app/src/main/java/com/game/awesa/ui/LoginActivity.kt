package com.game.awesa.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.View.OnClickListener
import androidx.databinding.DataBindingUtil
import androidx.multidex.BuildConfig
import com.codersworld.awesalibs.beans.CommonBean
import com.codersworld.awesalibs.beans.user.UserBean
import com.codersworld.awesalibs.database.DatabaseHelper
import com.codersworld.awesalibs.database.DatabaseManager
import com.codersworld.awesalibs.listeners.OnConfirmListener
import com.codersworld.awesalibs.listeners.OnResponse
import com.codersworld.awesalibs.rest.ApiCall
import com.codersworld.awesalibs.rest.UniverSelObjct
import com.codersworld.awesalibs.storage.UserSessions
import com.codersworld.awesalibs.utils.CommonMethods
import com.codersworld.awesalibs.utils.Logs
import com.codersworld.awesalibs.utils.Tags
import com.game.awesa.ui.dashboard.MainActivity
import com.game.awesa.R
import com.game.awesa.databinding.ActivityLoginNewBinding
import com.game.awesa.ui.dialogs.CustomDialog
import com.game.awesa.ui.forgotpassword.ForgotPasswordActivity
import com.game.awesa.utils.JBWatcher
import com.google.gson.Gson

class LoginActivity : BaseActivity(),OnClickListener , OnConfirmListener,
    OnResponse<UniverSelObjct> {
    lateinit var binding: ActivityLoginNewBinding
    var mApiCall: ApiCall? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login_new)
        initApiCall()
        DatabaseManager.initializeInstance(DatabaseHelper(this@LoginActivity))
        binding.txtSignUp.setOnClickListener(this)
        binding.btnLogin.setOnClickListener(this)
        binding.txtForgotPassword.setOnClickListener(this)

    }

    override fun onResume() {
        super.onResume()
        if(UserSessions.getUserInfo(this@LoginActivity) !=null && CommonMethods.isValidString(UserSessions.getUserInfo(this@LoginActivity).login_username) && CommonMethods.isValidString(UserSessions.getUserInfo(this@LoginActivity).password)){
            binding.etUsername.setText(UserSessions.getUserInfo(this@LoginActivity).login_username.toString());
            binding.etPassword.setText(UserSessions.getUserInfo(this@LoginActivity).password.toString());
            binding.btnLogin.performClick()
        }
        binding.etUsername.addTextChangedListener(JBWatcher(this@LoginActivity,binding.etUsername,null,1))
    }
    fun initApiCall() {
        if (mApiCall == null) {
            mApiCall = ApiCall(this@LoginActivity)
        }
    }
    override fun onClick(v: View) {
       when(v.id){
           R.id.txtSignUp->{
               CommonMethods.moveToNext(this@LoginActivity,SignUpActivity::class.java)
           }
           R.id.txtForgotPassword->{
               CommonMethods.moveToNext(this@LoginActivity,ForgotPasswordActivity::class.java)
           }
           R.id.btnLogin->{
               validateLogin()
           }
       }
    }
    var customDialog: CustomDialog? = null
    var isDialogOpen = false
    var mCommonBean: CommonBean?=null
/*    fun makeConfirmation(msg: LoginBean){
        mCommonBean= CommonBean(msg.status,msg.msg)
        var str = Gson().toJson(mCommonBean);
        if (!isDialogOpen) {
            if (customDialog == null) {
                customDialog = CustomDialog(this@LoginActivity,msg.msg1,resources.getString(R.string.lbl_cancel) ,this, str)
                customDialog!!.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
            isDialogOpen = true
            if (customDialog!! != null && customDialog!!.isShowing()) {
                customDialog!!.dismiss()
            }
            customDialog!!.show()
        }
    }*/
    fun makeLogin(vararg strParams: String) {
        var versionName = BuildConfig.VERSION_NAME + "/" + BuildConfig.VERSION_CODE.toString()

        if (CommonMethods.isNetworkAvailable(this@LoginActivity)) {
            mApiCall!!.userLogin(this, true,strParams[0],strParams[1],UserSessions.getFcmToken(this@LoginActivity),versionName)
        } else {
            CommonMethods.errorDialog(this@LoginActivity,getResources().getString(R.string.error_internet),getResources().getString(R.string.app_name),getResources().getString(R.string.lbl_ok),);
        }
    }
    fun validateLogin() {
        var strUsername =/* "kumawat";*/binding.etUsername.text.toString()
        var strPassword = /*"Ram@8824";*/binding.etPassword.text.toString()
        if (!CommonMethods.isValidString(strUsername)) {
            CommonMethods.setError(binding.etUsername,this@LoginActivity,getString(R.string.error_username),getString(R.string.error_username))
        }else if (!CommonMethods.isValidString(strPassword)) {
            CommonMethods.setError(binding.etPassword,this@LoginActivity,getString(R.string.password_required),getString(R.string.password_required))
        }  else {
            makeLogin(strUsername, strPassword)
        }
    }

    override fun onConfirm(isTrue: Boolean, type: String) {
        isDialogOpen = false
        if (isTrue) {
            var mCommonBean: CommonBean = Gson().fromJson(type,CommonBean::class.java)
            startActivity(Intent(this@LoginActivity, VerifyActivity::class.java).putExtra("mCommonBean",mCommonBean).putExtra("strEmail",binding.etUsername.text.toString()).putExtra("strPassword",binding.etPassword.text.toString()).putExtra("from","0"))
        }
    }


    override fun onSuccess(response: UniverSelObjct) {
        try {
            Logs.e(response.methodname.toString())
            when (response.methodname) {
                Tags.SB_LOGIN_API -> {
                    var mLoginBean: CommonBean = response.response as CommonBean
                    if (mLoginBean.status == 1) {
                        moveToNext(mLoginBean.info)
                    }else  if (mLoginBean.status == 2) {
                        startActivity(Intent(this@LoginActivity,VerifyActivity::class.java).putExtra("mCommonBean", mLoginBean).putExtra("strEmail", mLoginBean.info.email).putExtra("from","1"))
                    }else if (CommonMethods.isValidString(mLoginBean.msg)) {
                    //  CommonMethods.moveWithClear(this@LoginActivity, MainActivity::class.java)
                        errorMsg(mLoginBean.msg);
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
        CommonMethods.errorDialog(this@LoginActivity,strMsg,getResources().getString(R.string.app_name),getResources().getString(R.string.lbl_ok));
    }

    override fun onError(type: String, error: String) {
        errorMsg(error)
        //method body
    }

    fun moveToDashboard(mLoginBean: UserBean) {
        UserSessions.saveUserInfo(this@LoginActivity,mLoginBean)
        val hs = Handler()
        hs.postDelayed({
            CommonMethods.moveWithClear(this@LoginActivity, MainActivity::class.java)
        }, 100)
    }
    fun moveToNext(mLoginBean:UserBean) {
        mLoginBean.login_username = binding.etUsername.text!!.trim().toString()
        mLoginBean.password = binding.etPassword.text!!.trim().toString()
        moveToDashboard(mLoginBean);
    }


    }