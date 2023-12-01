package com.game.awesa.ui.league.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.codersworld.awesalibs.beans.leagues.LeagueBean;
import com.codersworld.awesalibs.listeners.OnLeagueListener;
import com.codersworld.awesalibs.utils.CommonMethods;
import com.game.awesa.R;
import com.game.awesa.databinding.ItemLeagueBinding;
import java.util.List;

public class LeagueAdapter extends RecyclerView.Adapter {
    public Context context;
    public List<LeagueBean.InfoBean> list;
    OnLeagueListener mListener;

    public LeagueAdapter(Context context2, List<LeagueBean.InfoBean> list, OnLeagueListener mListener) {
        this.context = context2;
        this.mListener = mListener;
        this.list = list;
    }

    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new GameHolder(LayoutInflater.from(this.context).inflate(R.layout.item_league, viewGroup, false));
    }

    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        GameHolder mHolder = (GameHolder) viewHolder;
        LeagueBean.InfoBean mBean = list.get(i);
        mHolder.binding.txtName.setText(mBean.getTitle());
        CommonMethods.loadImage(context, mBean.getImage(), mHolder.binding.imgLeague);
    }

    public int getItemCount() {
        return this.list.size();
    }

    class GameHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ItemLeagueBinding binding;

        public GameHolder(View view) {
            super(view);
            binding = DataBindingUtil.bind(view);
            view.setOnClickListener(this);
        }

        public void onClick(View view) {
            if (mListener != null) {
                mListener.onLeagueSelection(list.get(getAdapterPosition()));
            }
            //     context.startActivity(new Intent(context, TeamsActivity.class).putExtra("game_category", list.get(getAdapterPosition()).getId() + "").putExtra("county", list.get(getAdapterPosition()).getCounty_id() + ""));
        }
    }
}
