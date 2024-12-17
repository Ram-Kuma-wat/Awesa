package com.game.awesa.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.databinding.DataBindingUtil;

import com.codersworld.awesalibs.listeners.OnConfirmListener;
import com.codersworld.awesalibs.utils.CommonMethods;
import com.game.awesa.R;
import com.game.awesa.databinding.CustomDialogBinding;


public class CustomDialog extends Dialog implements View.OnClickListener {
    public Context activity;
    CustomDialogBinding binding;
    String strNo = "";
    String strYes = "";
    String strText = "";
    Boolean isCancelable = true;

    public CustomDialog(Context activity, String strText) {
        super(activity);
        this.activity = activity;
        this.strText = strText;
        isCancelable = true;
    }

    OnConfirmListener mListener;
    String mType = "";

    public CustomDialog(Context activity, String strText, String strNo, OnConfirmListener mListener, String mType) {
        super(activity);
        this.mType = mType;
        this.activity = activity;
        this.strText = strText;
        this.strNo = strNo;
        this.mListener = mListener;
        isCancelable = false;
    }

    public CustomDialog(Context activity, String strText, String strNo, String strYes, OnConfirmListener mListener, String mType) {
        super(activity);
        this.mType = mType;
        this.activity = activity;
        this.strText = strText;
        this.strNo = strNo;
        this.strYes = strYes;
        this.mListener = mListener;
        isCancelable = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = View.inflate(getContext(), R.layout.custom_dialog, null);
        setContentView(view);
        setCancelable(isCancelable);
        setCanceledOnTouchOutside(isCancelable);
        binding = DataBindingUtil.bind(view);
        if (strText != null && !strText.isEmpty()) {
            CommonMethods.textWithHtml(binding.txtTitle, strText);
        } else {
            CommonMethods.textWithHtml(binding.txtTitle, activity.getResources().getString(R.string.app_name));
        }
        if (strNo != null && !strNo.isEmpty()) {
            binding.btnNegative.setText(strNo);
            binding.btnPositive.setText(activity.getResources().getString(R.string.lbl_ok));
            binding.btnNegative.setVisibility(View.VISIBLE);
        } else {
            binding.btnNegative.setVisibility(View.GONE);
        }
        binding.btnPositive.setText((CommonMethods.isValidString(strYes)) ? strYes : activity.getResources().getString(R.string.lbl_ok));

        binding.btnNegative.setOnClickListener(this);
        binding.btnPositive.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnPositive) {
            if (mListener != null) {
                mListener.onConfirm(true, mType);
                dismiss();
            } else {
                dismiss();
            }
        } else if (id == R.id.btnNegative) {
            if (mListener != null) {
                mListener.onConfirm(false, mType);
                dismiss();
            } else {
                dismiss();
            }
        }
        dismiss();
    }
}