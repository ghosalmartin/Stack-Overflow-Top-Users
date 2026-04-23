package com.fnabl.domain.repository

import com.fnabl.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UsersRepository {
    val topUsers: Flow<TopUsersState>

    suspend fun refresh()
}

sealed interface TopUsersState {
    data object Loading : TopUsersState

    data class Loaded(
        val users: List<User>,
        val isRefreshing: Boolean = false,
    ) : TopUsersState

    data class Failed(
        val cause: Throwable,
    ) : TopUsersState
}
