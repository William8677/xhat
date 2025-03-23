package com.williamfq.xhat.di

import com.williamfq.data.repository.CallRepositoryImpl
import com.williamfq.data.repository.ChatRepositoryImpl
import com.williamfq.data.repository.CommunityRepositoryImpl
import com.williamfq.data.repository.ContactRepositoryImpl
import com.williamfq.data.repository.PanicRepositoryImpl
import com.williamfq.data.repository.UserActivityRepositoryImpl
import com.williamfq.data.repository.UserRepositoryImpl
import com.williamfq.domain.repository.CallRepository
import com.williamfq.domain.repository.ChatRepository
import com.williamfq.domain.repository.CommunityRepository
import com.williamfq.domain.repository.ContactRepository
import com.williamfq.domain.repository.PanicRepository
import com.williamfq.domain.repository.UserActivityRepository
import com.williamfq.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCommunityRepository(
        impl: CommunityRepositoryImpl
    ): CommunityRepository

    @Binds
    @Singleton
    abstract fun bindPanicRepository(
        impl: PanicRepositoryImpl
    ): PanicRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindUserActivityRepository(
        impl: UserActivityRepositoryImpl
    ): UserActivityRepository

    @Binds
    @Singleton
    abstract fun bindCallRepository(
        impl: CallRepositoryImpl
    ): CallRepository

    @Binds
    @Singleton
    abstract fun bindContactRepository(
        impl: ContactRepositoryImpl
    ): ContactRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        impl: ChatRepositoryImpl
    ): ChatRepository
}
