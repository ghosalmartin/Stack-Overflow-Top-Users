package com.fnabl.thetimestechtest.users

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fnabl.domain.users.UserDetailUiState
import com.fnabl.thetimestechtest.users.components.UserAvatar

@Composable
fun UserDetailScreen(
    viewModel: UserDetailViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    UserDetailContent(
        state = state,
        onToggleFollow = viewModel::toggleFollow,
        modifier = modifier,
    )
}

@Composable
private fun UserDetailContent(
    state: UserDetailUiState,
    onToggleFollow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.statusBars)
            .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        when (state) {
            UserDetailUiState.Loading -> CircularProgressIndicator()
            UserDetailUiState.NotFound -> Text("User not found.")
            is UserDetailUiState.Error -> Text(state.message)
            is UserDetailUiState.Loaded -> LoadedUserDetail(
                row = state.user.toUiModel(),
                onToggleFollow = onToggleFollow,
            )
        }
    }
}

@Composable
private fun LoadedUserDetail(
    row: UserRowUiModel,
    onToggleFollow: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            UserAvatar(
                profileImageUrl = row.profileImageUrl,
                modifier = Modifier.size(128.dp),
            )
        }
        Text(text = row.displayName, style = MaterialTheme.typography.headlineSmall)
        Text(text = "Reputation: ${row.displayReputation}")
        row.location?.let { Text(text = "Location: $it") }
        row.websiteUrl?.takeIf { it.isNotBlank() }?.let { Text(text = "Website: $it") }
        Button(onClick = onToggleFollow, modifier = Modifier.fillMaxWidth()) {
            Text(if (row.isFollowed) "Unfollow" else "Follow")
        }
    }
}
