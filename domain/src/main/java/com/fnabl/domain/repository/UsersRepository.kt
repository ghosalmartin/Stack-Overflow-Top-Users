package com.fnabl.domain.repository

import com.fnabl.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UsersRepository {
    val topUsers: Flow<TopUsersState>

    suspend fun refresh()

    suspend fun loadMore()
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
