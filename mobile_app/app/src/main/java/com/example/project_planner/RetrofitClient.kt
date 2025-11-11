package com.example.project_planner

import io.socket.client.IO
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://192.168.0.26:3000" // Используйте IP для эмулятора

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

val socket: io.socket.client.Socket? = IO.socket("http://192.168.0.26:3000").apply {
    connect()
}