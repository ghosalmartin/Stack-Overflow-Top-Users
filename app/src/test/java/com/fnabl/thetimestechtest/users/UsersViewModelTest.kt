package com.fnabl.thetimestechtest.users

import com.fnabl.domain.model.User
import com.fnabl.domain.repository.FollowRepository
import com.fnabl.domain.repository.TopUsersState
import com.fnabl.domain.repository.UsersRepository
import com.fnabl.domain.users.UsersIntent
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UsersViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val topUsersFlow = MutableStateFlow<TopUsersState>(TopUsersState.Loading)
    private val followedIds = MutableStateFlow<Set<Long>>(emptySet())
    private var nextOutcome: TopUsersState = TopUsersState.Loaded(emptyList())

    private val usersRepository: UsersRepository = mockk {
        every { topUsers } returns topUsersFlow
        coEvery { refresh() } coAnswers {
            topUsersFlow.value = TopUsersState.Loading
            topUsersFlow.value = nextOutcome
        }
    }

    private val followRepository: FollowRepository = mockk {
        every { followedUserIds } returns followedIds
    }

    init {
        Dispatchers.setMain(dispatcher)
    }

    @Test
    fun `subscribing triggers a refresh and exposes a Loaded state`() = runTest(dispatcher) {
        // Given
        nextOutcome = TopUsersState.Loaded(listOf(user(1L, "Alice"), user(2L, "Bob")))

        // When
        val vm = UsersViewModel(usersRepository, followRepository)
        val state = vm.state.subscribedIn(this)
        advanceUntilIdle()

        // Then
        val loaded = state.value as UsersViewState.Loaded.UserList
        assertEquals(listOf("Alice", "Bob"), loaded.users.map { it.displayName })
        assertTrue(loaded.users.none { it.isFollowed })
        assertEquals("100", loaded.users[0].displayReputation)
        coVerify(exactly = 1) { usersRepository.refresh() }
    }

    @Test
    fun `failed refresh exposes an Error state with the cause message`() = runTest(dispatcher) {
        // Given
        nextOutcome = TopUsersState.Failed(RuntimeException("boom"))

        // When
        val vm = UsersViewModel(usersRepository, followRepository)
        val state = vm.state.subscribedIn(this)
        advanceUntilIdle()

        // Then
        assertEquals(UsersViewState.Error("boom"), state.value)
    }

    @Test
    fun `refresh intent re-invokes the repository`() = runTest(dispatcher) {
        // Given
        nextOutcome = TopUsersState.Loaded(emptyList())
        val vm = UsersViewModel(usersRepository, followRepository)
        vm.state.subscribedIn(this)
        advanceUntilIdle()

        // When
        vm.onIntent(UsersIntent.Refresh)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 2) { usersRepository.refresh() }
    }

    @Test
    fun `toggling an unfollowed user calls follow`() = runTest(dispatcher) {
        // Given
        nextOutcome = TopUsersState.Loaded(listOf(user(7L, "Alice")))
        coEvery { followRepository.follow(7L) } just Runs
        coEvery { followRepository.unfollow(any()) } just Runs
        val vm = UsersViewModel(usersRepository, followRepository)
        vm.state.subscribedIn(this)
        advanceUntilIdle()

        // When
        vm.onIntent(UsersIntent.ToggleFollow(7L))
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { followRepository.follow(7L) }
        coVerify(exactly = 0) { followRepository.unfollow(any()) }
    }

    @Test
    fun `toggling an already-followed user calls unfollow`() = runTest(dispatcher) {
        // Given
        followedIds.value = setOf(7L)
        nextOutcome = TopUsersState.Loaded(listOf(user(7L, "Alice")))
        coEvery { followRepository.follow(any()) } just Runs
        coEvery { followRepository.unfollow(7L) } just Runs
        val vm = UsersViewModel(usersRepository, followRepository)
        vm.state.subscribedIn(this)
        advanceUntilIdle()

        // When
        vm.onIntent(UsersIntent.ToggleFollow(7L))
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { followRepository.unfollow(7L) }
        coVerify(exactly = 0) { followRepository.follow(any()) }
    }

    @Test
    fun `followed ids stream re-derives isFollowed without another users repository call`() = runTest(dispatcher) {
        // Given
        nextOutcome = TopUsersState.Loaded(listOf(user(1L, "Alice"), user(2L, "Bob")))
        val vm = UsersViewModel(usersRepository, followRepository)
        val state = vm.state.subscribedIn(this)
        advanceUntilIdle()
        assertTrue((state.value as UsersViewState.Loaded.UserList).users.none { it.isFollowed })

        // When
        followedIds.value = setOf(2L)
        advanceUntilIdle()

        // Then
        val loaded = state.value as UsersViewState.Loaded.UserList
        assertEquals(listOf(false, true), loaded.users.map { it.isFollowed })
        coVerify(exactly = 1) { usersRepository.refresh() }
    }

    private fun user(id: Long, name: String) =
        User(id = id, displayName = name, reputation = 100, profileImageUrl = "http://img/$id")

    private fun <T> StateFlow<T>.subscribedIn(scope: TestScope): StateFlow<T> {
        scope.backgroundScope.launch { collect {} }
        return this
    }
}
