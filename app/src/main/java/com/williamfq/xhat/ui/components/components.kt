/*
 * Updated: 2025-01-21 18:03:39
 * Author: William8677
 */

package com.williamfq.xhat.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.williamfq.xhat.R
import com.williamfq.xhat.ui.main.MainScreens
import com.williamfq.xhat.ui.main.MainScreens.CALLS
import com.williamfq.xhat.ui.main.MainScreens.CHANNELS
import com.williamfq.xhat.ui.main.MainScreens.CHATS
import com.williamfq.xhat.ui.main.MainScreens.CHAT_ROOMS
import com.williamfq.xhat.ui.main.MainScreens.COMMUNITIES
import com.williamfq.xhat.ui.main.MainScreens.STORIES
import com.williamfq.xhat.ui.theme.XhatTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppBar(
    navController: NavController,
    pagerState: PagerState,
    onMenuClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        tonalElevation = 4.dp
    ) {
        Column {
            // Top App Bar
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menú"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Implementar búsqueda */ }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar"
                        )
                    }
                    IconButton(onClick = { /* TODO: Implementar más opciones */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Más opciones"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )

            // Bottom Navigation
            BottomNavigationBar(navController, pagerState)
        }
    }
}

@Composable
private fun BottomNavigationBar(
    navController: NavController,
    pagerState: PagerState
) {
    val coroutineScope = rememberCoroutineScope()
    val screens = MainScreens.entries.toTypedArray()

    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 8.dp
    ) {
        screens.forEachIndexed { index, screen ->
            NavigationBarItem(
                icon = {
                    when (screen) {
                        CHATS -> Icon(Icons.AutoMirrored.Filled.Chat, "Chats")
                        STORIES -> Icon(Icons.Default.PlayCircle, "Historias")
                        CHANNELS -> Icon(Icons.Default.Tv, "Canales")
                        COMMUNITIES -> Icon(Icons.Default.Groups, "Comunidades")
                        CHAT_ROOMS -> Icon(Icons.Default.Forum, "Salas")
                        CALLS -> Icon(Icons.Default.Call, "Llamadas")
                    }
                },
                label = { Text(screen.name) },
                selected = pagerState.currentPage == index,
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainAppBarPreview() {
    XhatTheme {
        MainAppBar(
            navController = rememberNavController(),
            pagerState = rememberPagerState(initialPage = 0)
        )
    }
}