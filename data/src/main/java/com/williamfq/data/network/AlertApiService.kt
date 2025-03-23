// data/src/main/java/com/williamfq/data/network/AlertApiService.kt
package com.williamfq.data.network

import com.williamfq.data.network.models.AlertRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AlertApiService {
    @POST("alerts/send")
    suspend fun sendPanicAlert(@Body request: AlertRequest)
}
