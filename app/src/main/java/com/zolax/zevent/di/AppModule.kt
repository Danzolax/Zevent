package com.zolax.zevent.di

import android.content.Context
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.zolax.zevent.repositories.FirebaseRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton


@Module
@InstallIn(ApplicationComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideFirebaseRepository() = FirebaseRepository()

    @Singleton
    @Provides
    fun provideGlide(@ApplicationContext context: Context) = Glide.with(context)

    @Singleton
    @Provides
    fun provideFusedLocationProviderClient(
        @ApplicationContext app: Context
    ) = FusedLocationProviderClient(app)
}