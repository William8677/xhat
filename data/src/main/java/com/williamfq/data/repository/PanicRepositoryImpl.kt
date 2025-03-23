package com.williamfq.data.repository

import com.williamfq.data.dao.PanicDao
import com.williamfq.data.entities.PanicAlertStatusEntity
import com.williamfq.data.mappers.toPanicAlertEntity
import com.williamfq.data.mappers.toPanicAlert
import com.williamfq.data.mappers.toPanicAlertStatusEntity
import com.williamfq.domain.models.PanicAlert
import com.williamfq.domain.models.PanicAlertStatus
import com.williamfq.domain.repository.PanicRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PanicRepositoryImpl @Inject constructor(
    private val panicDao: PanicDao
) : PanicRepository {

    override suspend fun insertPanicAlert(alert: PanicAlert): Long {
        return panicDao.insertPanicAlert(alert.toPanicAlertEntity())
    }

    override suspend fun getPanicAlertsByUserId(userId: String): List<PanicAlert> {
        return panicDao.getPanicAlertsByUserId(userId).map { it.toPanicAlert() }
    }

    override suspend fun updatePanicAlertStatus(alertId: Long, status: PanicAlertStatus) {
        panicDao.updatePanicAlertStatus(alertId, status.toPanicAlertStatusEntity())
    }

    override suspend fun getPanicAlert(alertId: Long): PanicAlert? {
        // Como no existe el método en el DAO, buscamos en todas las alertas
        return panicDao.getPanicAlertsByUserId("") // Obtener todas las alertas
            .find { it.id == alertId }
            ?.toPanicAlert()
    }

    override suspend fun deletePanicAlert(alertId: Long) {
        // Como no existe deletePanicAlert en el DAO, actualizamos el estado a DELETED
        panicDao.updatePanicAlertStatus(
            alertId,
            PanicAlertStatusEntity.DELETED // Asegúrate de añadir este estado en el enum
        )
    }
}