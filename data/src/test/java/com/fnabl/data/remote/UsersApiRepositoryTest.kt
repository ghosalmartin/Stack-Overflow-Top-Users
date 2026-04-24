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
    fun `loadMore appends the next page to the cached users`() = runTest {
        // Given: first page loaded with hasMore
        coEvery { api.getTopUsers(page = 1, pageSize = any()) } returns UsersResponseDto(
            items = listOf(userDto(id = 1L, name = "Alice")),
            hasMore = true,
        )
        repo.refresh()

        // And: second page response set up
        coEvery { api.getTopUsers(page = 2, pageSize = any()) } returns UsersResponseDto(
            items = listOf(userDto(id = 2L, name = "Bob")),
            hasMore = false,
        )

        // When
        repo.loadMore()

        // Then
        val loaded = repo.topUsers.value as TopUsersState.Loaded
        assertEquals(listOf("Alice", "Bob"), loaded.users.map { it.displayName })
        assertFalse(loaded.isAppending)
        assertEquals(null, loaded.nextPage)
    }

    @Test
    fun `loadMore failure keeps cached data and clears isAppending`() = runTest {
        // Given: first page loaded with hasMore
        coEvery { api.getTopUsers(page = 1, pageSize = any()) } returns UsersResponseDto(
            items = listOf(userDto(id = 1L, name = "Alice")),
            hasMore = true,
        )
        repo.refresh()

        // And: second page throws
        coEvery { api.getTopUsers(page = 2, pageSize = any()) } throws RuntimeException("500")

        // When
        repo.loadMore()

        // Then: cache still holds original users, isAppending cleared, nextPage intact for retry
        val loaded = repo.topUsers.value as TopUsersState.Loaded
        assertEquals(listOf("Alice"), loaded.users.map { it.displayName })
        assertFalse(loaded.isAppending)
        assertEquals(2, loaded.nextPage)
    }

    @Test
    fun `loadMore is a no-op when an append is already in flight`() = runTest {
        // Given: first page loaded
        coEvery { api.getTopUsers(page = 1, pageSize = any()) } returns UsersResponseDto(
            items = listOf(userDto(id = 1L, name = "Alice")),
            hasMore = true,
        )
        repo.refresh()

        // And: second-page response is gated
        val deferred = CompletableDeferred<UsersResponseDto>()
        coEvery { api.getTopUsers(page = 2, pageSize = any()) } coAnswers { deferred.await() }

        // When: first loadMore starts and blocks on the gated response
        val first = launch { repo.loadMore() }
        advanceUntilIdle()
        assertTrue((repo.topUsers.value as TopUsersState.Loaded).isAppending)

        // And: a second loadMore is triggered while the first is still in flight
        repo.loadMore()
        advanceUntilIdle()

        // Then: the API was only hit once for page 2
        io.mockk.coVerify(exactly = 1) { api.getTopUsers(page = 2, pageSize = any()) }

        // Cleanup
        deferred.complete(UsersResponseDto(items = emptyList(), hasMore = false))
        first.join()
    }

    @Test
    fun `loadMore is a no-op when the cache has no next page`() = runTest {
        // Given: first page loaded with hasMore=false
        coEvery { api.getTopUsers(page = 1, pageSize = any()) } returns UsersResponseDto(
            items = listOf(userDto(id = 1L, name = "Alice")),
            hasMore = false,
        )
        repo.refresh()
        val before = repo.topUsers.value

        // When
        repo.loadMore()

        // Then: state is unchanged; no second-page fetch happened
        assertEquals(before, repo.topUsers.value)
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
