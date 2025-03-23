package com.williamfq.data.remote

import com.williamfq.domain.models.LocationUpdate
import retrofit2.http.*

interface LocationApi {
    @POST("locations/update")
    suspend fun updateLocation(@Body locationUpdate: LocationUpdate)

    @DELETE("locations/{userId}")
    suspend fun stopLocationSharing(@Path("userId") userId: String)

    @POST("locations/notify/{contactId}")
    suspend fun notifyLocationUpdate(
        @Path("contactId") contactId: String,
        @Body locationUpdate: LocationUpdate
    )
}