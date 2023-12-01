package com.codersworld.awesalibs.listeners;

 import com.codersworld.awesalibs.beans.matches.ReactionsBean;

public interface OnReactionListener {
    void OnReactionAction(ReactionsBean mReactionsBean ,int type,int position);
}
