package com.game.awesa.ui.dashboard.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.codersworld.awesalibs.beans.matches.MatchesBean;
import com.codersworld.awesalibs.listeners.OnConfirmListener;
import com.codersworld.awesalibs.listeners.OnMatchListener;
import com.codersworld.awesalibs.listeners.OnPageChangeListener;
import com.codersworld.awesalibs.listeners.OnResponse;
import com.codersworld.awesalibs.rest.ApiCall;
import com.codersworld.awesalibs.rest.UniversalObject;
import com.codersworld.awesalibs.storage.UserSessions;
import com.codersworld.awesalibs.utils.CommonMethods;
import com.codersworld.awesalibs.utils.Tags;
import com.game.awesa.R;
import com.game.awesa.databinding.FragmentHistoryBinding;
import com.game.awesa.ui.LoginActivity;
import com.game.awesa.ui.dashboard.adapter.HistoryAdapter;
import com.game.awesa.ui.matches.MatchDetailActivity;
import com.game.awesa.utils.Global;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class FragmentHistory extends Fragment implements SwipeRefreshLayout.OnRefreshListener, OnResponse<UniversalObject>, OnMatchListener, OnConfirmListener {
    ArrayList<MatchesBean.InfoBean> mListMatches = new ArrayList<>();

    @NotNull
    public static final String TAG = FragmentHistory.class.getSimpleName();

    public FragmentHistory() {}

    String gameCategory = "";
    String gameId = "";

    public void getMatches(int page) {
        if (page == 1) {
            mListMatches = new ArrayList<>();
        } else {
            binding.loadingProgress.setVisibility(View.VISIBLE);
        }

        Integer userId = UserSessions.getUserInfo(requireActivity()).getId();

        if(userId != null) {
            new ApiCall(requireActivity()).getMatches(this, !binding.swRefresh.isRefreshing(), page + "", "",
                    userId.toString(), gameCategory, gameId);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    FragmentHistoryBinding binding;
    HistoryAdapter mAdapter = null;

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_history, container, false);
        binding = DataBindingUtil.bind(view);
        binding.swRefresh.setOnRefreshListener(this);
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false));
        mListMatches = new ArrayList<>();
        mAdapter = new HistoryAdapter(requireActivity(), mListMatches, this);
        binding.rvHistory.setAdapter(mAdapter);
        setPagination();
        getMatches(mPage);
        return view;
    }


    public void setPagination() {
        binding.mNestedScroll.getViewTreeObserver().addOnScrollChangedListener(() -> {
            int diff = binding.mNestedScroll.getChildAt(binding.mNestedScroll.getChildCount() - 1).getBottom() - (binding.mNestedScroll.getHeight() + binding.mNestedScroll.getScrollY());
            if (diff == 0) {
                if (mPage < TOTAL_PAGES && !isLoadingsBar) {
                    isLoadingsBar = true;
                    mPage += 1;
                    binding.loadingProgress.setVisibility(View.VISIBLE);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getMatches(mPage);
                        }
                    }, 1000);
                }
            }
        });
    }

    @NotNull
    public static Fragment newInstance() {
        return new FragmentHistory();
    }

    OnPageChangeListener mListener = null;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mListener = (OnPageChangeListener) context;
            mListener.onPageChange("history");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRefresh() {
        mPage = 1;
        binding.swRefresh.setRefreshing(true);
        getMatches(mPage);
    }

    int mPage = 1;
    private int TOTAL_PAGES = 0;
    private Boolean isLoadingsBar = false;
    private Boolean isLastPageBar = false;

    @Override
    public void onSuccess(UniversalObject response) {
        try {
            if (response != null) {
                if (response.getMethodName().equals(Tags.SB_USER_MATCHES_API)) {
                    MatchesBean mBeanMatches = (MatchesBean) response.getResponse();
                    if (mBeanMatches.getStatus() == 1 && CommonMethods.isValidArrayList(mBeanMatches.getInfo())) {
                        TOTAL_PAGES = mBeanMatches.getInfo().get(0).getTotal_rows();
                        isLastPageBar = mPage == TOTAL_PAGES;
                        setPagination();
                        mListMatches.addAll(mBeanMatches.getInfo());
                    } else if(mBeanMatches.getStatus() == 99) {
                        UserSessions.clearUserInfo(requireActivity());
                        new Global().makeConfirmation(mBeanMatches.getMsg(),requireActivity(),this);
                    }
                    checkData();
                }
            }
        } catch (Exception ex) {
            binding.loadingProgress.setVisibility(View.GONE);
            binding.swRefresh.setRefreshing(false);
            ex.printStackTrace();
        }
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
    public void onError(String type, String error) {
        checkData();
    }

    public void checkData() {
        binding.loadingProgress.setVisibility(View.GONE);
        binding.swRefresh.setRefreshing(false);
        if (CommonMethods.isValidArrayList(mListMatches)) {
            if (mAdapter != null) {
                mAdapter.addAll(mListMatches);
            }
        }
        if (mAdapter != null) {
            if (mAdapter.checkSize() > 0) {
                CommonMethods.changeView(binding.mNestedScroll, binding.llNoData);
            } else {
                CommonMethods.changeView(binding.llNoData, binding.mNestedScroll);
            }
        } else {
            CommonMethods.changeView(binding.llNoData, binding.mNestedScroll);
        }
    }

    @Override
    public void onMatchClick(MatchesBean.InfoBean mBeanMatch) {
        startActivity(new Intent(requireActivity(), MatchDetailActivity.class).putExtra("matchBean", mBeanMatch).putExtra("game_id", mBeanMatch.getId() + ""));
    }

    @Override
    public void onVideoClick(MatchesBean.VideosBean mBeanVideo) {

    }
   @Override
    public void onVideoDelete(int positionVideo,MatchesBean.VideosBean mBeanVideo) {

    }

}
