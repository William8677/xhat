package com.williamfq.data.dao

import androidx.room.*
import com.williamfq.data.entities.Reaction

@Dao
interface ReactionDao {
    // Data class para mapear los conteos de reacciones
    data class ReactionCount(
        @ColumnInfo(name = "emoji")
        val emoji: String,

        @ColumnInfo(name = "count")
        val count: Int
    )

    /**
     * Inserta una nueva reacción en la base de datos.
     * Retorna el ID generado automáticamente.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addReaction(reaction: Reaction): Long

    /**
     * Inserta una lista de reacciones en la base de datos.
     * Retorna la lista de IDs generados.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addReactions(reactions: List<Reaction>): List<Long>

    /**
     * Obtiene todas las reacciones asociadas a un mensaje específico.
     *
     * @param messageId El ID del mensaje.
     * @return Lista de reacciones asociadas al mensaje.
     */
    @Query("SELECT * FROM reactions WHERE message_id = :messageId ORDER BY timestamp DESC")
    suspend fun getReactionsByMessage(messageId: String): List<Reaction>

    /**
     * Obtiene todas las reacciones realizadas por un usuario específico.
     *
     * @param userId El ID del usuario.
     * @return Lista de reacciones realizadas por el usuario.
     */
    @Query("SELECT * FROM reactions WHERE user_id = :userId ORDER BY timestamp DESC")
    suspend fun getReactionsByUser(userId: String): List<Reaction>

    /**
     * Verifica si un usuario ya ha reaccionado a un mensaje con un emoji específico.
     *
     * @param messageId El ID del mensaje.
     * @param userId El ID del usuario.
     * @param emoji El emoji usado en la reacción.
     * @return true si el usuario ya reaccionó, false en caso contrario.
     */
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM reactions 
            WHERE message_id = :messageId 
            AND user_id = :userId 
            AND emoji = :emoji
        )
    """)
    suspend fun hasUserReacted(messageId: String, userId: String, emoji: String): Boolean

    /**
     * Obtiene el número de reacciones por emoji para un mensaje.
     *
     * @param messageId El ID del mensaje.
     * @return Lista de ReactionCount con el emoji y su conteo.
     */
    @Query("""
        SELECT emoji, COUNT(*) as count 
        FROM reactions 
        WHERE message_id = :messageId 
        GROUP BY emoji
    """)
    suspend fun getReactionCountsByMessage(messageId: String): List<ReactionCount>

    /**
     * Elimina una reacción específica de un usuario en un mensaje.
     *
     * @param messageId El ID del mensaje.
     * @param userId El ID del usuario.
     * @param emoji El emoji de la reacción a eliminar.
     */
    @Query("""
        DELETE FROM reactions 
        WHERE message_id = :messageId 
        AND user_id = :userId 
        AND emoji = :emoji
    """)
    suspend fun deleteUserReaction(messageId: String, userId: String, emoji: String)

    /**
     * Elimina todas las reacciones asociadas a un mensaje específico.
     *
     * @param messageId El ID del mensaje.
     */
    @Query("DELETE FROM reactions WHERE message_id = :messageId")
    suspend fun deleteReactionsByMessage(messageId: String)

    /**
     * Elimina todas las reacciones de la base de datos.
     */
    @Query("DELETE FROM reactions")
    suspend fun clearReactions()

    /**
     * Obtiene las reacciones más recientes de un usuario.
     *
     * @param userId El ID del usuario.
     * @param limit El número máximo de reacciones a retornar.
     */
    @Query("""
        SELECT * FROM reactions 
        WHERE user_id = :userId 
        ORDER BY timestamp DESC 
        LIMIT :limit
    """)
    suspend fun getRecentUserReactions(userId: String, limit: Int = 10): List<Reaction>

    /**
     * Obtiene el total de reacciones para un mensaje.
     *
     * @param messageId El ID del mensaje.
     */
    @Query("SELECT COUNT(*) FROM reactions WHERE message_id = :messageId")
    suspend fun getTotalReactionCount(messageId: String): Int

    /**
     * Obtiene las reacciones realizadas en un rango de tiempo específico.
     *
     * @param startTime Tiempo de inicio en milisegundos.
     * @param endTime Tiempo final en milisegundos.
     */
    @Query("""
        SELECT * FROM reactions 
        WHERE timestamp BETWEEN :startTime AND :endTime 
        ORDER BY timestamp DESC
    """)
    suspend fun getReactionsByTimeRange(startTime: Long, endTime: Long): List<Reaction>
}