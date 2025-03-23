package com.williamfq.xhat.di

import android.content.Context
import com.williamfq.data.location.LocationTrackerImpl
import com.williamfq.domain.location.LocationTracker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PanicModule {
    @Provides
    @Singleton
    fun provideLocationTracker(
        @ApplicationContext context: Context
    ): LocationTracker = LocationTrackerImpl(context)
}