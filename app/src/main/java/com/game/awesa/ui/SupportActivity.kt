package com.game.awesa.ui

import android.content.res.Configuration
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.codersworld.awesalibs.beans.CommonBean
import com.codersworld.awesalibs.beans.county.CountyBean
import com.codersworld.awesalibs.beans.support.SubjectsBean
import com.codersworld.awesalibs.beans.support.TicketsBean
import com.codersworld.awesalibs.listeners.OnResponse
import com.codersworld.awesalibs.rest.ApiCall
import com.codersworld.awesalibs.rest.UniverSelObjct
import com.codersworld.awesalibs.storage.UserSessions
import com.codersworld.awesalibs.utils.CommonMethods
import com.codersworld.awesalibs.utils.Logs
import com.codersworld.awesalibs.utils.Tags
import com.game.awesa.R
import com.game.awesa.databinding.ActivitySupportBinding
import com.game.awesa.ui.adapters.SubjectsAdapter
import com.game.awesa.utils.JBWatcher

class SupportActivity : BaseActivity(), OnResponse<UniverSelObjct> {
     lateinit var binding: ActivitySupportBinding
    var strEmail = ""
    var strMessage = ""
    var strUserId = ""
    var strName = ""
    var strSubject = ""
    var strSubjectId = "0"
    var errorMsg = ""
    var errorEditText: EditText? = null

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
     }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         binding = DataBindingUtil.setContentView(this@SupportActivity, R.layout.activity_support)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.btnSend.setOnClickListener {
            makeSupport()
        }
        getSubjects("")
        binding.etName.addTextChangedListener(JBWatcher(this@SupportActivity,binding.etName,binding.imgRightName,1))
        binding.etEmail.addTextChangedListener(JBWatcher(this@SupportActivity,binding.etEmail,binding.imgRightEmail,1))
    }
    fun makeSupport(){
        strUserId =if ( UserSessions.getUserInfo(this@SupportActivity) !=null ) UserSessions.getUserInfo(this@SupportActivity).id.toString() else "0"
        strName =binding.etName.text.toString().trim()
        strSubjectId = mListSubjects.get(binding.spSubject.selectedItemPosition).id.toString()
        strSubject = mListSubjects.get(binding.spSubject.selectedItemPosition).title.toString()
        strEmail =binding.etEmail.text.toString().trim()
        strMessage =binding.etMessage.text.toString().trim()
        if (this.strName.isEmpty()) {
            this.errorMsg = getString(R.string.error_name);
            this.errorEditText = this.binding.etName;
        } else if (this.strName.length < 3) {
            this.errorMsg = getString(R.string.error_valid_name);
            this.errorEditText = this.binding.etName;
        } else if (this.strEmail.isEmpty()) {
            this.errorMsg = getString(R.string.error_email);
            this.errorEditText = this.binding.etEmail;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(this.binding.etEmail.getText().toString()).matches()) {
            this.errorMsg = getString(R.string.error_valid_email);
            this.errorEditText = this.binding.etEmail;
        } else if (strSubjectId.isEmpty() || strSubjectId.equals("0")) {
            errorMsg(getString(R.string.error_subject_required))
        }  else if (this.strMessage.isEmpty()) {
            this.errorMsg = getString(R.string.error_message);
            this.errorEditText = this.binding.etMessage;
        } else if (this.strMessage.length < 10) {
            this.errorMsg = getString(R.string.error_valid_message);
            this.errorEditText = this.binding.etMessage;
        }else{
            ApiCall(this@SupportActivity).supportTicket(this,true,strUserId,strName,strEmail,strSubjectId,strSubject,strMessage,"1")
        }
    }
    fun setSubjects(){
        if (!CommonMethods.isValidArrayList(mListSubjects)){
            mListSubjects = ArrayList()
            mListSubjects.add(SubjectsBean.InfoBean(0,getString(R.string.lbl_select_subject)))
        }
        binding.spSubject.adapter = SubjectsAdapter(this@SupportActivity,mListSubjects)
    }
    var mListSubjects : ArrayList<SubjectsBean.InfoBean> = ArrayList()
    override fun onSuccess(response: UniverSelObjct) {
        try{
            when(response.methodname){
                Tags.SB_SUPPORT_SUBJECTS_API->{
                    try{
                        var mSubjectsBean = response.response as SubjectsBean
                        if (mSubjectsBean.status==1 && CommonMethods.isValidArrayList(mSubjectsBean.info)){
                            mListSubjects = mSubjectsBean.info;
                        }
                    }catch (ex1:Exception){
                        ex1.printStackTrace()
                    }
                    setSubjects()
                }
                Tags.SB_API_SUPPORT->{
                    try{
                        var mCommonBean = response.response as TicketsBean
                        if (mCommonBean.status==1){
                            binding.llSuccess.visibility=View.VISIBLE
                            binding.llTicket.visibility=View.GONE
                            if (CommonMethods.isValidArrayList(mCommonBean.info)){
                                binding.txtTicketId.visibility=View.VISIBLE
                                binding.txtTicketId.setText(getString(R.string.lbl_ticket,mCommonBean.info[0].id.toString()))
                            }else{
                                binding.txtTicketId.visibility=View.INVISIBLE
                            }
                            binding.txtMessage.setText(mCommonBean.msg)
                        }else{
                            errorMsg(mCommonBean.msg)
                        }
                    }catch (ex1:Exception){
                        ex1.printStackTrace()
                        errorMsg(getString(R.string.something_wrong))
                    }
                }
            }
        }catch (ex:Exception){
            ex.printStackTrace()
            errorMsg(getString(R.string.something_wrong))
        }
    }

    override fun onError(type: String, error: String) {
        when(type){
            Tags.SB_SUPPORT_SUBJECTS_API->{
                setSubjects()
            }
            Tags.SB_API_SUPPORT->{
                errorMsg(error)
            }
        }
     }

    fun errorMsg(strMsg: String) {
        CommonMethods.errorDialog(this@SupportActivity,strMsg,getResources().getString(R.string.app_name),getResources().getString(R.string.lbl_ok));
    }

    fun getSubjects(vararg strParams: String) {
        if (CommonMethods.isNetworkAvailable(this@SupportActivity)) {
            ApiCall(this@SupportActivity).getSubjects(this, true)
        } else {
            errorMsg(getResources().getString(R.string.error_internet));
        }
    }
}