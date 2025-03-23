package com.williamfq.data.repository

import com.williamfq.data.dao.CommunityDao
import com.williamfq.data.mapper.toDomain
import com.williamfq.data.mapper.toEntity
import com.williamfq.domain.model.Community
import com.williamfq.domain.repository.CommunityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommunityRepositoryImpl @Inject constructor(
    private val communityDao: CommunityDao
) : CommunityRepository {

    override fun getCommunities(): Flow<List<Community>> =
        communityDao.getAllCommunities().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getUserCommunities(): Flow<List<Community>> =
        communityDao.getUserCommunities(CURRENT_USER).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getTrendingCommunities(): Flow<List<Community>> =
        communityDao.getTrendingCommunities().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun searchCommunities(query: String): Flow<List<Community>> =
        communityDao.searchCommunities(query).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun createCommunity(community: Community): Result<Unit> {
        return try {
            val communityToCreate = community.copy(
                createdAt = Instant.now().toEpochMilli(),
                createdBy = CURRENT_USER,
                isActive = true,
                lastActivityAt = Instant.now().toEpochMilli(),
                moderators = listOf(CURRENT_USER) + (community.moderators ?: emptyList())
            )

            validate(communityToCreate)
            val id = communityDao.insertCommunity(communityToCreate.toEntity())
            if (id > 0) Result.success(Unit)
            else Result.failure(Exception("Error al crear la comunidad"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun joinCommunity(communityId: String): Result<Unit> {
        return try {
            val id = communityId.toLongOrNull()
                ?: return Result.failure(Exception("ID de comunidad inválido"))

            val community = communityDao.getCommunityById(id)
                ?: return Result.failure(Exception("Comunidad no encontrada"))

            if (!community.isSubscribed) {
                communityDao.updateSubscriptionStatus(id, true)
                communityDao.updateMemberCount(id, 1) // Incrementa el número de miembros
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun leaveCommunity(communityId: String): Result<Unit> {
        return try {
            val id = communityId.toLongOrNull()
                ?: return Result.failure(Exception("ID de comunidad inválido"))

            val community = communityDao.getCommunityById(id)
                ?: return Result.failure(Exception("Comunidad no encontrada"))

            if (community.isSubscribed) {
                communityDao.updateSubscriptionStatus(id, false)
                communityDao.updateMemberCount(id, -1) // Decrementa el número de miembros
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun subscribeCommunity(communityId: String): Result<Unit> {
        return try {
            val id = communityId.toLongOrNull()
                ?: return Result.failure(Exception("ID de comunidad inválido"))

            val community = communityDao.getCommunityById(id)
                ?: return Result.failure(Exception("Comunidad no encontrada"))

            val isCurrentlySubscribed = community.isSubscribed
            communityDao.updateSubscriptionStatus(id, !isCurrentlySubscribed)

            val increment = if (isCurrentlySubscribed) -1 else 1
            communityDao.updateMemberCount(id, increment)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        private const val CURRENT_USER = "William8677"
        private const val MAX_NAME_LENGTH = 50
        private const val MAX_DESCRIPTION_LENGTH = 1000
    }

    private fun validate(community: Community) {
        require(community.name.isNotBlank()) {
            "El nombre de la comunidad no puede estar vacío"
        }
        require(community.name.length <= MAX_NAME_LENGTH) {
            "El nombre no puede exceder $MAX_NAME_LENGTH caracteres"
        }
        community.description?.let { desc ->
            require(desc.length <= MAX_DESCRIPTION_LENGTH) {
                "La descripción no puede exceder $MAX_DESCRIPTION_LENGTH caracteres"
            }
        }
    }
}
