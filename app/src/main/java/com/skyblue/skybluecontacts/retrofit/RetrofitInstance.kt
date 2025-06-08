package com.skyblue.skybluecontacts.retrofit

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
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
    val contactsService: APIInterface by lazy {
        retrofit.create(APIInterface::class.java)
    }

    // For normal
    val apiInterface: APIInterface = retrofit.create(APIInterface::class.java)
}