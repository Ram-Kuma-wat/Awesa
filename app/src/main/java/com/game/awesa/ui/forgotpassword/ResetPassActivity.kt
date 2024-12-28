package com.game.awesa.ui.forgotpassword

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
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
import com.game.awesa.databinding.ActivityResetPassBinding
import com.game.awesa.ui.BaseActivity
import com.game.awesa.ui.LoginActivity
import com.game.awesa.ui.dialogs.CustomDialog

class ResetPassActivity : BaseActivity(), OnConfirmListener,  OnClickListener,
    OnResponse<UniversalObject> {
    lateinit var binding: ActivityResetPassBinding
    var mApiCall: ApiCall? = null
    var strEmail:String=""
    var strOTP:String=""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reset_pass)
        UserSessions.saveIsProfile(this@ResetPassActivity, "-1")
        initApiCall()

        if (intent.hasExtra("strOTP")){
            strOTP = intent.getStringExtra("strOTP") as String
        }
        if (intent.hasExtra("strEmail")){
            strEmail = intent.getStringExtra("strEmail") as String
        }
        binding.btnLogin.setOnClickListener(this)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    fun initApiCall() {
        if (mApiCall == null) {
            mApiCall = ApiCall(this@ResetPassActivity)
        }
    }

    override fun onConfirm(isTrue: Boolean, type: String) {
        isDialogOpen = false
        CommonMethods.moveWithClear(this@ResetPassActivity, LoginActivity::class.java)
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnLogin -> {
                validateLogin()
            }
        }
    }

    override fun onSuccess(response: UniversalObject) {
        try {
            Logs.e(response.methodName.toString())
            when (response.methodName) {
                Tags.SB_FORGOT_PASSWORD_RESET_API -> {
                    var mCommonBean: CommonBean = response.response as CommonBean
                    if (mCommonBean.status == 1) {
                        makeConfirmation(mCommonBean!!.msg)
                    }else if (CommonMethods.isValidString(mCommonBean.msg)) {
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
        CommonMethods.errorDialog(this@ResetPassActivity,strMsg,getResources().getString(R.string.app_name),getResources().getString(R.string.lbl_ok));
    }

    override fun onError(type: String, error: String) {
        errorMsg(error)
        //method body
    }

    fun validateLogin() {
        var strPassword = binding.etPassword.text.toString()
        var strPassword1 =binding.etCPassword.text.toString()
        if (!CommonMethods.isValidString(strPassword)) {
            CommonMethods.setError(binding.etPassword,this@ResetPassActivity,getString(R.string.password_required),getString(R.string.password_required))
        }else if (!CommonMethods.validiate2(
                this.binding.etPassword.getText().toString(),
                "",
                this.binding.etPassword
            )
        ) {

        } else if  (!CommonMethods.isValidString(strPassword1)  || !strPassword1.equals(strPassword))  {
            CommonMethods.setError(
                binding.etCPassword,this@ResetPassActivity,
                getString(R.string.invalid_password),
                getString(R.string.invalid_password)
            )
        }else {
            makeReset(strPassword)
        }
    }

    fun makeReset(vararg strParams: String) {
        if (CommonMethods.isNetworkAvailable(this@ResetPassActivity)) {
              mApiCall!!.resetPassword(this, strParams[0],strEmail,strOTP)
        } else {
            CommonMethods.errorDialog(
                this@ResetPassActivity,
                getResources().getString(R.string.error_internet),
                getResources().getString(R.string.app_name),
                getResources().getString(R.string.lbl_ok)
            )
        }
    }

    private var customDialog: CustomDialog? = null
    private var isDialogOpen = false
     fun makeConfirmation(msg:String){
         if (!isDialogOpen) {
            if (customDialog == null) {
                customDialog = CustomDialog(this@ResetPassActivity,msg,"" ,this, "1")
            }
            isDialogOpen = true
            if (customDialog!!.isShowing) {
                customDialog!!.dismiss()
            }
            customDialog!!.show()
        }
    }
}
