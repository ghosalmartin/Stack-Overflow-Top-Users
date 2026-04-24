package com.fnabl.domain.users

import com.fnabl.domain.repository.TopUsersState

fun reduceUsersState(
    topUsersState: TopUsersState,
    followedUserIds: Set<Long>,
): UsersUiState =
    when (topUsersState) {
        TopUsersState.Loading -> UsersUiState.Loading
        is TopUsersState.Loaded ->
            UsersUiState.Loaded(
                users = topUsersState.users.toRows(followedUserIds),
                isRefreshing = topUsersState.isRefreshing,
                hasMore = topUsersState.nextPage != null,
                isAppending = topUsersState.isAppending,
            )
        is TopUsersState.Failed -> UsersUiState.Error(topUsersState.cause.toUserMessage())
    }

private fun Throwable.toUserMessage(): String = message?.takeIf { it.isNotBlank() } ?: "Something went wrong. Please try again."
