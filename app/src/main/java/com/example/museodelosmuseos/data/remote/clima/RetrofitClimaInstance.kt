package com.example.museodelosmuseos.data.remote.clima

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClimaInstance {

    private const val BASE_URL = "https://api.open-meteo.com/"

    val api: ClimaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ClimaApiService::class.java)
    }
}
