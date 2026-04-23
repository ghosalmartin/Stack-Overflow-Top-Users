package com.fnabl.thetimestechtest.users

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.fnabl.domain.users.UsersIntent
import com.fnabl.thetimestechtest.R
import com.fnabl.thetimestechtest.users.components.EmptyUserList
import com.fnabl.thetimestechtest.users.components.ErrorContent
import com.fnabl.thetimestechtest.users.components.LoadingContent
import com.fnabl.thetimestechtest.users.components.UserList

@Composable
fun UsersScreen(
    modifier: Modifier = Modifier,
    viewModel: UsersViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    UsersContent(
        state = state,
        onIntent = viewModel::onIntent,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersContent(
    state: UsersViewState,
    onIntent: (UsersIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.screen_title)) },
            )
        },
    ) { padding ->
        Crossfade(
            targetState = state,
            label = "users-state",
            modifier = Modifier.fillMaxSize().padding(padding),
        ) { current ->
            when (current) {
                UsersViewState.Loading -> LoadingContent()

                is UsersViewState.Loaded.UserList -> UserList(
                    users = current.users,
                    isRefreshing = current.isRefreshing,
                    onRefresh = { onIntent(UsersIntent.Refresh) },
                    onToggleFollow = { id -> onIntent(UsersIntent.ToggleFollow(id)) },
                )

                UsersViewState.Loaded.EmptyUserList -> EmptyUserList()

                is UsersViewState.Error -> ErrorContent(
                    message = current.message,
                    onRetry = { onIntent(UsersIntent.Refresh) },
                )
            }
        }
    }
}
