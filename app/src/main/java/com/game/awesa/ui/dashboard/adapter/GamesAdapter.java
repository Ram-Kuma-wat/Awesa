package com.game.awesa.ui.dashboard.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.media3.common.util.UnstableApi;
import androidx.recyclerview.widget.RecyclerView;

import com.codersworld.awesalibs.beans.game.GameBean;
import com.codersworld.awesalibs.beans.matches.MatchesBean;
import com.codersworld.awesalibs.utils.CommonMethods;
import com.game.awesa.BuildConfig;
import com.game.awesa.R;
import com.game.awesa.databinding.ItemGameBinding;
import com.game.awesa.ui.county.CountyActivity;
import com.game.awesa.ui.league.LeagueActivity;
import com.game.awesa.ui.recorder.CameraActivity;
import com.game.awesa.ui.recorder.TutorialActivity;

import java.util.List;

public class GamesAdapter extends RecyclerView.Adapter {
     public Context context;
     public List<GameBean.InfoBean> list;

    public GamesAdapter(Context context2, List<GameBean.InfoBean> list) {
        this.context = context2;
        this.list = list;
    }

    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new GameHolder(LayoutInflater.from(this.context).inflate(R.layout.item_game, viewGroup, false));
    }

    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
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

        @UnstableApi
        public void onClick(View view) {
            if(list.get(getBindingAdapterPosition()).getCounty()>1) {
                context.startActivity(new Intent(context, CountyActivity.class).putExtra("game_category", list.get(getAdapterPosition()).getId() + ""));
            } else {
                if (BuildConfig.DEBUG) {
                    Intent intent = new Intent(context, TutorialActivity.class);
                    MatchesBean.InfoBean mMatchBean = new MatchesBean.InfoBean();
                    mMatchBean.setId(32);
                    mMatchBean.setCounty_id(1);
                    mMatchBean.setCounty_title("Nairobi");
                    mMatchBean.setLeague_id(3);
                    mMatchBean.setLeague_title("KPL");
                    mMatchBean.setOpponent_team_id(1);
                    mMatchBean.setTeam_id(2);
                    mMatchBean.setTeam1("Manchester");
                    mMatchBean.setTeam2("Arsenal");
                    mMatchBean.setUser_id(32);
                    intent.putExtra(TutorialActivity.EXTRA_MATCH_BEAN, mMatchBean);
                    context.startActivity(intent);
                    return;
                }

                Intent intent = new Intent(context, LeagueActivity.class);
                intent.putExtra("game_category", list.get(getBindingAdapterPosition()).getId() + "");
                intent.putExtra("county", list.get(getBindingAdapterPosition()).getCounty_id() + "");
                context.startActivity(intent);
            }
        }
    }
}
