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
import com.codersworld.awesalibs.rest.UniversalObject
import com.codersworld.awesalibs.storage.UserSessions
import com.codersworld.awesalibs.utils.CommonMethods
import com.codersworld.awesalibs.utils.Tags
import com.game.awesa.ui.dashboard.MainActivity
import com.game.awesa.R
import com.game.awesa.databinding.ActivityVerityOtpBinding
import com.game.awesa.ui.forgotpassword.ResetPassActivity
import java.util.Locale

class VerifyActivity : AppCompatActivity(), OnConfirmListener, OnClickListener,
    OnResponse<UniversalObject> {
        
    companion object {
        val TAG: String = VerifyActivity::class.java.simpleName
    }
        
    lateinit var binding: ActivityVerityOtpBinding
    var mApiCall: ApiCall = ApiCall(this@VerifyActivity)
    var mCommonBean: CommonBean?=null
    var from: String="1"
    var strEmail: String=""
    private var strPassword: String=""
    private var mCountDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_verity_otp)
        binding.btnSubmit.setOnClickListener(this)
        binding.txtResend.setOnClickListener(this)
        binding.toolbar.setNavigationOnClickListener { finish() }

        mCommonBean = if (intent.hasExtra("mCommonBean")) {
            CommonMethods.getSerializable(intent,"mCommonBean",CommonBean::class.java)
        } else {
            CommonBean(1,"A verification code is sent to your registered email address.")
        }

        if (intent.hasExtra("from")) {
            from = intent.getStringExtra("from") as String
        }

        if (intent.hasExtra("strEmail")) {
            strEmail = intent.getStringExtra("strEmail") as String
        }

        if (mCommonBean !=null && CommonMethods.isValidString(mCommonBean!!.msg)) {
            binding.txtMsg.text = mCommonBean!!.msg
        }

        if (mCommonBean !=null && CommonMethods.isValidString(mCommonBean!!.msg)) {
            binding.txtMsg.text = mCommonBean!!.msg
            startCountDown()
        }
    }

    override fun onDestroy() {
        finishTimer()
        super.onDestroy()
    }

    override fun onPause() {
        finishTimer()
        super.onPause()
    }

    private fun finishTimer() {
        if (mCountDownTimer != null) {
            mCountDownTimer!!.onFinish()
        }
    }

    @Suppress("MagicNumber")
    private fun startCountDown() {
        finishTimer()
        mCountDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                this@VerifyActivity.binding.txtResend.text = String.format(
                    Locale.getDefault(), getString(R.string.lbl_wait_time),
                    millisUntilFinished / 1000
                )
                this@VerifyActivity.binding.txtResend.setTextColor(getColor(R.color.black))
            }

            override fun onFinish() {
                this@VerifyActivity.binding.txtResend.text = this@VerifyActivity.getString(R.string.lbl_resend)
                this@VerifyActivity.binding.txtResend.setTextColor(getColor(R.color.colorAccent))
                this@VerifyActivity.binding.txtResend.setOnClickListener(OnClickListener {
                     resendOTP(strEmail)
                })
            }
        }.start()
    }

    @Suppress("EmptyFunctionBlock")
    override fun onConfirm(isTrue: Boolean, type: String) {}

    @Suppress("MagicNumber")
    override fun onClick(view: View) {
        when (view.id) {
            R.id.txtResend -> {
                //CommonMethods.moveToNext(this@VerifyActivity,SignUpActivity::class.java)
            }
            R.id.btnSubmit -> {
                if (!binding.otpView.text.isNullOrEmpty() || binding.otpView.text.toString().length<4) {
                     CommonMethods.errorDialog(
                         this@VerifyActivity,
                         getString(R.string.error_otp_required),
                         getString(R.string.app_name),
                         getString(R.string.lbl_ok)
                     )
                } else {
                    makeVerify(strEmail, binding.otpView.text.toString())
                }
            }
         }

    }

    override fun onSuccess(response: UniversalObject) {
        try {
            when (response.methodName) {
                Tags.SB_SIGNUP_VERIFY_OTP_API -> {
                    val mCommonBean: CommonBean? = response.response as? CommonBean
                    if (mCommonBean == null) return
                    if (mCommonBean.status != 1) {

                        if (CommonMethods.isValidString(mCommonBean.msg)) {
                            errorMsg(mCommonBean.msg)
                            return
                        }

                        errorMsg(getResources().getString(R.string.something_wrong))
                        return
                    }

                    if (from != "2") {
                        mCommonBean.info.login_username = mCommonBean.info.email
                        mCommonBean.info.password = strPassword
                        UserSessions.saveUserInfo(this@VerifyActivity,mCommonBean.info)
                        CommonMethods.moveWithClear(this@VerifyActivity, MainActivity::class.java)
                    } else {
                        val intent = Intent(this@VerifyActivity, ResetPassActivity::class.java)
                        intent.putExtra("strEmail", strEmail)
                        intent.putExtra("strOTP", binding.otpView.text.toString())
                        startActivity(intent)
                    }
                }
                Tags.SB_RESEND_OTP_API -> {
                    val mCommonBean: CommonBean? = response.response as? CommonBean
                    if (mCommonBean == null) return
                    if (mCommonBean.status == 1) {
                        startCountDown()
                    } else if (CommonMethods.isValidString(mCommonBean.msg)) {
                        errorMsg(mCommonBean.msg)
                    } else {
                        errorMsg(getResources().getString(R.string.something_wrong))
                    }
                }
             }
        } catch (ex: Exception) {
            errorMsg(getResources().getString(R.string.something_wrong))
            Log.e(TAG, ex.localizedMessage, ex)
        }
    }

    fun errorMsg(strMsg: String) {
        CommonMethods.errorDialog(
            this@VerifyActivity,
            strMsg,
            getResources().getString(R.string.app_name),
            getResources().getString(R.string.lbl_ok))
    }

    override fun onError(type: String, error: String) {
        errorMsg(error)
    }

    private fun makeVerify(vararg strParams: String) {
        val versionName = BuildConfig.VERSION_NAME + "/" + BuildConfig.VERSION_CODE.toString()
        if (CommonMethods.isNetworkAvailable(this@VerifyActivity)) {
              mApiCall.verifySignUpOTP(
                  this,
                  true,
                  strParams[0],
                  strParams[1],
                  UserSessions.getFcmToken(this@VerifyActivity),
                  versionName,
                  if(from=="2") "1" else ""
              )
        } else {
            CommonMethods.errorDialog(
                this@VerifyActivity,
                getResources().getString(R.string.error_internet),
                getResources().getString(R.string.app_name),
                getResources().getString(R.string.lbl_ok)
            )
        }
    }

    fun resendOTP(vararg strParams: String) {
        val versionName = BuildConfig.VERSION_NAME + "/" + BuildConfig.VERSION_CODE.toString()
        if (CommonMethods.isNetworkAvailable(this@VerifyActivity)) {
             mApiCall.resendOTP(
                 this,
                 true,
                 strParams[0],
                 UserSessions.getFcmToken(this@VerifyActivity),
                 versionName,
                 from
             )
        } else {
            CommonMethods.errorDialog(
                this@VerifyActivity,
                getResources().getString(R.string.error_internet),
                getResources().getString(R.string.app_name),
                getResources().getString(R.string.lbl_ok))
        }
    }
}