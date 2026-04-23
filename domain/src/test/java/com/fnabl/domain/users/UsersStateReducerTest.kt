package com.fnabl.domain.users

import com.fnabl.domain.model.User
import com.fnabl.domain.repository.TopUsersState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UsersStateReducerTest {
    @Test
    fun `loading state yields Loading`() {
        // Given / When
        val state = reduceUsersState(TopUsersState.Loading, followedUserIds = emptySet())

        // Then
        assertEquals(UsersUiState.Loading, state)
    }

    @Test
    fun `loaded state maps users to rows and marks followed ones`() {
        // Given
        val users =
            listOf(
                User(id = 1L, displayName = "Alice", reputation = 1_234, profileImageUrl = "http://img/1"),
                User(id = 2L, displayName = "Bob", reputation = 9_000, profileImageUrl = "http://img/2"),
            )

        // When
        val state = reduceUsersState(TopUsersState.Loaded(users), followedUserIds = setOf(2L))

        // Then
        assertTrue(state is UsersUiState.Loaded)
        val loaded = state as UsersUiState.Loaded
        assertEquals(2, loaded.users.size)
        assertEquals(false, loaded.users[0].isFollowed)
        assertEquals(true, loaded.users[1].isFollowed)
        assertEquals("Bob", loaded.users[1].displayName)
    }

    @Test
    fun `loaded state with empty list yields Loaded with empty rows`() {
        // Given / When
        val state = reduceUsersState(TopUsersState.Loaded(emptyList()), followedUserIds = setOf(1L, 2L))

        // Then
        assertEquals(UsersUiState.Loaded(emptyList()), state)
    }

    @Test
    fun `failed state with message surfaces that message`() {
        // Given / When
        val state = reduceUsersState(TopUsersState.Failed(RuntimeException("No network")), followedUserIds = emptySet())

        // Then
        assertEquals(UsersUiState.Error("No network"), state)
    }

    @Test
    fun `failed state without message falls back to a generic user-facing error`() {
        // Given / When
        val state = reduceUsersState(TopUsersState.Failed(RuntimeException()), followedUserIds = emptySet())

        // Then
        assertEquals(UsersUiState.Error("Something went wrong. Please try again."), state)
    }

    @Test
    fun `failed state with blank message falls back to the generic error`() {
        // Given / When
        val state = reduceUsersState(TopUsersState.Failed(RuntimeException("   ")), followedUserIds = emptySet())

        // Then
        assertEquals(UsersUiState.Error("Something went wrong. Please try again."), state)
    }
}
