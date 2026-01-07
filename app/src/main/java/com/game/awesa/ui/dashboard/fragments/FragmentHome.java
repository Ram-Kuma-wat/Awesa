package com.game.awesa.ui.dashboard.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.codersworld.awesalibs.beans.game.GameBean;
import com.codersworld.awesalibs.beans.matches.MatchesBean;
import com.codersworld.awesalibs.beans.user.UserBean;
import com.codersworld.awesalibs.database.DatabaseHelper;
import com.codersworld.awesalibs.database.DatabaseManager;
import com.codersworld.awesalibs.database.dao.GamesCategoryDAO;
import com.codersworld.awesalibs.listeners.OnConfirmListener;
import com.codersworld.awesalibs.listeners.OnPageChangeListener;
import com.codersworld.awesalibs.listeners.OnResponse;
import com.codersworld.awesalibs.rest.ApiCall;
import com.codersworld.awesalibs.rest.UniversalObject;
import com.codersworld.awesalibs.storage.UserSessions;
import com.codersworld.awesalibs.utils.CommonMethods;
import com.codersworld.awesalibs.utils.Tags;
import com.game.awesa.R;
import com.game.awesa.databinding.FragmentHomeBinding;
import com.game.awesa.services.VideoUploadService;
import com.game.awesa.ui.LoginActivity;
import com.game.awesa.ui.SupportActivity;
import com.game.awesa.ui.dashboard.adapter.GamesAdapter;
import com.game.awesa.ui.recorder.CameraActivity;
import com.game.awesa.ui.teams.TeamsActivity;
import com.game.awesa.utils.Global;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FragmentHome extends Fragment implements View.OnClickListener, OnConfirmListener,OnResponse<UniversalObject> {

    @Inject DatabaseManager databaseManager;

    @NotNull
    public static final String TAG = FragmentHome.class.getSimpleName();

    public FragmentHome() {}

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    ArrayList<GameBean.InfoBean> mListGames = new ArrayList<>();
    FragmentHomeBinding binding;
    ApiCall mApiCall = null;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_home, container, false);
        binding = DataBindingUtil.bind(view);
        if (binding.txtAddSports.getText().toString().contains(getString(R.string.lbl_contact_us) + ".")) {
            CommonMethods.setClickableHighLightedText(binding.txtAddSports,
                    getString(R.string.lbl_contact_us) + ".", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CommonMethods.moveToNext(requireActivity(), SupportActivity.class);
                        }
                    });
        }
        mListGames = new ArrayList<>();

        binding.imgFootBall.setOnClickListener(this);
        binding.imgHandBall.setOnClickListener(this);

        initApiCall();
        getGames();
/*
        MatchesBean mMatchesBean= new Gson().fromJson("{\"status\":1,\"msg\":\"Spiel erfolgreich erstellt.\",\"info\":[{\"id\":\"672\",\"user_id\":\"38\",\"game_category\":\"1\",\"county_id\":\"1\",\"league_id\":\"17\",\"team_id\":\"94\",\"opponent_team_id\":\"159\",\"total_actions\":\"0\",\"location_type\":\"1\",\"interview\":\"\",\"interview_thumbnail\":\"\",\"status\":\"1\",\"created_date\":\"06 January 2026\",\"modified_date\":\"2026-01-06 13:37:42\",\"county_title\":\"Hameln-Pyrmont\",\"county_image\":\"https://test.awesa-vereins.tv/public/uploads/counties/29Feb2024/Download (1).jpg\",\"league_title\":\"Testspiele\",\"league_image\":\"https://test.awesa-vereins.tv/public/uploads/leagues/24Jul2024/Test-Team.png\",\"game_title\":\"Fussball\",\"game_image\":\"https://test.awesa-vereins.tv/public/uploads/gameCategory/15Aug2023/awesa-football.png\",\"team1\":\"Test-Gegner\",\"team1_image\":\"https://test.awesa-vereins.tv/public/uploads/teams/12Jul2024/Test-Team.png\",\"team2\":\"TSV Lüntorf II\",\"team2_image\":\"https://test.awesa-vereins.tv/public/uploads/teams/18Jul2024/TSV Lüntorf_1.png\"}]}", MatchesBean.class);
        Intent intent = new Intent(requireActivity(), CameraActivity.class);
        intent.putExtra("MatchBean", mMatchesBean.getInfo().get(0));
        intent.putExtra("mHalf", 1);
        startActivity(intent);
*/

        return view;
    }

    public void initApiCall() {
        if (mApiCall == null){
            mApiCall= new ApiCall(requireActivity());
        }
    }
    @NotNull
    public static Fragment newInstance() {
        return new FragmentHome();
    }

    OnPageChangeListener mListener = null;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mListener = (OnPageChangeListener) context;
            mListener.onPageChange(R.id.navHome);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.imgFootBall) {
            CommonMethods.moveToNext(requireActivity(), TeamsActivity.class);
        } else if (v.getId() == R.id.imgHandBall) {}
    }

    @Override
    public void onConfirm(Boolean isTrue, String type) {
        if (isTrue){
            if (type.equalsIgnoreCase("99")){
                UserSessions.clearUserInfo(requireActivity());
                startActivity(new Intent(requireActivity(), LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                requireActivity().finishAffinity();
            }
        }
    }

    @Override
    public void onSuccess(UniversalObject response) {
        if (response !=null){
            if (response.getMethodName() == Tags.SB_GAME_CATEGORY_API){
                GameBean mBean = (GameBean)response.getResponse();
                //Log.e("mBean",new Gson().toJson(mBean));
                if(mBean.getStatus()==1 && CommonMethods.isValidArrayList(mBean.getInfo())){
                    mListGames = mBean.getInfo();
                    if (CommonMethods.isValidArrayList(mListGames)){
                        GamesAdapter mGamesAdapter = new GamesAdapter(requireActivity(),mListGames );
                        binding.rvGames.setLayoutManager(new GridLayoutManager(requireActivity(),2));
                        binding.rvGames.setAdapter(mGamesAdapter);
                    }
                }else if(mBean.getStatus() == 99){
                    UserSessions.clearUserInfo(requireActivity());
                    new Global().makeConfirmation(mBean.getMsg(),requireActivity(),this);
                }
                //getDBGames();
            }
        }
    }
    public void errorMsg(String strMsg) {
        CommonMethods.errorDialog(requireContext(),strMsg,getResources().getString(R.string.app_name),getResources().getString(R.string.lbl_ok));
    }
    @Override
    public void onError(String type, String error) {
        getDBGames();
    }

    public void getDBGames() {
        SQLiteDatabase database = databaseManager.openDatabase();
        GamesCategoryDAO mDAO=new GamesCategoryDAO(database, requireActivity());
        if (mDAO !=null){
            mListGames =mDAO.selectAll();
            databaseManager.closeDatabase();
            if (CommonMethods.isValidArrayList(mListGames)){
                GamesAdapter mGamesAdapter = new GamesAdapter(requireActivity(),mListGames );
                binding.rvGames.setLayoutManager(new GridLayoutManager(requireActivity(),2));
                binding.rvGames.setAdapter(mGamesAdapter);
            }
        } else {
            errorMsg(getString(R.string.something_wrong));
        }
    }
    public void getGames() {
        if (CommonMethods.isNetworkAvailable(requireActivity())) {
            mApiCall.getGames(this, true,UserSessions.getUserInfo(requireActivity()).getId()+"", UserSessions.getFcmToken(requireActivity()));
        }else{
            getDBGames();
        }
    }
}
