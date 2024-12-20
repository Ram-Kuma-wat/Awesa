package com.game.awesa.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.game.awesa.ui.Awesa
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.AndroidInjectionModule
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.annotation.AnnotationRetention.RUNTIME

@InstallIn(SingletonComponent::class)
@Module(
    includes = [
        AndroidInjectionModule::class,
    ]
)
abstract class ApplicationModule {
    // Expose Application as an injectable context
    @Binds
    internal abstract fun bindContext(@ApplicationContext context: Context): Context

    @Binds
    abstract fun bindCoroutineScope(@AppCoroutineScope scope: CoroutineScope): CoroutineScope

    companion object {
        @Provides
        @AppCoroutineScope
        @Singleton
        fun provideAppCoroutineScope(dispatcher: CoroutineDispatcher): CoroutineScope =
            CoroutineScope(SupervisorJob() + dispatcher)

        @Provides
        fun provideBackgroundDispatcher(): CoroutineDispatcher {
            return Dispatchers.Default
        }

        @OptIn(UnstableApi::class)
        @Provides
        @Singleton
        fun provideAwesa(@ApplicationContext application: Context): Awesa {
            return application as Awesa
        }

//        @Provides
//        fun providesFirebaseRemoteConfig() = Firebase.remoteConfig
    }
}

@Qualifier
@MustBeDocumented
@Retention(RUNTIME)
annotation class AppCoroutineScope
