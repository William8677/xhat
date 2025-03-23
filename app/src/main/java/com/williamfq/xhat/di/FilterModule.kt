package com.williamfq.xhat.di

import android.content.Context
import com.williamfq.xhat.domain.repository.FilterRepository
import com.williamfq.xhat.service.filter.FilterProcessor
import com.williamfq.xhat.service.filter.FilterProcessorImpl
import com.williamfq.xhat.utils.ar.ARSystem
import com.williamfq.xhat.utils.filters.shaders.ShaderProcessor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FilterModule {

    @Provides
    @Singleton
    fun provideFilterRepository(@ApplicationContext context: Context): FilterRepository =
        FilterRepository(context)

    @Provides
    @Singleton
    fun provideFilterProcessor(): FilterProcessor = FilterProcessorImpl()

    @Provides
    @Singleton
    fun provideShaderProcessor(): ShaderProcessor = ShaderProcessor()

    @Provides
    @Singleton
    fun provideARSystem(@ApplicationContext context: Context): ARSystem = ARSystem(context)
}
