package com.williamfq.xhat.di

import android.content.Context
import com.google.common.eventbus.EventBus
import com.williamfq.xhat.notifications.AppNotificationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EventModule {

    @Provides
    @Singleton
    fun provideEventBus(): EventBus = EventBus()

    @Provides
    @Singleton
    fun provideNotificationManager(@ApplicationContext context: Context): AppNotificationManager =
        AppNotificationManager(context)
}
