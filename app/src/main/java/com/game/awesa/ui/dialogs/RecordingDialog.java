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
    Boolean showExtraTime;

    public RecordingDialog(Context activity) {
        super(activity);
        this.showExtraTime = false;
    }

    public RecordingDialog(Context activity, Boolean showExtraTime) {
        super(activity);
        this.showExtraTime = showExtraTime;
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
        View v = getWindow().getDecorView();
        v.setBackgroundResource(android.R.color.transparent);

        if(showExtraTime) {
            binding.btnStartExtraTime.setVisibility(View.VISIBLE);
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