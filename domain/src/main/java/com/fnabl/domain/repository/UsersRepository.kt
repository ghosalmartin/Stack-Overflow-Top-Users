package com.fnabl.domain.repository

import com.fnabl.domain.model.User
import com.fnabl.domain.users.UserSortSelection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface UsersRepository {
    val topUsers: Flow<TopUsersState>

    val currentSort: StateFlow<UserSortSelection>

    suspend fun refresh()

    suspend fun loadMore()

    suspend fun setSort(selection: UserSortSelection)
}

sealed interface TopUsersState {
    data object Loading : TopUsersState

    data class Loaded(
        val users: List<User>,
        val nextPage: Int? = null,
        val isRefreshing: Boolean = false,
        val isAppending: Boolean = false,
    ) : TopUsersState

    data class Failed(
        val cause: Throwable,
    ) : TopUsersState
}
