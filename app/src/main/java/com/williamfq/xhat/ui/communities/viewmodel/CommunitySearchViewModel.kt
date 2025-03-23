/*
 * Updated: 2025-01-25 23:39:57
 * Author: William8677
 *
 * Este archivo forma parte de la app xhat.
 */

package com.williamfq.xhat.ui.communities.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.williamfq.domain.repository.CommunityRepository
import com.williamfq.domain.model.Community
import com.williamfq.xhat.domain.model.PostType
import com.williamfq.xhat.ui.communities.filters.CommunityCategory
import com.williamfq.xhat.ui.communities.filters.CommunitySearchFilters
import com.williamfq.xhat.ui.communities.filters.CommunitySortOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class CommunitySearchViewModel @Inject constructor(
    private val communityRepository: CommunityRepository
) : ViewModel() {

    private val _searchFilters = MutableStateFlow(CommunitySearchFilters())
    val searchFilters: StateFlow<CommunitySearchFilters> = _searchFilters.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _communities = MutableStateFlow<List<Community>>(emptyList())
    val communities: StateFlow<List<Community>> = _communities.asStateFlow()

    init {
        viewModelScope.launch {
            _searchFilters
                .debounce(300)
                .distinctUntilChanged()
                .collect { filters ->
                    searchCommunities(filters)
                }
        }
    }

    fun updateQuery(query: String) {
        _searchFilters.value = _searchFilters.value.copy(query = query)
    }

    fun updateSortOption(option: CommunitySortOption) {
        _searchFilters.value = _searchFilters.value.copy(sortBy = option)
    }

    fun updateCategories(categories: List<CommunityCategory>) {
        _searchFilters.value = _searchFilters.value.copy(categoryFilter = categories)
    }

    fun updateMemberCountRange(range: ClosedRange<Int>) {
        _searchFilters.value = _searchFilters.value.copy(memberCountRange = range)
    }

    fun updateAgeRange(range: ClosedRange<Long>) {
        _searchFilters.value = _searchFilters.value.copy(ageRange = range)
    }

    fun updateContentTypes(types: List<PostType>) {
        _searchFilters.value = _searchFilters.value.copy(contentTypeFilter = types)
    }

    fun toggleNSFW(show: Boolean) {
        _searchFilters.value = _searchFilters.value.copy(showNSFW = show)
    }

    fun togglePrivate(show: Boolean) {
        _searchFilters.value = _searchFilters.value.copy(showPrivate = show)
    }

    fun toggleJoinedOnly(onlyJoined: Boolean) {
        _searchFilters.value = _searchFilters.value.copy(onlyJoined = onlyJoined)
    }

    fun toggleModeratedOnly(onlyModerated: Boolean) {
        _searchFilters.value = _searchFilters.value.copy(onlyModerated = onlyModerated)
    }

    fun updateLanguage(language: String?) {
        // Como el modelo Community no posee 'language', esta línea se deja por si se añade en el futuro.
        _searchFilters.value = _searchFilters.value.copy(language = language)
    }

    fun updateActiveTimeFilter(timeInMillis: Long?) {
        _searchFilters.value = _searchFilters.value.copy(activeInLast = timeInMillis)
    }

    private suspend fun searchCommunities(filters: CommunitySearchFilters) {
        try {
            _isLoading.value = true
            _error.value = null

            // Se pasa el query (String) en lugar del objeto completo.
            val results = communityRepository.searchCommunities(filters.query)
                .map { list ->
                    list.filter { community ->
                        applyLocalFilters(community, filters)
                    }.sortedWith(getSortComparator(filters.sortBy))
                }
                .first()  // Se obtiene la primera emisión del Flow

            _communities.value = results
        } catch (e: Exception) {
            _error.value = e.message
        } finally {
            _isLoading.value = false
        }
    }

    private fun applyLocalFilters(community: Community, filters: CommunitySearchFilters): Boolean {
        return (!filters.onlyJoined || community.isJoined) &&
                (!filters.onlyModerated || community.isModerator("William8677")) &&  // Se usa un ID de ejemplo (CURRENT_USER)
                (filters.showNSFW || !community.isNSFW) &&
                (filters.showPrivate || !community.isPrivate) &&
                (filters.memberCountRange?.contains(community.memberCount) ?: true) &&
                (filters.ageRange?.contains(community.createdAt) ?: true) &&
                // Se omite la condición de language, ya que Community no posee esa propiedad.
                (filters.activeInLast == null || (System.currentTimeMillis() - community.lastActivityAt) <= filters.activeInLast)
    }

    private fun getSortComparator(sortOption: CommunitySortOption): Comparator<Community> {
        return when (sortOption) {
            CommunitySortOption.TRENDING -> compareByDescending { it.memberCount + it.activeUsers }
            CommunitySortOption.NEWEST -> compareByDescending { it.createdAt }
            CommunitySortOption.MOST_MEMBERS -> compareByDescending { it.memberCount }
            CommunitySortOption.MOST_ACTIVE -> compareByDescending { it.activeUsers }
            CommunitySortOption.ALPHABETICAL -> compareBy { it.name }
        }
    }
}
