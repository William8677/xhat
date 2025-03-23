package com.williamfq.data.remote

import com.williamfq.domain.model.Community
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface XhatApiService {

    // Endpoint para obtener todas las comunidades
    @GET("communities")
    suspend fun fetchCommunities(): List<Community>

    // Endpoint para crear una nueva comunidad
    @POST("communities")
    suspend fun createCommunity(@Body community: Community): Unit

    // Endpoint para suscribirse a una comunidad
    @POST("communities/{id}/subscribe")
    suspend fun subscribeToCommunity(@Path("id") communityId: String): Unit
}
