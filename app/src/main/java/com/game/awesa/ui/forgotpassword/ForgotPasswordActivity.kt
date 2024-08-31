package com.game.awesa.ui.forgotpassword

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.codersworld.awesalibs.beans.CommonBean
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
import com.game.awesa.R
import com.game.awesa.databinding.ActivityForgotPasswordBinding
import com.game.awesa.ui.VerifyActivity

class ForgotPasswordActivity : AppCompatActivity(), OnConfirmListener, OnClickListener,
    OnResponse<UniversalObject> {
    lateinit var binding: ActivityForgotPasswordBinding
    var mApiCall: ApiCall? = null
    var strEmail: String = "";
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_forgot_password)
        UserSessions.saveIsProfile(this@ForgotPasswordActivity, "-1")
        initApiCall()
        if (intent.hasExtra("strEmail")) {
            strEmail = intent.getStringExtra("strEmail") as String
            if (CommonMethods.isValidString(strEmail)) {
                binding.etEmail.setText(strEmail)
            }
        }

         binding.btnSubmit.setOnClickListener(this)
        binding.toolbar.setNavigationOnClickListener { finish() }

    }

    fun initApiCall() {
        if (mApiCall == null) {
            mApiCall = ApiCall(this@ForgotPasswordActivity)
        }
    }

    override fun onConfirm(isTrue: Boolean, type: String) {
        //method body
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnSubmit -> {
                validateLogin()
            }
        }
    }

    override fun onSuccess(response: UniversalObject) {
        try {
            Logs.e(response.methodName.toString())
            when (response.methodName) {
                Tags.SB_FORGOT_PASSWORD_API -> {
                    var mCommonBean: CommonBean = response.response as CommonBean
                    if (mCommonBean.status == 1) {
                        startActivity(
                            Intent(
                                this@ForgotPasswordActivity,
                                VerifyActivity::class.java
                            ).putExtra("mCommonBean", mCommonBean)
                                .putExtra("strEmail", binding.etEmail.text.toString())
                                .putExtra("from", "2")
                        )
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
        CommonMethods.errorDialog(
            this@ForgotPasswordActivity,
            strMsg,
            getResources().getString(R.string.app_name),
            getResources().getString(R.string.lbl_ok)
        );
    }

    override fun onError(type: String, error: String) {
        errorMsg(error)
        //method body
    }

    fun validateLogin() {
        var strUsername =/* "kumawat";*/binding.etEmail.text.toString()
        if (!CommonMethods.isValidString(strUsername)) {
            CommonMethods.setError(
                binding.etEmail,
                this@ForgotPasswordActivity,
                getString(R.string.email_required),
                getString(R.string.email_required)
            )
        } else if (CommonMethods.checkEmail(strUsername, this@ForgotPasswordActivity) != -1) {
            CommonMethods.setError(
                binding.etEmail,
                this@ForgotPasswordActivity,
                getString(R.string.error_email_invalid),
                getString(R.string.error_email_invalid)
            )
        } else {
            makeAction(strUsername)
        }
    }

    fun makeAction(vararg strParams: String) {
        if (CommonMethods.isNetworkAvailable(this@ForgotPasswordActivity)) {
            mApiCall!!.forgotPassword(
                this,
                strParams[0],
                UserSessions.getFcmToken(this@ForgotPasswordActivity)
            )
        } else {
            CommonMethods.errorDialog(
                this@ForgotPasswordActivity,
                getResources().getString(R.string.error_internet),
                getResources().getString(R.string.app_name),
                getResources().getString(R.string.lbl_ok),
            );
        }
    }

 }
