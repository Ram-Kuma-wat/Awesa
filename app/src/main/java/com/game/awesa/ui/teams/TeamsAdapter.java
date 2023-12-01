package com.game.awesa.ui.teams;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.codersworld.awesalibs.beans.teams.TeamsBean;
import com.codersworld.awesalibs.listeners.OnTeamsListener;
import com.codersworld.awesalibs.utils.CommonMethods;
import com.game.awesa.R;
import com.game.awesa.databinding.ItemTeamsBinding;

import java.util.ArrayList;
import java.util.List;

public class TeamsAdapter extends RecyclerView.Adapter {
     public Context context;
     public List<TeamsBean.InfoBean> list=new ArrayList<>();
     public List<TeamsBean.InfoBean> list1=new ArrayList<>();
    OnTeamsListener mListener;
    int type=0;
    int selected=-1;
    public TeamsAdapter(Context context2, List<TeamsBean.InfoBean> list,OnTeamsListener mListener,int type) {
        this.context = context2;
        this.list1 = list;
        this.list.addAll(list1);
        this.mListener = mListener;
        this.type = type;
    }

    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new GameHolder(LayoutInflater.from(this.context).inflate(R.layout.item_teams, viewGroup, false));
    }

    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        GameHolder mHolder = (GameHolder) viewHolder;
        TeamsBean.InfoBean mBean = list.get(i);
        mHolder.binding.txtName.setText(mBean.getTitle());
        CommonMethods.loadImage(context, mBean.getImage(), mHolder.binding.imgTeam);
        if (type==1){
            int color = (selected==mBean.getId())? ContextCompat.getColor(context,R.color.white):ContextCompat.getColor(context,R.color.black);
            mHolder.binding.llView.setBackgroundResource((selected==mBean.getId())?R.drawable.row_selected:R.drawable.row_selector);
            mHolder.binding.txtName.setTextColor(color);
            mHolder.binding.imgArrow.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    public void filter(String str){
        list=new ArrayList<>();
        if (str.length()>0){
            for (int a =0;a<list1.size();a++ ) {
                if (list1.get(a).getTitle().toLowerCase().contains(str.toString().toLowerCase())){
                    list.add(list1.get(a));
                }
            }
        }else{
            list =list1;
        }
        //notify();
        notifyDataSetChanged();
    }
    public int getItemCount() {
        return this.list.size();
    }

    class GameHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ItemTeamsBinding binding;

        public GameHolder(View view) {
            super(view);
            binding = DataBindingUtil.bind(view);
            view.setOnClickListener(this);
        }

        public void onClick(View view) {
            if (mListener !=null){
                mListener.onTeamSelection(list.get(getAdapterPosition()));
                selected = list.get(getAdapterPosition()).getId();
                notifyDataSetChanged();
            }
           //     context.startActivity(new Intent(context, TeamsActivity.class).putExtra("game_category", list.get(getAdapterPosition()).getId() + "").putExtra("county", list.get(getAdapterPosition()).getCounty_id() + ""));
        }
    }
}
