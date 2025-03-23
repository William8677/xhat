/*
 * Updated: 2025-02-14 16:00:13
 * Author: William8677
 */

package com.williamfq.xhat.di

import android.app.Application
import android.content.Context
import com.williamfq.xhat.XhatApplication
import com.williamfq.data.di.DatabaseModule
import dagger.BindsInstance
import dagger.Component
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        DatabaseModule::class,
        RepositoryModule::class,
        AppModule::class,
        LoggingModule::class,
        ContextModule::class
    ]
)
interface AppComponent {

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance application: Application,
            @BindsInstance @ApplicationContext context: Context
        ): AppComponent
    }

    fun inject(application: XhatApplication)
}