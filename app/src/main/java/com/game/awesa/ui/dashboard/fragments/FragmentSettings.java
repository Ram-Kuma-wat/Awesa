package com.game.awesa.ui.dashboard.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;


import com.codersworld.awesalibs.beans.CommonBean;
import com.codersworld.awesalibs.listeners.OnConfirmListener;
import com.codersworld.awesalibs.listeners.OnPageChangeListener;
import com.codersworld.awesalibs.listeners.OnResponse;
import com.codersworld.awesalibs.rest.ApiCall;
import com.codersworld.awesalibs.rest.UniversalObject;
import com.codersworld.awesalibs.storage.UserSessions;
import com.codersworld.awesalibs.utils.CommonMethods;
import com.codersworld.awesalibs.utils.Tags;
import com.game.awesa.R;
import com.game.awesa.databinding.FragmentSettingsBinding;
import com.game.awesa.ui.LoginActivity;
import com.game.awesa.ui.SupportActivity;
import com.game.awesa.ui.WebViewActivity;
import com.game.awesa.ui.dialogs.CustomDialog;
import com.game.awesa.utils.Global;

import org.jetbrains.annotations.NotNull;


public class FragmentSettings extends Fragment implements View.OnClickListener, OnResponse<UniversalObject>,OnConfirmListener {

    @NotNull
    public static final String TAG = FragmentSettings.class.getSimpleName();

    public FragmentSettings() {
        //if required
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    FragmentSettingsBinding binding;

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    ApiCall mApiCall=null;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_settings, container, false);
        binding = DataBindingUtil.bind(view);
        binding.rlAbout.setOnClickListener(this);
        binding.rlTerms.setOnClickListener(this);
        binding.rlContactUs.setOnClickListener(this);
        binding.rlPrivacy.setOnClickListener(this);
        binding.rlShare.setOnClickListener(this);
        binding.rlLogout.setOnClickListener(this);
        binding.rlDelete.setOnClickListener(this);
        binding.txtVersion.setText(getString(R.string.lbl_version, new Global().getVersionName(requireActivity())));
        initApiCall();

        return view;
    }

    public void initApiCall(){
        if (mApiCall ==null){
            mApiCall=new ApiCall(requireActivity());
        }
    }
    @NotNull
    public static Fragment newInstance() {
        return new FragmentSettings();
    }

    OnPageChangeListener mListener = null;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mListener = (OnPageChangeListener) context;
            if (mListener != null) {
                mListener.onPageChange(R.id.navMore);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == binding.rlAbout) {
            startActivity(new Intent(requireActivity(), WebViewActivity.class).putExtra("type", 2));
        } else  if (v == binding.rlTerms) {
            startActivity(new Intent(requireActivity(), WebViewActivity.class).putExtra("type", 3));
        } else   if (v == binding.rlLogout) {
            CustomDialog customDialog = new CustomDialog(requireActivity(), getString(R.string.lbl_exit_app_msg),getString(R.string.lbl_cancel) ,this, "2");
            customDialog.show();
        } else   if (v == binding.rlDelete) {
            CustomDialog customDialog = new CustomDialog(requireActivity(), getString(R.string.lbl_delete_account_msg),getString(R.string.lbl_cancel) ,this, "1");
            customDialog.show();
        } else if (v == binding.rlContactUs) {
            CommonMethods.moveToNext(requireActivity(), SupportActivity.class);
        } else if (v == binding.rlPrivacy) {
            startActivity(new Intent(requireActivity(), WebViewActivity.class).putExtra("type", 1));
        } else if (v == binding.rlShare) {
            try {
                Uri uri = null;
                try {
                    uri = Uri.parse(MediaStore.Images.Media.insertImage(requireActivity().getContentResolver(), BitmapFactory.decodeResource(getResources(), R.drawable.app_icon), (String) null, (String) null));
                } catch (Exception unused) {
                }
                Intent intent = new Intent();
                intent.setType("image/*");
                if (uri != null) {
                    intent.putExtra("android.intent.extra.STREAM", uri);
                }
                intent.setAction("android.intent.action.SEND");
                //"Still Asking For Bus Routes and places in Jaipur City? Now No More. Download 'JaipurBus & Tourism' https://play.google.com/store/apps/details?id=" +
                intent.putExtra("android.intent.extra.TEXT", getString(R.string.msg_share, requireActivity().getPackageName()));
                startActivity(Intent.createChooser(intent, getString(R.string.lbl_share_via)));

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void onConfirm(Boolean isTrue, String type) {
        if (isTrue) {
            if (type=="1"){
                mApiCall.deleteAccount(this, true,UserSessions.getUserInfo(requireActivity()).getId()+"");
            }else {
                makeLogout();
            }
        }
    }

    public void makeLogout(){
        UserSessions.clearUserInfo(requireActivity());
        startActivity(new Intent(requireActivity(), LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        requireActivity().finishAffinity();
    }


    @Override
    public void onSuccess(UniversalObject response) {
        try {
            if (response !=null){
                if (response.getMethodName()== Tags.SB_DELETE_ACCOUNT_API){
                    CommonBean mCommonBean =(CommonBean) response.getResponse();
                    if (mCommonBean.getStatus()==1){
                        makeLogout();
                    }else if (CommonMethods.isValidString(mCommonBean.getMsg())){
                        errorMsg(mCommonBean.getMsg());
                    }else{
                        errorMsg(getString(R.string.something_wrong));
                    }
                 }
            }
        }catch (Exception ex){
            ex.printStackTrace();
            errorMsg(getString(R.string.something_wrong));
        }
    }

    @Override
    public void onError(String type, String error) {
        errorMsg(getString(R.string.something_wrong));
     }
    public void errorMsg(String strMsg) {
        CommonMethods.errorDialog(requireContext(),strMsg,getResources().getString(R.string.app_name),getResources().getString(R.string.lbl_ok));
    }
}