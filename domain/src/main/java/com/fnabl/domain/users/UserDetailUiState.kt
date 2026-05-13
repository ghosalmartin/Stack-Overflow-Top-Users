package com.fnabl.domain.users

sealed interface UserDetailUiState {
    data object Loading : UserDetailUiState

    data object NotFound : UserDetailUiState

    data class Loaded(
        val user: UserRow,
    ) : UserDetailUiState

    data class Error(
        val message: String,
    ) : UserDetailUiState
}
