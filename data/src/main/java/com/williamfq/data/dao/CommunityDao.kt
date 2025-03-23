package com.williamfq.data.dao

import androidx.room.*
import com.williamfq.data.entities.CommunityEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para las operaciones de base de datos relacionadas con comunidades.
 * Creado por William8677 el 2025-01-04
 */
@Dao
interface CommunityDao {

    /**
     * Inserta una nueva comunidad en la base de datos.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommunity(communityEntity: CommunityEntity): Long

    /**
     * Inserta múltiples comunidades en la base de datos.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommunities(communities: List<CommunityEntity>)

    /**
     * Actualiza una comunidad existente.
     */
    @Update
    suspend fun updateCommunity(communityEntity: CommunityEntity)

    /**
     * Actualiza el estado de suscripción de una comunidad.
     */
    @Query("UPDATE communities SET isSubscribed = :status WHERE id = :communityId")
    suspend fun updateSubscriptionStatus(communityId: Long, status: Boolean)

    /**
     * Elimina una comunidad por su ID.
     */
    @Query("DELETE FROM communities WHERE id = :communityId")
    suspend fun deleteCommunityById(communityId: Long)

    /**
     * Obtiene una comunidad por su ID.
     */
    @Query("SELECT * FROM communities WHERE id = :communityId")
    suspend fun getCommunityById(communityId: Long): CommunityEntity?

    /**
     * Obtiene todas las comunidades ordenadas por fecha de creación.
     */
    @Query("SELECT * FROM communities ORDER BY createdAt DESC")
    fun getAllCommunities(): Flow<List<CommunityEntity>>

    /**
     * Obtiene las comunidades activas del usuario.
     */
    @Query("""
        SELECT * FROM communities 
        WHERE isActive = 1 
        AND (createdBy = :userId OR isSubscribed = 1)
        ORDER BY lastActivityAt DESC
    """)
    fun getUserCommunities(userId: String): Flow<List<CommunityEntity>>

    /**
     * Busca comunidades por nombre o descripción.
     */
    @Query("""
        SELECT * FROM communities 
        WHERE isActive = 1 
        AND (name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%')
        ORDER BY memberCount DESC
    """)
    fun searchCommunities(query: String): Flow<List<CommunityEntity>>

    /**
     * Obtiene las comunidades por categoría.
     */
    @Query("""
        SELECT * FROM communities 
        WHERE isActive = 1 
        AND categories LIKE '%' || :category || '%'
        ORDER BY memberCount DESC
    """)
    fun getCommunityByCategory(category: String): Flow<List<CommunityEntity>>

    /**
     * Obtiene comunidades populares.
     */
    @Query("""
        SELECT * FROM communities 
        WHERE isActive = 1 
        ORDER BY memberCount DESC, lastActivityAt DESC
    """)
    fun getTrendingCommunities(): Flow<List<CommunityEntity>>

    /**
     * Actualiza el contador de miembros de una comunidad.
     */
    @Query("UPDATE communities SET memberCount = memberCount + :increment WHERE id = :communityId")
    suspend fun updateMemberCount(communityId: Long, increment: Int)
}
