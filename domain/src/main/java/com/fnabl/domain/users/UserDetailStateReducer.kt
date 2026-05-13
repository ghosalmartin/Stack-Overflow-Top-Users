package com.fnabl.domain.users

import com.fnabl.domain.repository.TopUsersState

fun reduceUserDetailState(
    userId: Long,
    topUsersState: TopUsersState,
    followedUserIds: Set<Long>,
): UserDetailUiState =
    when (topUsersState) {
        TopUsersState.Loading -> UserDetailUiState.Loading
        is TopUsersState.Failed -> UserDetailUiState.Error(topUsersState.cause.toUserMessage())
        is TopUsersState.Loaded ->
            topUsersState.users
                .selectUserRow(userId, isFollowed = userId in followedUserIds)
                ?.let(UserDetailUiState::Loaded)
                ?: UserDetailUiState.NotFound
    }

private fun Throwable.toUserMessage(): String = message?.takeIf { it.isNotBlank() } ?: "Something went wrong. Please try again."
