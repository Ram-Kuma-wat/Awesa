package com.codersworld.awesalibs.listeners;

import com.codersworld.awesalibs.beans.county.CountyBean;
import com.codersworld.awesalibs.beans.matches.MatchesBean;

public interface OnMatchListener {
    void onMatchClick(MatchesBean.InfoBean mBeanMatch);
    void onVideoClick(MatchesBean.VideosBean mBeanVideo);
    void onVideoDelete(int position,MatchesBean.VideosBean mBeanVideo);
}
