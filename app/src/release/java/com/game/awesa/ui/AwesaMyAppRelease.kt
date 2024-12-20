package com.game.awesa.ui

import androidx.media3.common.util.UnstableApi
import dagger.hilt.android.HiltAndroidApp

@UnstableApi
@HiltAndroidApp
class AwesaRelease: AWesa() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }