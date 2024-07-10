package com.example.locationawareapp

import com.example.locationawareapp.model.ResponseSuccess
import io.reactivex.rxjava3.core.Observable
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiInterface {

    @Multipart
    @POST("/setting/add_user_location")
    fun addLocationData(
        @Part("location") location: RequestBody,
        @Part("latitude") latitude: RequestBody,
        @Part("longitude") longitude: RequestBody,
        @Part("location_type") location_type: RequestBody
    ): Observable<ResponseSuccess>

}