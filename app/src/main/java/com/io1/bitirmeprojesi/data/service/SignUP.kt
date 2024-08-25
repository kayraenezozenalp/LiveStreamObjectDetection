package com.io1.bitirmeprojesi.data.service

import com.io1.bitirmeprojesi.data.model.AuthResponseModel
import com.io1.bitirmeprojesi.data.model.HistoryModel
import com.io1.bitirmeprojesi.data.model.HistoryModelItem
import com.io1.bitirmeprojesi.data.model.PostImageResponse
import com.io1.bitirmeprojesi.data.model.UploadImageResponse
import com.io1.bitirmeprojesi.view.auth.UserCredentials
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


interface SignUP {
    @POST("signup")
    suspend fun signup(@Body request: UserCredentials): Response<AuthResponseModel>
}

interface SignIn {
    @POST("login")
    suspend fun signin(@Body request: UserCredentials): Response<AuthResponseModel>
}

interface PostImage{
    @POST("uploadImage")
    suspend fun postimage(@Header("Authorization") authorization : String ,@Body body: RequestBody): Response<UploadImageResponse>
}

interface History{
    @GET("history")
    suspend fun gethistory(@Header("Authorization") authorization : String): Response<HistoryModel>
}

interface GetImage{
    @POST("predicted-images")
    suspend fun postimage(@Header("Authorization") authorization : String ,@Body predictedImage: String): Response<UploadImageResponse>
}

interface GoogleSign {
    @GET("google")
    suspend fun googlesign()
}