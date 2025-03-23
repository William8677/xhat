/*
 * Updated: 2025-02-13 01:56:33
 * Author: William8677
 */

package com.williamfq.xhat.di

import android.content.Context
import com.williamfq.xhat.utils.analytics.Analytics
import com.williamfq.xhat.utils.logging.LoggerInterface
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AnalyticsModule {

    @Provides
    @Singleton
    fun provideAnalytics(
        @ApplicationContext context: Context,
        logger: LoggerInterface
    ): Analytics {
        return Analytics(
            context = context,
            logger = logger
        )
    }
}