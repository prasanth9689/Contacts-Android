package com.skyblue.skybluecontacts.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
private const val BASE_URL = "https://contacts.skyblue.co.in/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val contactsService: ContactsService by lazy {
        retrofit.create(ContactsService::class.java)
    }
}