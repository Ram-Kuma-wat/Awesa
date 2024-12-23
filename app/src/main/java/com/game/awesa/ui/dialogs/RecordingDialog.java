package com.game.awesa.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import androidx.databinding.DataBindingUtil;
import com.game.awesa.R;
import com.game.awesa.databinding.RecordingDialogBinding;

public class RecordingDialog extends Dialog {
    RecordingDialogBinding binding;
    Boolean showExtrTime;

    public RecordingDialog(Context activity) {
        super(activity);
        this.showExtrTime = false;
    }

    public RecordingDialog(Context activity, Boolean showExtrTime) {
        super(activity);
        this.showExtrTime = showExtrTime;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = View.inflate(getContext(), R.layout.recording_dialog, null);
        setContentView(view);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        binding = DataBindingUtil.bind(view);

        if(showExtrTime) {
            binding.btnLayout.setOrientation(LinearLayout.VERTICAL);
            binding.btnStartExtraTime.setVisibility(View.VISIBLE);
        } else {
            binding.btnLayout.setOrientation(LinearLayout.HORIZONTAL);
            binding.btnStartExtraTime.setVisibility(View.GONE);
        }
    }

    public void setOnClickRecordInterviewListener(View.OnClickListener l) {
        binding.btnStartInterview.setOnClickListener(l);
    }

    public void setOnClickRecordExtraTimeListener(View.OnClickListener l) {
        binding.btnStartExtraTime.setOnClickListener(l);
    }

    public  void setOnClickEndVideoListener(View.OnClickListener l) {
        binding.btnEndVideo.setOnClickListener(l);
    }
}