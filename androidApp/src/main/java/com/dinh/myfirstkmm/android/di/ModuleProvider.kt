package com.dinh.myfirstkmm.android.di

import android.content.Context
import com.dinh.myfirstkmm.android.ui.MainViewModel
import com.dinh.myfirstkmm.domain.RoboMoveCommandPayload

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ModuleProvider {
    @Provides
    @Singleton //for share instance
    fun provideShareViewModel(@ApplicationContext context: Context, moshi: Moshi): MainViewModel {
        return MainViewModel(context, moshi = moshi)
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder().add(KotlinJsonAdapterFactory()).build().apply {
            this.adapter(RoboMoveCommandPayload::class.java)
        }
    }
}