package com.fnabl.domain.users

import com.fnabl.domain.model.User
import com.fnabl.domain.repository.TopUsersState
import org.junit.Assert.assertEquals
import org.junit.Test

class UserDetailStateReducerTest {
    @Test
    fun `Loading topUsers yields Loading`() {
        // Given / When
        val state = reduceUserDetailState(userId = 1L, TopUsersState.Loading, followedUserIds = emptySet())

        // Then
        assertEquals(UserDetailUiState.Loading, state)
    }

    @Test
    fun `Loaded list containing the user yields Loaded with isFollowed propagated`() {
        // Given
        val users = listOf(user(1L, "Alice"), user(2L, "Bob"))

        // When
        val state =
            reduceUserDetailState(userId = 2L, TopUsersState.Loaded(users), followedUserIds = setOf(2L))

        // Then
        assertEquals(UserDetailUiState.Loaded::class, state::class)
        val loaded = state as UserDetailUiState.Loaded
        assertEquals("Bob", loaded.user.displayName)
        assertEquals(true, loaded.user.isFollowed)
    }

    @Test
    fun `Loaded list without the user yields NotFound`() {
        // Given
        val users = listOf(user(1L, "Alice"))

        // When
        val state =
            reduceUserDetailState(userId = 99L, TopUsersState.Loaded(users), followedUserIds = emptySet())

        // Then
        assertEquals(UserDetailUiState.NotFound, state)
    }

    @Test
    fun `Loaded empty list yields NotFound rather than throwing`() {
        // Given / When
        val state =
            reduceUserDetailState(userId = 1L, TopUsersState.Loaded(emptyList()), followedUserIds = emptySet())

        // Then
        assertEquals(UserDetailUiState.NotFound, state)
    }

    @Test
    fun `Failed topUsers with message surfaces the message as Error`() {
        // Given / When
        val state =
            reduceUserDetailState(
                userId = 1L,
                TopUsersState.Failed(RuntimeException("No network")),
                followedUserIds = emptySet(),
            )

        // Then
        assertEquals(UserDetailUiState.Error("No network"), state)
    }

    @Test
    fun `Failed topUsers with blank message falls back to a generic error`() {
        // Given / When
        val state =
            reduceUserDetailState(
                userId = 1L,
                TopUsersState.Failed(RuntimeException("   ")),
                followedUserIds = emptySet(),
            )

        // Then
        assertEquals(UserDetailUiState.Error("Something went wrong. Please try again."), state)
    }

    private fun user(
        id: Long,
        name: String,
    ) = User(
        id = id,
        displayName = name,
        reputation = 100,
        profileImageUrl = "http://img/$id",
        websiteUrl = null,
        location = null,
        creationDate = 0L,
        lastModifiedDate = null,
    )
}
