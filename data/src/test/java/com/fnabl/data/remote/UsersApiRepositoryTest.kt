package com.fnabl.data.remote

import com.fnabl.data.remote.dto.UserDto
import com.fnabl.data.remote.dto.UsersResponseDto
import com.fnabl.domain.repository.TopUsersState
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UsersApiRepositoryTest {

    private val api: StackOverflowApi = mockk()
    private val repo = UsersApiRepository(api)

    @Test
    fun `initial topUsers value is Loading`() {
        // Given / When / Then
        assertEquals(TopUsersState.Loading, repo.topUsers.value)
    }

    @Test
    fun `successful refresh maps response to Loaded`() = runTest {
        // Given
        coEvery { api.getTopUsers(pageSize = any()) } returns UsersResponseDto(
            items = listOf(userDto(id = 1L, name = "Alice")),
        )

        // When
        repo.refresh()

        // Then
        val loaded = repo.topUsers.value as TopUsersState.Loaded
        assertEquals(1L, loaded.users.single().id)
        assertEquals("Alice", loaded.users.single().displayName)
        assertFalse(loaded.isRefreshing)
    }

    @Test
    fun `refresh failure wraps the throwable in Failed`() = runTest {
        // Given
        val cause = RuntimeException("500")
        coEvery { api.getTopUsers(pageSize = any()) } throws cause

        // When
        repo.refresh()

        // Then
        assertEquals(TopUsersState.Failed(cause), repo.topUsers.value)
    }

    @Test
    fun `re-refresh exposes cached data with isRefreshing=true during the fetch`() = runTest {
        // Given: cache is already Loaded
        coEvery { api.getTopUsers(pageSize = any()) } returns UsersResponseDto(
            items = listOf(userDto(id = 1L, name = "Alice")),
        )
        repo.refresh()

        // And: a second refresh whose response we can gate
        val deferred = CompletableDeferred<UsersResponseDto>()
        coEvery { api.getTopUsers(pageSize = any()) } coAnswers { deferred.await() }

        // When: refresh starts but the API call hasn't returned
        val job = launch { repo.refresh() }
        advanceUntilIdle()

        // Then: cache still shows the old users, with isRefreshing true
        val midFlight = repo.topUsers.value as TopUsersState.Loaded
        assertEquals("Alice", midFlight.users.single().displayName)
        assertTrue(midFlight.isRefreshing)

        // When: the API call completes with fresh data
        deferred.complete(UsersResponseDto(items = listOf(userDto(id = 2L, name = "Bob"))))
        job.join()

        // Then: cache settles to the new users, isRefreshing cleared
        val settled = repo.topUsers.value as TopUsersState.Loaded
        assertEquals("Bob", settled.users.single().displayName)
        assertFalse(settled.isRefreshing)
    }

    @Test
    fun `retry from Failed transitions through Loading before the next outcome`() = runTest {
        // Given: first refresh failed
        coEvery { api.getTopUsers(pageSize = any()) } throws RuntimeException("boom")
        repo.refresh()
        assertTrue(repo.topUsers.value is TopUsersState.Failed)

        // And: a second refresh whose response is gated
        val deferred = CompletableDeferred<UsersResponseDto>()
        coEvery { api.getTopUsers(pageSize = any()) } coAnswers { deferred.await() }

        // When: retry starts
        val job = launch { repo.refresh() }
        advanceUntilIdle()

        // Then: cache flipped Failed → Loading while the retry is in flight
        assertEquals(TopUsersState.Loading, repo.topUsers.value)

        // When: the retry completes
        deferred.complete(UsersResponseDto(items = listOf(userDto(id = 1L, name = "Alice"))))
        job.join()

        // Then: cache settles to Loaded with the new users
        val loaded = repo.topUsers.value as TopUsersState.Loaded
        assertEquals("Alice", loaded.users.single().displayName)
    }

    @Test
    fun `CancellationException is rethrown rather than wrapped in Failed`() = runTest {
        // Given
        coEvery { api.getTopUsers(pageSize = any()) } throws CancellationException("test")

        // When
        val outcome = runCatching { repo.refresh() }

        // Then
        assertTrue(outcome.exceptionOrNull() is CancellationException)
    }

    private fun userDto(id: Long, name: String) = UserDto(
        userId = id,
        displayName = name,
        reputation = 100,
        profileImage = "http://img/$id",
    )
}
