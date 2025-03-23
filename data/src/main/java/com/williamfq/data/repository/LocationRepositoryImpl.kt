package com.williamfq.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.williamfq.data.dao.LocationDao
import com.williamfq.data.entities.LocationEntity
import com.williamfq.data.entities.LocationHistoryEntity
import com.williamfq.data.remote.LocationApi
import com.williamfq.domain.models.Location
import com.williamfq.domain.models.LocationUpdate
import com.williamfq.domain.repository.LocationRepository
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

/**
 * Implementación del repositorio de ubicaciones.
 * @author William8677
 * @since 2025-02-10 19:15:15
 */
class LocationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val locationDao: LocationDao,
    private val locationApi: LocationApi
) : LocationRepository {

    override suspend fun updateLocation(locationUpdate: LocationUpdate) {
        try {
            // Actualizar en Firestore
            firestore.collection("locations")
                .document(locationUpdate.userId)
                .set(locationUpdate.toMap())
                .await()

            // Guardar en base de datos local
            locationDao.insertLocationUpdate(LocationEntity.fromLocationUpdate(locationUpdate))

            // Actualizar en el servidor
            locationApi.updateLocation(locationUpdate)
        } catch (e: Exception) {
            Timber.e(e, "Error actualizando ubicación")
            throw e
        }
    }

    override suspend fun saveLocationHistory(locationUpdate: LocationUpdate) {
        try {
            firestore.collection("location_history")
                .document("${locationUpdate.userId}_${locationUpdate.timestamp}")
                .set(locationUpdate.toMap())
                .await()

            locationDao.insertLocationHistory(LocationHistoryEntity(
                userId = locationUpdate.userId,
                latitude = locationUpdate.location.latitude,
                longitude = locationUpdate.location.longitude,
                accuracy = locationUpdate.accuracy,
                altitude = locationUpdate.altitude,
                speed = locationUpdate.speed,
                bearing = locationUpdate.bearing,
                timestamp = locationUpdate.timestamp
            ))
        } catch (e: Exception) {
            Timber.e(e, "Error guardando historial de ubicación")
            throw e
        }
    }

    override suspend fun stopLocationSharing(userId: String) {
        try {
            firestore.collection("locations")
                .document(userId)
                .delete()
                .await()

            locationApi.stopLocationSharing(userId)
            locationDao.deleteActiveLocation(userId)
        } catch (e: Exception) {
            Timber.e(e, "Error deteniendo compartición de ubicación")
            throw e
        }
    }

    override suspend fun notifyLocationUpdate(contactId: String, locationUpdate: LocationUpdate) {
        try {
            firestore.collection("location_notifications")
                .document(contactId)
                .collection("updates")
                .add(locationUpdate.toMap())
                .await()

            locationApi.notifyLocationUpdate(contactId, locationUpdate)
        } catch (e: Exception) {
            Timber.e(e, "Error notificando actualización de ubicación")
            throw e
        }
    }

    override suspend fun getLastKnownLocation(): Location? {
        return try {
            locationDao.getLastKnownLocation("")?.toDomainModel()
        } catch (e: Exception) {
            Timber.e(e, "Error obteniendo última ubicación conocida")
            null
        }
    }

    override suspend fun getLocationHistory(userId: String): List<LocationUpdate> {
        return try {
            locationDao.getLocationHistory(userId).map { historyEntity ->
                LocationUpdate(
                    userId = historyEntity.userId,
                    chatId = "",  // Valor por defecto para el historial
                    chatType = "",  // Valor por defecto para el historial
                    location = Location(
                        latitude = historyEntity.latitude,
                        longitude = historyEntity.longitude,
                        accuracy = historyEntity.accuracy,
                        altitude = historyEntity.altitude,
                        speed = historyEntity.speed,
                        bearing = historyEntity.bearing,
                        time = historyEntity.timestamp
                    ),
                    timestamp = historyEntity.timestamp,
                    accuracy = historyEntity.accuracy,
                    speed = historyEntity.speed,
                    altitude = historyEntity.altitude,
                    bearing = historyEntity.bearing
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Error obteniendo historial de ubicación")
            emptyList()
        }
    }

    override suspend fun clearLocationHistory(userId: String) {
        try {
            locationDao.clearLocationHistory(userId)
            firestore.collection("location_history")
                .whereEqualTo("userId", userId)
                .get()
                .await()
                .documents
                .forEach { it.reference.delete() }
        } catch (e: Exception) {
            Timber.e(e, "Error limpiando historial de ubicación")
            throw e
        }
    }

    override suspend fun getSharingContacts(): List<String> {
        return try {
            locationDao.getSharingContacts()
        } catch (e: Exception) {
            Timber.e(e, "Error obteniendo contactos compartiendo")
            emptyList()
        }
    }

    private fun LocationUpdate.toMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "chatId" to chatId,
        "chatType" to chatType,
        "location" to location.toMap(),
        "timestamp" to timestamp,
        "accuracy" to accuracy,
        "speed" to speed,
        "altitude" to altitude,
        "bearing" to bearing
    )

    private fun Location.toMap(): Map<String, Any> = mapOf(
        "latitude" to latitude,
        "longitude" to longitude,
        "accuracy" to accuracy,
        "altitude" to altitude,
        "speed" to speed,
        "bearing" to bearing,
        "time" to time
    )
}