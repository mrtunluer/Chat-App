package com.yks.chatapp.di

import dagger.Module
import com.google.firebase.ktx.Firebase
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Singleton
    @Provides
    fun provideFirebase() = Firebase
}