package com.codersworld.awesalibs.listeners;


public interface DataResponse<T> {

    void onSuccess(T response);

    void onFaliure(String error);
}
