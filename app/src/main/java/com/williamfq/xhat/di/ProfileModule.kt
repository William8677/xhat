package com.williamfq.xhat.di

import android.content.Context
import com.williamfq.xhat.data.repository.UserRepository
import com.williamfq.xhat.utils.ImageManager
import com.williamfq.xhat.utils.LocationManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProfileModule {

    // Se eliminan las provisiones de FirebaseAuth, Firestore y Storage
    // porque se obtienen de FirebaseModule

    @Provides
    @Singleton
    fun provideImageManager(
        @ApplicationContext context: Context,
        storage: FirebaseStorage
    ): ImageManager = ImageManager(context, storage)

    @Provides
    @Singleton
    fun provideLocationManager(
        @ApplicationContext context: Context
    ): LocationManager = LocationManager(context)

    @Provides
    @Singleton
    fun provideUserRepository(
        auth: FirebaseAuth,
        db: FirebaseFirestore
    ): UserRepository = UserRepository(auth, db)
}
