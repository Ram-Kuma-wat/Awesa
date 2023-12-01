package com.game.awesa.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.codersworld.awesalibs.beans.support.SubjectsBean;
import com.game.awesa.R;

import java.util.ArrayList;

public class SubjectsAdapter extends ArrayAdapter<SubjectsBean.InfoBean> {
    ArrayList<SubjectsBean.InfoBean> algorithmList;
    Context context;

    public SubjectsAdapter(Context context2, ArrayList<SubjectsBean.InfoBean> algorithmList2) {
        super(context2, 0, algorithmList2);
        this.context = context2;
        this.algorithmList = algorithmList2;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    private View initView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_subjects, parent, false);
        }
        TextView textViewName = (TextView) convertView.findViewById(R.id.txtName);
        SubjectsBean.InfoBean currentItem = (SubjectsBean.InfoBean) getItem(position);
        if (currentItem != null) {
            textViewName.setText(currentItem.getTitle());
        }
        return convertView;
    }
}
