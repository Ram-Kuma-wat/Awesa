package com.game.awesa.ui.dashboard.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.codersworld.awesalibs.beans.game.GameBean;
import com.codersworld.awesalibs.utils.CommonMethods;
import com.game.awesa.R;
import com.game.awesa.databinding.ItemGameBinding;
import com.game.awesa.ui.county.CountyActivity;
import com.game.awesa.ui.league.LeagueActivity;
import com.game.awesa.ui.teams.TeamsActivity;

import java.util.List;

public class GamesAdapter extends RecyclerView.Adapter {
     public Context context;
     public List<GameBean.InfoBean> list;

    public GamesAdapter(Context context2, List<GameBean.InfoBean> list) {
        this.context = context2;
        this.list = list;
    }

    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new GameHolder(LayoutInflater.from(this.context).inflate(R.layout.item_game, viewGroup, false));
    }

    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        GameHolder mHolder = (GameHolder) viewHolder;
        GameBean.InfoBean mBean = list.get(i);
        mHolder.binding.txtGameTitle.setText(mBean.getTitle());
        CommonMethods.loadImage(context, mBean.getImage(), mHolder.binding.imgGame);
    }

    public int getItemCount() {
        return this.list.size();
    }

    class GameHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ItemGameBinding binding;

        public GameHolder(View view) {
            super(view);
            binding = DataBindingUtil.bind(view);
            view.setOnClickListener(this);
        }

        public void onClick(View view) {
            if(list.get(getAdapterPosition()).getCounty()>1) {
                context.startActivity(new Intent(context, CountyActivity.class).putExtra("game_category", list.get(getAdapterPosition()).getId() + ""));
            }else{
                context.startActivity(new Intent(context, LeagueActivity.class).putExtra("game_category", list.get(getAdapterPosition()).getId() + "").putExtra("county", list.get(getAdapterPosition()).getCounty_id() + ""));
            }
        }
    }
}
