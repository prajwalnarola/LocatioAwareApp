package com.example.locationawareapp

import com.example.locationawareapp.model.ResponseSuccess
import io.reactivex.rxjava3.core.Observable
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class UserRepository {

    private val apiService: ApiInterface

    init {
        // Create an Interceptor to add headers to each request
        val headerInterceptor = object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val originalRequest: Request = chain.request()
                val newRequest: Request = originalRequest.newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6NCwiY29udGFjdF9udW1iZXIiOiI5MTc1MTY1ODQwIiwiaWF0IjoxNzE5MjA4MDkxfQ.gKms8FOKezNiYuex5dQy6xVrKxf0fo5Ju4WaogBQV6Y") // Replace with your token or method to get token
                    .addHeader("device_token", "10001")
                    .addHeader("device_type", "android")
                    .addHeader("is_testdata", "1")
                    .build()
                return chain.proceed(newRequest)
            }
        }

        // Create OkHttpClient and add the interceptor
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .build()

        // Create Retrofit instance
        val retrofit = Retrofit.Builder()
            .baseUrl("http://clientapp.narola.online:9560")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()

        apiService = retrofit.create(ApiInterface::class.java)
    }

    fun addLocationData(
        location: RequestBody,
        latitude: RequestBody,
        longitude: RequestBody,
        locationType: RequestBody
    ): Observable<ResponseSuccess> {
        return apiService.addLocationData(location, latitude, longitude, locationType)
    }
}
