package com.game.awesa.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import com.codersworld.awesalibs.rest.UniversalObject
import com.codersworld.awesalibs.storage.UserSessions
import com.codersworld.awesalibs.utils.CommonMethods
import com.codersworld.awesalibs.utils.Logs
import com.codersworld.awesalibs.utils.Tags
import com.game.awesa.ui.dashboard.MainActivity
import com.game.awesa.R
import com.game.awesa.databinding.ActivityLoginNewBinding
import com.game.awesa.ui.dialogs.CustomDialog
import com.game.awesa.ui.forgotpassword.ForgotPasswordActivity
import com.game.awesa.utils.ErrorReporter
import com.game.awesa.utils.JBWatcher
import com.google.gson.Gson

class LoginActivity : BaseActivity(),OnClickListener , OnConfirmListener,
    OnResponse<UniversalObject> {
    lateinit var binding: ActivityLoginNewBinding
    var mApiCall: ApiCall? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login_new)
        initApiCall()
        binding.txtSignUp.setOnClickListener(this)
        binding.btnLogin.setOnClickListener(this)
        binding.txtForgotPassword.setOnClickListener(this)
        val errReporter = ErrorReporter()
        errReporter.Init(this)
        errReporter.CheckErrorAndSendMail(this)

    }
var clicked=0;
    var isClicked=false
    override fun onResume() {
        super.onResume()
        if(UserSessions.getUserInfo(this@LoginActivity) !=null && CommonMethods.isValidString(UserSessions.getUserInfo(this@LoginActivity).login_username) && CommonMethods.isValidString(UserSessions.getUserInfo(this@LoginActivity).password)){
            binding.etUsername.setText(UserSessions.getUserInfo(this@LoginActivity).login_username.toString());
            binding.etPassword.setText(UserSessions.getUserInfo(this@LoginActivity).password.toString());
            isClicked=false
            /*binding.btnLogin.*/validateLogin()
        }
        checkSignup()
        clicked=1
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
               isClicked=true
               validateLogin()
           }
       }
    }
    var customDialog: CustomDialog? = null
    var isDialogOpen = false
    fun makeConfirmation(msg:String){
        if (!isDialogOpen) {
            if (customDialog == null) {
                customDialog = CustomDialog(this@LoginActivity,msg,resources.getString(R.string.lbl_cancel) ,this, "1")
                customDialog!!.getWindow()!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
            isDialogOpen = true
            if (customDialog!! != null && customDialog!!.isShowing()) {
                customDialog!!.dismiss()
            }
            customDialog!!.show()
        }
    }
    fun makeLogin(vararg strParams: String) {
        var versionName = BuildConfig.VERSION_NAME + "/" + BuildConfig.VERSION_CODE.toString()
        if (CommonMethods.isNetworkAvailable(this@LoginActivity)) {
            mApiCall!!.userLogin(this, true,strParams[0],strParams[1],UserSessions.getFcmToken(this@LoginActivity),versionName)
        } else {
            CommonMethods.errorDialog(this@LoginActivity,getResources().getString(R.string.error_internet),getResources().getString(R.string.app_name),getResources().getString(R.string.lbl_ok),);
        }
    }
    var strUsername =""
    var strPassword = ""
    fun validateLogin() {
          strUsername =/* "kumawat";*/binding.etUsername.text.toString()
          strPassword = /*"Ram@8824";*/binding.etPassword.text.toString()
        if (!CommonMethods.isValidString(strUsername)) {
            CommonMethods.setError(binding.etUsername,this@LoginActivity,getString(R.string.error_username),getString(R.string.error_username))
        }else if (!CommonMethods.isValidString(strPassword)) {
            CommonMethods.setError(binding.etPassword,this@LoginActivity,getString(R.string.password_required),getString(R.string.password_required))
        }  else {
            if (isClicked){
                makeConfirmation(getString(R.string.msg_logout_on_login))
            }else {
                makeLogin(strUsername, strPassword)
            }
        }
    }
    fun checkSignup() {
         if (CommonMethods.isNetworkAvailable(this@LoginActivity)) {
            mApiCall!!.checkSignup(
                this,
                if(clicked>0) false else true,
            )
         } else {
            CommonMethods.errorDialog(
                this@LoginActivity,
                getResources().getString(R.string.error_internet),
                getResources().getString(R.string.app_name),
                getResources().getString(R.string.lbl_ok)
            );
        }
    }
    override fun onConfirm(isTrue: Boolean, type: String) {
        isDialogOpen = false
        if (isTrue) {
            if (type.equals("1")){
                makeLogin(strUsername, strPassword)
            }else{
                var mCommonBean: CommonBean = Gson().fromJson(type,CommonBean::class.java)
                startActivity(Intent(this@LoginActivity, VerifyActivity::class.java).putExtra("mCommonBean",mCommonBean).putExtra("strEmail",binding.etUsername.text.toString()).putExtra("strPassword",binding.etPassword.text.toString()).putExtra("from","0"))
            }
        }
    }

    override fun onSuccess(response: UniversalObject) {
        try {
            Logs.e(response.methodName.toString())
            when (response.methodName) {
                Tags.SB_LOGIN_API -> {
                    var mLoginBean: CommonBean = response.response as CommonBean
                   // Log.e("mLoginBean",Gson().toJson(mLoginBean))
                    if (mLoginBean.status == 1) {
                        moveToNext(mLoginBean.info)
                    }else  if (mLoginBean.status == 2) {
                        startActivity(Intent(this@LoginActivity,VerifyActivity::class.java).putExtra("mCommonBean", mLoginBean).putExtra("strEmail", mLoginBean.info.email).putExtra("from","1"))
                    }else if (CommonMethods.isValidString(mLoginBean.msg)) {
                         errorMsg(mLoginBean.msg);
                    } else {
                        errorMsg(getResources().getString(R.string.something_wrong));
                    }
                }
                Tags.SB_CHECK_SIGNUP_API -> {
                    var mCommonBean: CommonBean = response.response as CommonBean
                    if (mCommonBean.status == 1) {
                        if (mCommonBean.app_signup_allowed==1){
                            binding.llSignup.visibility=View.VISIBLE
                        }else{
                            binding.llSignup.visibility=View.GONE
                        }
                        if (CommonMethods.isValidArrayList(mCommonBean.sponsors)){
                            UserSessions.saveSponsors(this@LoginActivity,Gson().toJson(mCommonBean.sponsors))
                        }
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
