package com.codersworld.awesalibs.utils

public interface ProgressCallback {
    public fun onSuccess(file: String)
    public fun onError(error: Exception)
    public fun onProgress(progress: Long)
}