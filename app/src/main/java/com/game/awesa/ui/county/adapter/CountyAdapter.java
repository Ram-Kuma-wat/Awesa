package com.game.awesa.ui.county.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.codersworld.awesalibs.beans.county.CountyBean;
import com.codersworld.awesalibs.listeners.OnCountyListener;
import com.codersworld.awesalibs.utils.CommonMethods;
import com.game.awesa.R;
import com.game.awesa.databinding.ItemCountyBinding;
import com.game.awesa.databinding.ItemTeamsBinding;

import java.util.List;

public class CountyAdapter extends RecyclerView.Adapter {
     public Context context;
     public List<CountyBean.InfoBean> list;
OnCountyListener mListener;
    public CountyAdapter(Context context2, List<CountyBean.InfoBean> list,OnCountyListener mListener) {
        this.context = context2;
        this.mListener = mListener;
        this.list = list;
    }

    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new GameHolder(LayoutInflater.from(this.context).inflate(R.layout.item_county, viewGroup, false));
    }

    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        GameHolder mHolder = (GameHolder) viewHolder;
        CountyBean.InfoBean mBean = list.get(i);
        mHolder.binding.txtName.setText(mBean.getTitle());
        CommonMethods.loadImage(context, mBean.getImage(), mHolder.binding.imgCounty);
    }

    public int getItemCount() {
        return this.list.size();
    }

    class GameHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ItemCountyBinding binding;

        public GameHolder(View view) {
            super(view);
            binding = DataBindingUtil.bind(view);
            view.setOnClickListener(this);
        }

        public void onClick(View view) {
            if (mListener !=null){
                mListener.onCountySelection(list.get(getAdapterPosition()));
            }
           //     context.startActivity(new Intent(context, TeamsActivity.class).putExtra("game_category", list.get(getAdapterPosition()).getId() + "").putExtra("county", list.get(getAdapterPosition()).getCounty_id() + ""));
        }
    }
}
