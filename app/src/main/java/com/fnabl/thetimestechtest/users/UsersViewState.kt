package com.fnabl.thetimestechtest.users

import com.fnabl.domain.users.UsersUiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

sealed interface UsersViewState {
    data object Loading : UsersViewState

    sealed interface Loaded : UsersViewState {
        data class UserList(
            val users: ImmutableList<UserRowUiModel>,
            val isRefreshing: Boolean = false,
            val hasMore: Boolean = false,
            val isAppending: Boolean = false,
        ) : Loaded
        data object EmptyUserList : Loaded
    }

    data class Error(val message: String) : UsersViewState
}

fun UsersUiState.toViewState(): UsersViewState =
    when (this) {
        UsersUiState.Loading -> UsersViewState.Loading
        is UsersUiState.Loaded -> when {
            users.isEmpty() -> UsersViewState.Loaded.EmptyUserList
            else -> UsersViewState.Loaded.UserList(
                users = users.map { it.toUiModel() }.toImmutableList(),
                isRefreshing = isRefreshing,
                hasMore = hasMore,
                isAppending = isAppending,
            )
        }
        is UsersUiState.Error -> UsersViewState.Error(message)
    }
