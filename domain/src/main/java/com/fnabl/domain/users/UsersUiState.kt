package com.fnabl.domain.users

sealed interface UsersUiState {
    data object Loading : UsersUiState

    data class Loaded(
        val users: List<UserRow>,
        val isRefreshing: Boolean = false,
        val hasMore: Boolean = false,
        val isAppending: Boolean = false,
    ) : UsersUiState

    data class Error(
        val message: String,
    ) : UsersUiState
}
