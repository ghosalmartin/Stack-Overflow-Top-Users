package com.fnabl.thetimestechtest.users.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.fnabl.thetimestechtest.users.UserRowUiModel
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserList(
    users: ImmutableList<UserRowUiModel>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onToggleFollow: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(items = users, key = { it.id }) { row ->
                UserRowItem(row = row, onToggleFollow = onToggleFollow)
                HorizontalDivider()
            }
        }
    }
}
