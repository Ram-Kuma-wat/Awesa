package com.game.awesa.utils

import android.app.Application
import android.content.Intent
import androidx.lifecycle.Lifecycle.State.STARTED
import androidx.lifecycle.ProcessLifecycleOwner
import com.game.awesa.di.AppCoroutineScope
import com.game.awesa.ui.dashboard.MainActivity
import dagger.android.DispatchingAndroidInjector
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@Suppress("TooManyFunctions")
class AppInitializer @Inject constructor() : ApplicationLifecycleMonitor.ApplicationLifecycleListener {

    @Inject lateinit var androidInjector: DispatchingAndroidInjector<Any>

//    @Inject lateinit var dispatcher: Dispatcher

    @Inject @AppCoroutineScope
    lateinit var appCoroutineScope: CoroutineScope

    private lateinit var application: Application

    fun init(application: Application) {
        this.application = application

        val lifecycleMonitor = ApplicationLifecycleMonitor(this)
        application.registerActivityLifecycleCallbacks(lifecycleMonitor)
        application.registerComponentCallbacks(lifecycleMonitor)

    }

    @Suppress("DEPRECATION")
    override fun onAppComesFromBackground() {}

    override fun onFirstActivityResumed() {
        // App is completely restarted

    }

    override fun onAppGoesToBackground() {}

    private fun restartMainActivity() {
        if (ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(STARTED)) {
            val intent = Intent(application, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            application.startActivity(intent)
        }
    }

}
