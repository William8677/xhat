package com.williamfq.xhat.di

import android.content.Context
import com.williamfq.xhat.BuildConfig
import com.williamfq.xhat.utils.logging.FileLogger
import com.williamfq.xhat.utils.logging.LoggerInterface
import com.williamfq.xhat.utils.logging.TimberLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LoggingModule {

    @Provides
    @Singleton
    @Named("isDebug")
    fun provideIsDebug(): Boolean = BuildConfig.DEBUG

    @Provides
    @Singleton
    fun provideFileLogger(@ApplicationContext context: Context): FileLogger = FileLogger(context)

    @Provides
    @Singleton
    fun provideTimberLogger(): TimberLogger = TimberLogger()

    @Provides
    @Singleton
    fun provideLoggerInterface(
        @Named("isDebug") isDebug: Boolean,
        fileLogger: FileLogger,
        timberLogger: TimberLogger
    ): LoggerInterface = if (isDebug) timberLogger else fileLogger
}
