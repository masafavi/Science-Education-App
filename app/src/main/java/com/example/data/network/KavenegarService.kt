package com.example.data.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface KavenegarService {
    @GET("v1/{apiKey}/verify/lookup.json")
    suspend fun sendOtp(
        @Path("apiKey") apiKey: String,
        @Query("receptor") phoneNumber: String,
        @Query("token") otpCode: String,
        @Query("template") template: String
    ): KavenegarResponse
}

data class KavenegarResponse(
    val returnData: KavenegarReturnData?
)

data class KavenegarReturnData(
    val status: Int,
    val message: String
)
