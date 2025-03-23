package com.xhat.api

import com.williamfq.domain.model.UserActivityData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface XhatApiService {

    @GET("user/activity")
    fun getUserActivity(
        @Query("userId") userId: String,
        @Query("period") period: String // "weekly", "monthly", "yearly"
    ): Call<UserActivityData>
}
