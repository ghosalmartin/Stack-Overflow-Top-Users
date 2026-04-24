package com.fnabl.domain.users

sealed interface UsersIntent {
    data object Refresh : UsersIntent

    data object LoadMore : UsersIntent

    data class ToggleFollow(
        val userId: Long,
    ) : UsersIntent
}
