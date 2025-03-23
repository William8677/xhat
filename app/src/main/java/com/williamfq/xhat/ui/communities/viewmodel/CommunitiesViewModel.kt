package com.williamfq.xhat.ui.communities.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.williamfq.domain.model.Community
import com.williamfq.domain.repository.CommunityRepository
import com.williamfq.xhat.ui.communities.model.CommunityFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommunitiesViewModel @Inject constructor(
    private val communityRepository: CommunityRepository
) : ViewModel() {

    private val _communities = MutableStateFlow<List<Community>>(emptyList())
    val communities: StateFlow<List<Community>> = _communities.asStateFlow()

    private val _userCommunities = MutableStateFlow<List<Community>>(emptyList())
    val userCommunities: StateFlow<List<Community>> = _userCommunities.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _currentFilter = MutableStateFlow(CommunityFilter.ALL)
    val currentFilter: StateFlow<CommunityFilter> = _currentFilter.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadCommunities()
        loadUserCommunities()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        searchCommunities(query)
    }

    fun updateFilter(filter: CommunityFilter) {
        _currentFilter.value = filter
        when (filter) {
            CommunityFilter.ALL -> loadCommunities()
            CommunityFilter.MY_COMMUNITIES -> loadUserCommunities()
            CommunityFilter.TRENDING -> loadTrendingCommunities()
        }
    }

    fun joinCommunity(communityId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                communityRepository.joinCommunity(communityId)
                updateCommunityLists()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun leaveCommunity(communityId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                communityRepository.leaveCommunity(communityId)
                updateCommunityLists()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createCommunity(community: Community) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                communityRepository.createCommunity(community)
                loadCommunities() // Recargar la lista después de crear
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al crear la comunidad"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun subscribeToCommunity(communityId: String) {
        viewModelScope.launch {
            try {
                communityRepository.subscribeCommunity(communityId)
                loadCommunities() // Recargar la lista después de suscribirse
            } catch (e: Exception) {
                _error.value = e.message ?: "Error al suscribirse"
            }
        }
    }

    private fun loadCommunities() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                communityRepository.getCommunities().collect { communities ->
                    _communities.value = communities
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadUserCommunities() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                communityRepository.getUserCommunities().collect { communities ->
                    _userCommunities.value = communities
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadTrendingCommunities() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                communityRepository.getTrendingCommunities().collect { trending ->
                    _communities.value = trending
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun searchCommunities(query: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                communityRepository.searchCommunities(query).collect { results ->
                    _communities.value = results
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun updateCommunityLists() {
        loadCommunities()
        loadUserCommunities()
    }

    fun clearError() {
        _error.value = null
    }

    // Método para obtener comunidades populares
    fun getTrendingCommunities(): List<Community> {
        return _communities.value
    }
}
