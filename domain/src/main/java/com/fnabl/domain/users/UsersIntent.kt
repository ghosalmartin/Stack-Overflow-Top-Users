package com.fnabl.domain.users

sealed interface UsersIntent {
    data object Refresh : UsersIntent

    data class ToggleFollow(
        val userId: Long,
    ) : UsersIntent
}
