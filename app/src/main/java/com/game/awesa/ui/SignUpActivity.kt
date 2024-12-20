package com.game.awesa.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.multidex.BuildConfig
import com.codersworld.awesalibs.beans.CommonBean
import com.codersworld.awesalibs.listeners.OnResponse
import com.codersworld.awesalibs.rest.ApiCall
import com.codersworld.awesalibs.rest.UniversalObject
import com.codersworld.awesalibs.storage.UserSessions
import com.codersworld.awesalibs.utils.CommonMethods
import com.codersworld.awesalibs.utils.Logs
import com.codersworld.awesalibs.utils.Tags
import com.game.awesa.R
import com.game.awesa.databinding.ActivitySignupBinding

class SignUpActivity : BaseActivity(),
    OnResponse<UniversalObject> {
    lateinit var binding: ActivitySignupBinding
    var mApiCall: ApiCall? = null

    @Suppress("MagicNumber")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_signup)
        initApiCall()
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.txtSignIn.setOnClickListener { finish() }
        binding.btnSignup.setOnClickListener { makeSignUp() }
        CommonMethods.signupPolicy(getString(R.string.lbl_privacy),
            getString(R.string.lbl_terms_conditions),
            getString(R.string.lbl_privacy_policy)+"." ,
            getString(R.string.lbl_and),
            binding.txtPrivacy,
            this@SignUpActivity,
            {
                val intent = Intent(this@SignUpActivity,WebViewActivity::class.java)
                intent.putExtra("type", 3)
                startActivity(intent)
            }, {
                val intent = Intent(this@SignUpActivity,WebViewActivity::class.java)
                intent.putExtra("type", 1)
                startActivity(intent)
            })

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

    override fun onSuccess(response: UniversalObject) {
        try {
            when (response.methodName) {
                Tags.SB_SIGNUP_API -> {
                    val mCommonBean: CommonBean = response.response as CommonBean
                    if (mCommonBean.status == 1) {
                        val intent = Intent(this@SignUpActivity, VerifyActivity::class.java)
                        intent.putExtra("mCommonBean", mCommonBean)
                        intent.putExtra("strEmail", binding.etEmail.text.toString())
                        intent.putExtra("from","1")
                        startActivity(intent)
                    } else if (CommonMethods.isValidString(mCommonBean.msg)) {
                        errorMsg(mCommonBean.msg)
                    } else {
                        errorMsg(getResources().getString(R.string.something_wrong))
                    }
                }
            }
        } catch (ignored: Exception) {
            errorMsg(getResources().getString(R.string.something_wrong))
        }
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
    }

    private fun makeSignUp() {
        if (!binding.etFirstname.text.isNullOrEmpty()) {
            CommonMethods.setError(
                binding.etFirstname,
                this@SignUpActivity,
                getString(R.string.first_name_required),
                getString(R.string.first_name_required)
            )
        } else if (!binding.etLastname.text.isNullOrEmpty()) {
            CommonMethods.setError(
                binding.etLastname,
                this@SignUpActivity,
                getString(R.string.last_name_required),
                getString(R.string.last_name_required)
            )
        } else if (!binding.etEmail.text.isNullOrEmpty()) {
            CommonMethods.setError(
                binding.etEmail,
                this@SignUpActivity,
                getString(R.string.email_required),
                getString(R.string.email_required)
            )
        } else if (CommonMethods.checkEmail(binding.etEmail.text.toString(), this@SignUpActivity) != -1) {
            CommonMethods.setError(
                binding.etEmail,
                this@SignUpActivity,
                getString(R.string.error_email_invalid),
                getString(R.string.error_email_invalid)
            )
        } else if (!binding.etPhone.text.isNullOrEmpty()) {
            CommonMethods.setError(
                binding.etPhone,
                this@SignUpActivity,
                getString(R.string.phone_required),
                getString(R.string.phone_required)
            )
        } else if (!binding.etUsername.text.isNullOrEmpty()) {
            CommonMethods.setError(
                binding.etUsername,
                this@SignUpActivity,
                getString(R.string.error_username),
                getString(R.string.error_username)
            )
        } else if (!binding.etPassword.text.isNullOrEmpty()) {
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

        } else if(!binding.cbPrivacy.isChecked){
            errorMsg(getString(R.string.error_check_privacy))
        }else {
            val versionName = BuildConfig.VERSION_NAME + "/" + BuildConfig.VERSION_CODE.toString()
            if (CommonMethods.isNetworkAvailable(this@SignUpActivity)) {
                mApiCall!!.userSignUp(
                    this,
                    true,
                    binding.etFirstname.text.toString(),
                    binding.etLastname.text.toString(),
                    binding.etEmail.text.toString(),
                    binding.etPhone.text.toString(),
                    binding.etUsername.text.toString(),
                    binding.etPassword.text.toString(),
                    binding.etPassword.text.toString(),
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