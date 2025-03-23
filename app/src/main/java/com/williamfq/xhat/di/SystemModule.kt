package com.williamfq.xhat.di

import android.content.Context
import android.os.UserManager
import android.location.LocationManager as SysLocationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SystemModule {

    @Provides
    @Singleton
    fun provideUserManager(@ApplicationContext context: Context): UserManager {
        return context.getSystemService(Context.USER_SERVICE) as UserManager
    }

    @Provides
    @Singleton
    fun provideSystemLocationManager(@ApplicationContext context: Context): SysLocationManager {
        return context.getSystemService(Context.LOCATION_SERVICE) as SysLocationManager
    }
}
