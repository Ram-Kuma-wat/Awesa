package com.game.awesa.ui.dashboard.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.codersworld.awesalibs.beans.game.GameBean;
import com.codersworld.awesalibs.beans.teams.TeamsBean;
import com.codersworld.awesalibs.database.DatabaseHelper;
import com.codersworld.awesalibs.database.DatabaseManager;
import com.codersworld.awesalibs.database.dao.GamesCategoryDAO;
import com.codersworld.awesalibs.listeners.OnConfirmListener;
import com.codersworld.awesalibs.listeners.OnPageChangeListener;
import com.codersworld.awesalibs.listeners.OnResponse;
import com.codersworld.awesalibs.listeners.QueryExecutor;
import com.codersworld.awesalibs.rest.ApiCall;
import com.codersworld.awesalibs.rest.UniverSelObjct;
import com.codersworld.awesalibs.storage.UserSessions;
import com.codersworld.awesalibs.utils.CommonMethods;
import com.codersworld.awesalibs.utils.Tags;
import com.game.awesa.R;
import com.game.awesa.databinding.FragmentHomeBinding;
import com.game.awesa.databinding.FragmentSettingsBinding;
import com.game.awesa.ui.SupportActivity;
import com.game.awesa.ui.dashboard.adapter.GamesAdapter;
import com.game.awesa.ui.teams.TeamsActivity;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.ArrayList;

public class FragmentHome extends Fragment implements View.OnClickListener, OnConfirmListener,OnResponse<UniverSelObjct> {
    @NotNull
    public static final String TAG = FragmentHome.class.getSimpleName();

    public FragmentHome() {
        //if required
    }

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
    ApiCall mApiCall=null;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_home, container, false);
        binding = DataBindingUtil.bind(view);
        DatabaseManager.initializeInstance(new DatabaseHelper(requireActivity()));
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
/*        GameBean.InfoBean mBean = new GameBean.InfoBean();
        mBean.setTitle("Hadnball");
        mBean.setId(1);
        mBean.setImage("https://farmkey.in/admin/apk/handball.jpg");
        mBean.setCounty(1);
        mBean.setCounty_id(1);
        mListGames.add(mBean);

        mBean = new GameBean.InfoBean();
        mBean.setTitle("Football");
        mBean.setId(2);
        mBean.setImage("https://farmkey.in/admin/apk/football.jpg");
        mBean.setCounty(2);
        mBean.setCounty_id(0);
        mListGames.add(mBean);*/
        binding.imgFootBall.setOnClickListener(this);
        binding.imgHandBall.setOnClickListener(this);

        initApiCall();
        getGames();
        return view;
    }

    public void initApiCall(){
        if (mApiCall ==null){
            mApiCall=new ApiCall(requireActivity());
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
            if (mListener != null) {
                mListener.onPageChange("home");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.imgFootBall) {
            CommonMethods.moveToNext(requireActivity(), TeamsActivity.class);
        } else if (v.getId() == R.id.imgHandBall) {

        }
    }

    @Override
    public void onConfirm(Boolean isTrue, String type) {

    }

    @Override
    public void onSuccess(UniverSelObjct response) {
        if (response !=null){
            if (response.getMethodname() == Tags.SB_GAME_CATEGORY_API){
                GameBean mBean = (GameBean)response.getResponse();
                if(mBean.getStatus()==1 && CommonMethods.isValidArrayList(mBean.getInfo())){
                    mListGames = mBean.getInfo();
                    if (CommonMethods.isValidArrayList(mListGames)){
                        GamesAdapter mGamesAdapter = new GamesAdapter(requireActivity(),mListGames );
                        binding.rvGames.setLayoutManager(new GridLayoutManager(requireActivity(),2));
                        binding.rvGames.setAdapter(mGamesAdapter);
                    }
                    /*SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
                    DatabaseManager.getInstance().executeQuery(new QueryExecutor() {
                        @Override
                        public void run(SQLiteDatabase database) {
                            GamesCategoryDAO mDAO = new GamesCategoryDAO(database,requireActivity());
                            mDAO.deleteAll();
                            mDAO.insert(mBean.getInfo());
                        }
                    });*/
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

    public void getDBGames(){
        SQLiteDatabase database = DatabaseManager.getInstance().openDatabase();
        GamesCategoryDAO mDAO=new GamesCategoryDAO(database,requireActivity());
        if (mDAO !=null){
            mListGames =mDAO.selectAll();
            DatabaseManager.getInstance().closeDatabase();
            if (CommonMethods.isValidArrayList(mListGames)){
                GamesAdapter mGamesAdapter = new GamesAdapter(requireActivity(),mListGames );
                binding.rvGames.setLayoutManager(new GridLayoutManager(requireActivity(),2));
                binding.rvGames.setAdapter(mGamesAdapter);
            }else{
             //no data found
            }
        }else{
            errorMsg(getString(R.string.something_wrong));
        }
    }
    public void getGames(){
        if (CommonMethods.isNetworkAvailable(requireActivity())) {
            mApiCall.getGames(this, true,UserSessions.getUserInfo(requireActivity()).getId()+"", UserSessions.getFcmToken(requireActivity()));
        }else{
            getDBGames();
        }
    }
}