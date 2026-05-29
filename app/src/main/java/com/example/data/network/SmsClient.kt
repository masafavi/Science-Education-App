package com.example.data.network

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object SmsClient {
    private const val BASE_URL = "https://api.kavenegar.com/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val service: KavenegarService = retrofit.create(KavenegarService::class.java)
}
