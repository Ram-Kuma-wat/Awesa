package com.codersworld.awesalibs.mp4compose.source;

import androidx.annotation.NonNull;

import java.io.FileDescriptor;

public interface DataSource {
    @NonNull
    FileDescriptor getFileDescriptor();

    interface Listener {
        void onError(Exception e);
    }
}
