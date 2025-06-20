package com.skyblue.skybluecontacts.encrypt.retrofit

import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EncryptRetrofitInstance {
    private const val BASE_URL = "https://contacts.skyblue.co.in/"
    val gson = GsonBuilder()
        .setLenient()
        .create()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // For MVVM
    val contactsService: EncryptAPIInterface by lazy {
        retrofit.create(EncryptAPIInterface::class.java)
    }

    // For normal
    @get:Provides
    @Singleton
    val apiInterface: EncryptAPIInterface = retrofit.create(EncryptAPIInterface::class.java)
}