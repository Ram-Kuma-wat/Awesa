package com.game.awesa.ui

import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import android.util.Log
import androidx.media3.common.util.UnstableApi
import com.game.awesa.utils.SystemVersionUtils
import dagger.hilt.android.HiltAndroidApp

@UnstableApi
@HiltAndroidApp
class MyAppDebug: MyApp() {
    override fun onCreate() {
        super.onCreate()
        enableStrictMode()
    }

    /**
     * enables "strict mode" for testing
     */
    private fun enableStrictMode() {
        StrictMode.setThreadPolicy(
            ThreadPolicy.Builder()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .penaltyFlashScreen()
                .build()
        )

        StrictMode.setVmPolicy(
            VmPolicy.Builder()
                .detectActivityLeaks()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .detectLeakedRegistrationObjects()
                .penaltyLog()
                .apply {
                    if (SystemVersionUtils.isAtLeastP()) {
                        detectNonSdkApiUsage()
                    }
                }
                .build()
        )
        Log.w("MyAppDebug", "Strict mode enabled")
    }
}