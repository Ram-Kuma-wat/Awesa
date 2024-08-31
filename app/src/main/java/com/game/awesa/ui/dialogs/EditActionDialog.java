package com.game.awesa.ui.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.codersworld.awesalibs.beans.matches.ReactionsBean;
import com.codersworld.awesalibs.listeners.OnReactionListener;
import com.game.awesa.R;
import com.game.awesa.databinding.DialogEditActionBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;


public class EditActionDialog extends BottomSheetDialogFragment implements View.OnClickListener {
    BottomSheetBehavior bottomSheetBehavior;
    DialogEditActionBinding binding;
    ReactionsBean mReactionsBean;
    OnReactionListener mOnReactionListener;
    public EditActionDialog(ReactionsBean mReactionsBean,OnReactionListener mOnReactionListener) {
        this.mReactionsBean = mReactionsBean;
        this.mOnReactionListener = mOnReactionListener;
    }

    public EditActionDialog() {

    }
    BottomSheetDialog bottomSheet;
    View mView = null;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        bottomSheet = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = View.inflate(getActivity(), R.layout.dialog_edit_action, null);
        mView = view;
        binding = DataBindingUtil.bind(view);

        bottomSheet.setContentView(view);
        bottomSheet.setCancelable(false);
        bottomSheet.setCanceledOnTouchOutside(false);
        bottomSheetBehavior = BottomSheetBehavior.from((View) (view.getParent()));
        bottomSheetBehavior.setPeekHeight(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {
                if (BottomSheetBehavior.STATE_EXPANDED == i) {

                }
                if (BottomSheetBehavior.STATE_COLLAPSED == i) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    //dismiss();
                }

                if (BottomSheetBehavior.STATE_HIDDEN == i) {
                    dismiss();
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {

            }
        });
        if (mReactionsBean !=null){
            if (mReactionsBean.getReaction().equalsIgnoreCase("goal")){
                binding.rbGoal.setChecked(true);
            }else if (mReactionsBean.getReaction().equalsIgnoreCase("chance")){
                binding.rbChance.setChecked(true);
            }else if (mReactionsBean.getReaction().equalsIgnoreCase("wow")){
                binding.rbWow.setChecked(true);
            }else if (mReactionsBean.getReaction().equalsIgnoreCase("fail")){
                binding.rbFail.setChecked(true);
            }else if (mReactionsBean.getReaction().equalsIgnoreCase("highlight")){
                binding.rbHighlight.setChecked(true);
            }else{
                binding.rbGoal.setChecked(true);
            }
        }
        binding.txtSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.rgAction.getCheckedRadioButtonId()==R.id.rbGoal){
                    mReactionsBean.setReaction("goal");
                }else if (binding.rgAction.getCheckedRadioButtonId()==R.id.rbChance){
                    mReactionsBean.setReaction("chance");
                }else if (binding.rgAction.getCheckedRadioButtonId()==R.id.rbWow){
                    mReactionsBean.setReaction("wow");
                }else if (binding.rgAction.getCheckedRadioButtonId()==R.id.rbFail){
                    mReactionsBean.setReaction("fail");
                }else if (binding.rgAction.getCheckedRadioButtonId()==R.id.rbHighlight){
                    mReactionsBean.setReaction("highlight");
                }
                if (mOnReactionListener !=null){
                    mOnReactionListener.OnReactionAction(mReactionsBean,99,0);
                }
                 dismiss();
            }
        });

        binding.imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 dismiss();
            }
        });

        return bottomSheet;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {

        super.onDismiss(dialog);
    }

    @Override
    public void onStart() {
        super.onStart();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    public void onBackClick() {
        dismiss();
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.imgBack) {
            onBackClick();
        }
    }

}