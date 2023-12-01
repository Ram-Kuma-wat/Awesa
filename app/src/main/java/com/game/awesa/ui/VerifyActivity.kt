package com.game.awesa.ui

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.multidex.BuildConfig
import com.codersworld.awesalibs.beans.CommonBean
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
import com.game.awesa.databinding.ActivityVerityOtpBinding
import com.game.awesa.ui.forgotpassword.ResetPassActivity
import com.google.gson.Gson

class VerifyActivity : AppCompatActivity(), OnConfirmListener, OnClickListener,
    OnResponse<UniverSelObjct> {
    lateinit var binding: ActivityVerityOtpBinding
    var mApiCall: ApiCall? = null
    var mCommonBean: CommonBean?=null
    var from:String="1";
    var strEmail:String="";
    var strPassword:String="";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_verity_otp)
         initApiCall()
        if (intent.hasExtra("mCommonBean")){
            mCommonBean = CommonMethods.getSerializable(intent,"mCommonBean",CommonBean::class.java)
        }else{
            mCommonBean = CommonBean(1,"A verification code is sent to your registered email address.")
        }
        if (intent.hasExtra("from")){
            from = intent.getStringExtra("from") as String
        }
        if (intent.hasExtra("strEmail")){
            strEmail = intent.getStringExtra("strEmail") as String
        }
        if (mCommonBean !=null && CommonMethods.isValidString(mCommonBean!!.msg)){
            binding.txtMsg.setText(mCommonBean!!.msg)
        }
        if (mCommonBean !=null && CommonMethods.isValidString(mCommonBean!!.msg)){
            binding.txtMsg.setText(mCommonBean!!.msg)
            startCountDown()
        }

        binding.btnSubmit.setOnClickListener(this)
        binding.txtResend.setOnClickListener(this)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    override fun onDestroy() {
        finishTimer()
        super.onDestroy()
    }

    override fun onPause() {
        finishTimer()
        super.onPause()
    }
    var mCountDownTimer: CountDownTimer? = null
    fun finishTimer() {
        if (mCountDownTimer != null) {
            mCountDownTimer!!.onFinish()
        }
    }

    fun startCountDown() {
        finishTimer()
        mCountDownTimer=object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                this@VerifyActivity.binding.txtResend.setText(this@VerifyActivity.getString(R.string.lbl_wait_time).replace("XXXX","00:" + millisUntilFinished / 1000))
                this@VerifyActivity.binding.txtResend.setTextColor(this@VerifyActivity.getResources().getColor(R.color.black))
            }

            override fun onFinish() {
                this@VerifyActivity.binding.txtResend.setText(this@VerifyActivity.getString(R.string.lbl_resend))
                this@VerifyActivity.binding.txtResend.setTextColor(this@VerifyActivity.getResources().getColor(R.color.colorAccent))
                this@VerifyActivity.binding.txtResend.setOnClickListener(OnClickListener { //   com.example.fubballgolf.ui.VerifyActivity.this.resendCode(com.example.fubballgolf.ui.VerifyActivity.this.strfName, com.example.fubballgolf.ui.VerifyActivity.this.strlName, com.example.fubballgolf.ui.VerifyActivity.this.strPhone, com.example.fubballgolf.ui.VerifyActivity.this.strEmail, com.example.fubballgolf.ui.VerifyActivity.this.strPass);
                     resendOTP(strEmail)
                })
            }
        }.start()
    }

    fun initApiCall() {
        if (mApiCall == null) {
            mApiCall = ApiCall(this@VerifyActivity)
        }
    }

    override fun onConfirm(isTrue: Boolean, type: String) {
     }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.txtResend -> {
                //CommonMethods.moveToNext(this@VerifyActivity,SignUpActivity::class.java)
            }
            R.id.btnSubmit -> {
                if (!CommonMethods.isValidString(binding.otpView.text.toString()) || binding.otpView.text.toString().length<4){
                 CommonMethods.errorDialog(this@VerifyActivity,getString(R.string.error_otp_required),getString(R.string.app_name),getString(R.string.lbl_ok))
                }else {
                    makeVerify(strEmail,binding.otpView.text.toString())
                }
            }
         }
        //method body
    }

    override fun onSuccess(response: UniverSelObjct) {
        try {
            Logs.e(response.methodname.toString())
            when (response.methodname) {
                Tags.SB_SIGNUP_VERIFY_OTP_API -> {
                    var mCommonBean: CommonBean = response.response as CommonBean
                    if (mCommonBean.status == 1) {
                        if (!from.equals("2")){
                            mCommonBean.info.login_username=mCommonBean.info.email
                            mCommonBean.info.password=strPassword
                            UserSessions.saveUserInfo(this@VerifyActivity,mCommonBean.info)
                            CommonMethods.moveWithClear(this@VerifyActivity, MainActivity::class.java)
                        }else{
                            startActivity(Intent(this@VerifyActivity, ResetPassActivity::class.java).putExtra("strEmail",strEmail).putExtra("strOTP",binding.otpView.text.toString()))
                        }
                    } else if (CommonMethods.isValidString(mCommonBean.msg)) {
                        errorMsg(mCommonBean.msg);
                    } else {
                        errorMsg(getResources().getString(R.string.something_wrong));
                    }
                }
                Tags.SB_RESEND_OTP_API -> {
                    var mCommonBean: CommonBean = response.response as CommonBean
                    if (mCommonBean.status == 1) {
                        startCountDown()
                    } else if (CommonMethods.isValidString(mCommonBean.msg)) {
                        errorMsg(mCommonBean.msg);
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
        CommonMethods.errorDialog(this@VerifyActivity,strMsg,getResources().getString(R.string.app_name),getResources().getString(R.string.lbl_ok) );
    }

    override fun onError(type: String, error: String) {
        errorMsg(error)
        //method body
    }

    fun makeVerify(vararg strParams: String) {
         var versionName = BuildConfig.VERSION_NAME + "/" + BuildConfig.VERSION_CODE.toString()
        if (CommonMethods.isNetworkAvailable(this@VerifyActivity)) {
              mApiCall!!.verifySignUpOTP(this, true,strParams[0],strParams[1],UserSessions.getFcmToken(this@VerifyActivity),versionName, if(from=="2") "1" else "")
        } else {
            CommonMethods.errorDialog(this@VerifyActivity, getResources().getString(R.string.error_internet),getResources().getString(R.string.app_name),getResources().getString(R.string.lbl_ok));
        }
    }
    fun resendOTP(vararg strParams: String) {
         var versionName = BuildConfig.VERSION_NAME + "/" + BuildConfig.VERSION_CODE.toString()
        if (CommonMethods.isNetworkAvailable(this@VerifyActivity)) {
             mApiCall!!.resendOTP(this, true,strParams[0],UserSessions.getFcmToken(this@VerifyActivity),versionName,from)
        } else {
            CommonMethods.errorDialog(this@VerifyActivity,getResources().getString(R.string.error_internet),getResources().getString(R.string.app_name),getResources().getString(R.string.lbl_ok));
        }
    }
}