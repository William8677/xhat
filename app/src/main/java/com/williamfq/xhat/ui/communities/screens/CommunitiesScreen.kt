/*
 * Updated: 2025-01-25 23:39:57
 * Author: William8677
 *
 * Este archivo forma parte de la app xhat.
 */

package com.williamfq.xhat.ui.communities.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.williamfq.domain.model.Community
import com.williamfq.xhat.ui.communities.model.CommunityFilter
import com.williamfq.xhat.ui.communities.viewmodel.CommunitiesViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CommunitiesScreen(
    onNavigateToCreateCommunity: () -> Unit,
    onNavigateToCommunityDetail: (String) -> Unit,
    viewModel: CommunitiesViewModel = hiltViewModel()
) {
    val communities by viewModel.communities.collectAsState()
    val userCommunities by viewModel.userCommunities.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()
    var showSearchBar by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CommunitiesTopBar(
                searchQuery = searchQuery,
                onSearchQueryChange = viewModel::updateSearchQuery,
                onFilterSelect = viewModel::updateFilter,
                showSearchBar = showSearchBar,
                onSearchBarVisibilityChange = { showSearchBar = it }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreateCommunity) {
                Icon(Icons.Default.Add, contentDescription = "Crear comunidad")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            CommunityTabs(
                selectedTab = currentFilter,
                onTabSelected = viewModel::updateFilter
            )

            when (currentFilter) {
                CommunityFilter.ALL -> CommunitiesList(
                    communities = communities,
                    onCommunityClick = onNavigateToCommunityDetail,
                    onJoinClick = viewModel::joinCommunity,
                    onLeaveClick = viewModel::leaveCommunity
                )
                CommunityFilter.MY_COMMUNITIES -> CommunitiesList(
                    communities = userCommunities,
                    onCommunityClick = onNavigateToCommunityDetail,
                    onJoinClick = viewModel::joinCommunity,
                    onLeaveClick = viewModel::leaveCommunity
                )
                CommunityFilter.TRENDING -> CommunitiesList(
                    communities = viewModel.getTrendingCommunities(),
                    onCommunityClick = onNavigateToCommunityDetail,
                    onJoinClick = viewModel::joinCommunity,
                    onLeaveClick = viewModel::leaveCommunity
                )
                else -> CommunitiesList(
                    communities = communities,
                    onCommunityClick = onNavigateToCommunityDetail,
                    onJoinClick = viewModel::joinCommunity,
                    onLeaveClick = viewModel::leaveCommunity
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommunitiesTopBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onFilterSelect: (CommunityFilter) -> Unit,
    showSearchBar: Boolean,
    onSearchBarVisibilityChange: (Boolean) -> Unit
) {
    TopAppBar(
        title = {
            if (showSearchBar) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    onSearch = { /* Implementar búsqueda */ },
                    active = showSearchBar,
                    onActiveChange = onSearchBarVisibilityChange,
                    placeholder = { Text("Buscar comunidades...") }
                ) {
                    // Sugerencias de búsqueda (si se requieren)
                }
            } else {
                Text("Comunidades")
            }
        },
        actions = {
            IconButton(onClick = { onSearchBarVisibilityChange(!showSearchBar) }) {
                Icon(
                    imageVector = if (showSearchBar) Icons.Default.Close else Icons.Default.Search,
                    contentDescription = if (showSearchBar) "Cerrar búsqueda" else "Buscar"
                )
            }
            IconButton(onClick = { /* Abrir filtros */ }) {
                Icon(Icons.Default.FilterList, contentDescription = "Filtrar")
            }
        }
    )
}

@Composable
private fun CommunityTabs(
    selectedTab: CommunityFilter,
    onTabSelected: (CommunityFilter) -> Unit
) {
    ScrollableTabRow(
        selectedTabIndex = CommunityFilter.entries.indexOf(selectedTab)
    ) {
        CommunityFilter.entries.forEach { filter ->
            Tab(
                selected = filter == selectedTab,
                onClick = { onTabSelected(filter) },
                text = { Text(filter.title) }
            )
        }
    }
}

@Composable
private fun CommunitiesList(
    communities: List<Community>,
    onCommunityClick: (String) -> Unit,
    onJoinClick: (String) -> Unit,
    onLeaveClick: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(communities) { community ->
            CommunityCard(
                community = community,
                onCommunityClick = { onCommunityClick(community.id) },
                onJoinClick = { onJoinClick(community.id) },
                onLeaveClick = { onLeaveClick(community.id) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun CommunityCard(
    community: Community,
    onCommunityClick: () -> Unit,
    onJoinClick: () -> Unit,
    onLeaveClick: () -> Unit
) {
    Card(
        onClick = onCommunityClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            AsyncImage(
                model = community.bannerUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = community.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "${community.memberCount} miembros",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Button(
                    onClick = if (community.isJoined) onLeaveClick else onJoinClick
                ) {
                    Text(if (community.isJoined) "Abandonar" else "Unirse")
                }
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                community.tags.forEach { tag ->
                    AssistChip(
                        onClick = { },
                        label = { Text(tag) }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                CommunityStatistic(
                    icon = Icons.Default.Forum,
                    value = community.postsCount.toString(),
                    label = "Posts"
                )
                CommunityStatistic(
                    icon = Icons.Default.Group,
                    value = community.activeUsers.toString(),
                    label = "Activos"
                )
                CommunityStatistic(
                    icon = Icons.Default.Star,
                    value = String.format("%.1f", community.ranking),
                    label = "Ranking"
                )
            }
        }
    }
}

@Composable
private fun CommunityStatistic(
    icon: ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
