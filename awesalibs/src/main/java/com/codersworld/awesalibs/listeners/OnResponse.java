package com.codersworld.awesalibs.listeners;


public interface OnResponse<T> {
    void onSuccess(T response);
    void onError(String type, String error);
}
