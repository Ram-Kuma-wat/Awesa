package com.codersworld.awesalibs.listeners;

import com.codersworld.awesalibs.beans.matches.MatchesBean;

public interface OnDeleteVideoListener {
    void onInterviewDelete(int position, MatchesBean.VideosBean mBeanVideo);
    void onActionDelete(int position, MatchesBean.VideosBean mBeanVideo);
}
