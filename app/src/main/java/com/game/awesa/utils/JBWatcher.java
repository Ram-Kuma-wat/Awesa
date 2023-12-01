package com.game.awesa.utils;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageView;

import com.game.awesa.R;


public class JBWatcher implements TextWatcher {

    public EditText view;
    public ImageView view1;
    public int type;
    public Context mContex;

    public JBWatcher(Context mContex, EditText view, ImageView view1, int type) {
        this.mContex = mContex;
        this.view = view;
        this.view1 = view1;
        this.type = type;
    }

    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    public void afterTextChanged(Editable editable) {
    }

    public void onTextChanged(CharSequence s, int p1, int p2, int p3) {
        if (view.getId() == R.id.etName) {
            if (s.length() > 3) {
                view1.setImageResource(R.drawable.ic_enable);
                return;
            }
            view1.setImageResource(R.drawable.ic_disable);
        } else if (view.getId() == R.id.etEmail) {
            if (s.toString().contains(" ")) {
                if (s.length() <= 1) {
                    view.setText("");
                } else if (s.toString().contains(" ")) {
                    view.setText(s.toString().replace(" ", ""));
                }
            }
            if (s.toString().isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(view.getText().toString()).matches()) {
                view1.setImageResource(R.drawable.ic_disable);
                return;
            }
            view1.setImageResource(R.drawable.ic_enable);
        }else if(view.getId()==R.id.etUsername){
            if (s.toString().contains(" ")) {
                view.setText(s.toString().replace(" ", ""));
                view.setSelection(view.getText().length()); // Set cursor end point
            }
        }
    }
}
