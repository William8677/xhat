package com.williamfq.data.dao

import androidx.room.*
import com.williamfq.data.entities.Session

@Dao
interface SessionDao {

    /**
     * Inserta una nueva sesión en la base de datos.
     * Si existe un conflicto (como el mismo `userId`), reemplaza la sesión existente.
     *
     * @param session La entidad de sesión a insertar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: Session): Long

    /**
     * Inserta múltiples sesiones en la base de datos.
     * Si existe un conflicto, reemplaza las sesiones existentes.
     *
     * @param sessions Lista de sesiones a insertar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(sessions: List<Session>)

    /**
     * Obtiene una sesión específica por el `userId`.
     *
     * @param userId El ID del usuario para buscar la sesión.
     * @return La entidad de sesión correspondiente, o null si no existe.
     */
    @Query("SELECT * FROM sessions WHERE user_id = :userId LIMIT 1")
    suspend fun getSessionByUserId(userId: String): Session?

    /**
     * Recupera todas las sesiones activas almacenadas en la base de datos.
     *
     * @return Una lista de todas las sesiones activas.
     */
    @Query("SELECT * FROM sessions WHERE is_active = 1")
    suspend fun getActiveSessions(): List<Session>

    /**
     * Actualiza una sesión específica.
     *
     * @param session La entidad de sesión con los nuevos valores.
     */
    @Update
    suspend fun updateSession(session: Session)

    /**
     * Marca una sesión como inactiva por el `userId`.
     *
     * @param userId El ID del usuario cuya sesión será marcada como inactiva.
     */
    @Query("UPDATE sessions SET is_active = 0 WHERE user_id = :userId")
    suspend fun deactivateSessionByUserId(userId: String)

    /**
     * Elimina una sesión específica por el `userId`.
     *
     * @param userId El ID del usuario cuya sesión será eliminada.
     */
    @Query("DELETE FROM sessions WHERE user_id = :userId")
    suspend fun deleteSessionByUserId(userId: String)

    /**
     * Elimina todas las sesiones almacenadas en la base de datos.
     */
    @Query("DELETE FROM sessions")
    suspend fun clearAllSessions()

    /**
     * Elimina todas las sesiones que hayan expirado.
     *
     * @param currentTime El tiempo actual en milisegundos.
     */
    @Query("DELETE FROM sessions WHERE expires_at < :currentTime")
    suspend fun deleteExpiredSessions(currentTime: Long)

    /**
     * Obtiene el número total de sesiones activas.
     *
     * @return El conteo total de sesiones activas.
     */
    @Query("SELECT COUNT(*) FROM sessions WHERE is_active = 1")
    suspend fun getActiveSessionCount(): Int
}
