package com.williamfq.data.di

import android.content.Context
import androidx.room.Room
import com.williamfq.data.dao.*
import com.williamfq.data.local.db.XhatDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.williamfq.data.dao.MessageDao
import com.williamfq.data.dao.StoryDao
import com.williamfq.data.dao.CommunityDao
import com.williamfq.data.dao.UserDao
import com.williamfq.data.dao.ChannelDao
import com.williamfq.data.dao.ReactionDao
import com.williamfq.data.dao.CallHistoryDao
import com.williamfq.data.dao.SettingsDao
import com.williamfq.data.dao.MediaDao
import com.williamfq.data.dao.NotificationDao
import com.williamfq.data.dao.ChatDao
import com.williamfq.data.dao.PanicDao
import com.williamfq.data.dao.WalkieTalkieAudioDao
import com.williamfq.data.dao.ContactDao
import com.williamfq.data.dao.LocationDao


@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): XhatDatabase {
        return Room.databaseBuilder(context, XhatDatabase::class.java, XhatDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration(false)
            .build()
    }

    @Provides
    @Singleton
    fun provideMessageDao(database: XhatDatabase): MessageDao = database.messageDao()

    @Provides
    @Singleton
    fun provideStoryDao(database: XhatDatabase): StoryDao = database.storyDao()

    @Provides
    @Singleton
    fun provideCommunityDao(database: XhatDatabase): CommunityDao = database.communityDao()

    @Provides
    @Singleton
    fun provideUserDao(database: XhatDatabase): UserDao = database.userDao()

    @Provides
    @Singleton
    fun provideChannelDao(database: XhatDatabase): ChannelDao = database.channelDao()

    @Provides
    @Singleton
    fun provideReactionDao(database: XhatDatabase): ReactionDao = database.reactionDao()

    @Provides
    @Singleton
    fun provideCallHistoryDao(database: XhatDatabase): CallHistoryDao = database.callHistoryDao()

    @Provides
    @Singleton
    fun provideSettingsDao(database: XhatDatabase): SettingsDao = database.settingsDao()

    @Provides
    @Singleton
    fun provideMediaDao(database: XhatDatabase): MediaDao = database.mediaDao()

    @Provides
    @Singleton
    fun provideNotificationDao(database: XhatDatabase): NotificationDao = database.notificationDao()

    @Provides
    @Singleton
    fun provideChatDao(database: XhatDatabase): ChatDao = database.chatDao()

    @Provides
    @Singleton
    fun providePanicDao(database: XhatDatabase): PanicDao = database.panicDao()

    @Provides
    @Singleton
    fun provideWalkieTalkieAudioDao(database: XhatDatabase): WalkieTalkieAudioDao = database.walkieTalkieAudioDao()

    @Provides
    @Singleton
    fun provideContactDao(database: XhatDatabase): ContactDao = database.contactDao()

    @Provides
    @Singleton
    fun provideLocationDao(database: XhatDatabase): LocationDao = database.locationDao()
}