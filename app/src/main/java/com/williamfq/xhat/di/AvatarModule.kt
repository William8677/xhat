// app/src/main/java/com/williamfq/xhat/di/AvatarModule.kt

package com.williamfq.xhat.di

import android.content.Context
import com.williamfq.xhat.utils.avatar.SignalAvatarEncryption
import com.williamfq.xhat.utils.avatar.SignalAvatarManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AvatarModule {

    @Provides
    @Singleton
    fun provideSignalAvatarEncryption(): SignalAvatarEncryption {
        return SignalAvatarEncryption()
    }

    @Provides
    @Singleton
    fun provideSignalAvatarManager(
        @ApplicationContext context: Context,
        encryption: SignalAvatarEncryption
    ): SignalAvatarManager {
        return SignalAvatarManager(context, encryption)
    }
}