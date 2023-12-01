package com.game.awesa.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.multidex.BuildConfig
import com.codersworld.awesalibs.beans.CommonBean
import com.codersworld.awesalibs.listeners.OnResponse
import com.codersworld.awesalibs.rest.ApiCall
import com.codersworld.awesalibs.rest.UniverSelObjct
import com.codersworld.awesalibs.storage.UserSessions
import com.codersworld.awesalibs.utils.CommonMethods
import com.codersworld.awesalibs.utils.Logs
import com.codersworld.awesalibs.utils.Tags
import com.game.awesa.R
import com.game.awesa.databinding.ActivitySignupBinding


class SignUpActivity : BaseActivity(),
    OnResponse<UniverSelObjct> {
    lateinit var binding: ActivitySignupBinding
    var mApiCall: ApiCall? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_signup)
        initApiCall()
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.txtSignIn.setOnClickListener { finish() }
        binding.btnSignup.setOnClickListener { makeSignUp() }
       /* if (binding.txtPrivacy.text.toString().contains(getString(R.string.lbl_terms_conditions))) {*/
            CommonMethods.signupPolicy(getString(R.string.lbl_privacy) ,getString(R.string.lbl_terms_conditions) ,getString(R.string.lbl_privacy_policy)+"." ,getString(R.string.lbl_and) ,binding.txtPrivacy,this@SignUpActivity,
                View.OnClickListener { // TODO: do your stuff here
                    startActivity(Intent(this@SignUpActivity,WebViewActivity::class.java).putExtra("type", 3))
                },View.OnClickListener { // TODO: do your stuff here
                    startActivity(Intent(this@SignUpActivity,WebViewActivity::class.java).putExtra("type", 1))
                })
      //  }
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (!Patterns.EMAIL_ADDRESS.matcher(binding.etEmail.text.toString()).matches()) {
                    binding.etEmail.setTextColor(
                        ContextCompat.getColor(
                            this@SignUpActivity,
                            R.color.red
                        )
                    )
                } else {
                    binding.etEmail.setTextColor(
                        ContextCompat.getColor(
                            this@SignUpActivity,
                            R.color.black
                        )
                    )
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
    }

    fun initApiCall() {
        if (mApiCall == null) {
            mApiCall = ApiCall(this@SignUpActivity)
        }
    }

    override fun onSuccess(response: UniverSelObjct) {
        try {
            Logs.e(response.methodname.toString())
            when (response.methodname) {
                Tags.SB_SIGNUP_API -> {
                    var mCommonBean: CommonBean = response.response as CommonBean
                    if (mCommonBean.status == 1) {
                        startActivity(Intent(this@SignUpActivity,VerifyActivity::class.java).putExtra("mCommonBean", mCommonBean).putExtra("strEmail", binding.etEmail.text.toString()).putExtra("from","1"))
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
            this@SignUpActivity,
            strMsg,
            getResources().getString(R.string.app_name),
            getResources().getString(R.string.lbl_ok),
        );
    }

    override fun onError(type: String, error: String) {
        errorMsg(error)
        //method body
    }

    fun makeSignUp() {
        var strFName = binding.etFirstname.text.toString()
        var strLName = binding.etLastname.text.toString()
        var strEmail = binding.etEmail.text.toString()
        var strUsername = binding.etUsername.text.toString()
        var strPassword = binding.etPassword.text.toString()
        var strPhone = binding.etPhone.text.toString()
        if (!CommonMethods.isValidString(strFName)) {
            CommonMethods.setError(
                binding.etFirstname,
                this@SignUpActivity,
                getString(R.string.first_name_required),
                getString(R.string.first_name_required)
            )
        } else if (!CommonMethods.isValidString(strLName)) {
            CommonMethods.setError(
                binding.etLastname,
                this@SignUpActivity,
                getString(R.string.last_name_required),
                getString(R.string.last_name_required)
            )
        } else if (!CommonMethods.isValidString(strEmail)) {
            CommonMethods.setError(
                binding.etEmail,
                this@SignUpActivity,
                getString(R.string.email_required),
                getString(R.string.email_required)
            )
        } else if (CommonMethods.checkEmail(strEmail, this@SignUpActivity) != -1) {
            CommonMethods.setError(
                binding.etEmail,
                this@SignUpActivity,
                getString(R.string.error_email_invalid),
                getString(R.string.error_email_invalid)
            )
        } else if (!CommonMethods.isValidString(strPhone)) {
            CommonMethods.setError(
                binding.etPhone,
                this@SignUpActivity,
                getString(R.string.phone_required),
                getString(R.string.phone_required)
            )
        } /*else if (CommonMethods.checkPhone(strPhone, this@SignUpActivity) != 0) {
            CommonMethods.setError(
                binding.etPhone,
                this@SignUpActivity,
                getString(R.string.error_phone_invalid),
                getString(R.string.error_phone_invalid)
            )
        } */else if (!CommonMethods.isValidString(strUsername)) {
            CommonMethods.setError(
                binding.etUsername,
                this@SignUpActivity,
                getString(R.string.error_username),
                getString(R.string.error_username)
            )
        } else if (!CommonMethods.isValidString(strPassword)) {
            CommonMethods.setError(
                binding.etPassword,
                this@SignUpActivity,
                getString(R.string.password_required),
                getString(R.string.password_required)
            )
        } else if (!CommonMethods.validiate2(
                this.binding.etPassword.getText().toString(),
                this.binding.etFirstname.getText().toString(),
                this.binding.etPassword
            )
        ) {

        } else if(binding.cbPrivacy.isChecked == false){
            errorMsg(getString(R.string.error_check_privacy))
        }else {
            var versionName = BuildConfig.VERSION_NAME + "/" + BuildConfig.VERSION_CODE.toString()
            if (CommonMethods.isNetworkAvailable(this@SignUpActivity)) {
               // CommonMethods.moveToNext(this@SignUpActivity,VerifyActivity::class.java)
            //    firstname,lastname,email,phone,username,pass,conm_pass,device_id,version
                mApiCall!!.userSignUp(
                    this,true, strFName, strLName, strEmail, strPhone, strUsername, strPassword,strPassword,
                    UserSessions.getFcmToken(this@SignUpActivity), versionName
                )
            } else {
                CommonMethods.errorDialog(
                    this@SignUpActivity,
                    getResources().getString(R.string.error_internet),
                    getResources().getString(R.string.app_name),
                    getResources().getString(R.string.lbl_ok),
                );
            }
        }
    }
}