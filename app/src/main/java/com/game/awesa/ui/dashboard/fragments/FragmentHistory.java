package com.game.awesa.ui.dashboard.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.codersworld.awesalibs.beans.matches.MatchesBean;
import com.codersworld.awesalibs.listeners.OnMatchListener;
import com.codersworld.awesalibs.listeners.OnPageChangeListener;
import com.codersworld.awesalibs.listeners.OnResponse;
import com.codersworld.awesalibs.rest.ApiCall;
import com.codersworld.awesalibs.rest.UniverSelObjct;
import com.codersworld.awesalibs.storage.UserSessions;
import com.codersworld.awesalibs.utils.CommonMethods;
import com.codersworld.awesalibs.utils.Tags;
import com.game.awesa.R;
import com.game.awesa.databinding.FragmentHistoryBinding;
import com.game.awesa.databinding.FragmentSettingsBinding;
import com.game.awesa.ui.dashboard.adapter.HistoryAdapter;
import com.game.awesa.ui.matches.MatchDetailActivity;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class FragmentHistory extends Fragment implements SwipeRefreshLayout.OnRefreshListener, OnResponse<UniverSelObjct>, OnMatchListener {
    ArrayList<MatchesBean.InfoBean> mListMatches = new ArrayList<>();

    @NotNull
    public static final String TAG = FragmentHistory.class.getSimpleName();

    public FragmentHistory() {
        //if required
    }

    String game_category = "";
    String game_id = "";

    public void getMatches(int page) {
        if (page == 1) {
            mListMatches = new ArrayList<>();
        } else {
            binding.loadingProgress.setVisibility(View.VISIBLE);
        }

        //search user_id game_category  game_id
        new ApiCall(requireActivity()).getMatches(this, (page == 1) ? true : false, page + "", "",
                UserSessions.getUserInfo(requireActivity()).getId() + "", game_category, game_id);
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
        binding.mNestedScroll.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                int diff = binding.mNestedScroll.getChildAt(binding.mNestedScroll.getChildCount() - 1).getBottom() - (binding.mNestedScroll.getHeight() + binding.mNestedScroll.getScrollY());
                if (diff == 0) {
                    if (mPage < TOTAL_PAGES && isLoadingsBar == false) {
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
            if (mListener != null) {
                mListener.onPageChange("history");
            }
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
    public void onSuccess(UniverSelObjct response) {
        try {
            if (response != null) {
                if (response.getMethodname() == Tags.SB_USER_MATCHES_API) {
                    MatchesBean mBeanMatches = (MatchesBean) response.getResponse();
                    if (mBeanMatches.getStatus() == 1 && CommonMethods.isValidArrayList(mBeanMatches.getInfo())) {
                        TOTAL_PAGES = mBeanMatches.getInfo().get(0).getTotal_rows();
                        if (mPage == TOTAL_PAGES) {
                            isLastPageBar = true;
                        } else {
                            isLastPageBar = false;
                        }
                        setPagination();
                        mListMatches.addAll(mBeanMatches.getInfo());
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