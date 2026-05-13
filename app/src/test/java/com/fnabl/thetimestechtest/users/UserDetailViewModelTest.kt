package com.fnabl.thetimestechtest.users

import androidx.lifecycle.SavedStateHandle
import com.fnabl.domain.model.User
import com.fnabl.domain.repository.FollowRepository
import com.fnabl.domain.repository.TopUsersState
import com.fnabl.domain.repository.UsersRepository
import com.fnabl.domain.users.UserDetailUiState
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
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserDetailViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private val topUsersFlow = MutableStateFlow<TopUsersState>(TopUsersState.Loading)
    private val followedIds = MutableStateFlow<Set<Long>>(emptySet())

    private val usersRepository: UsersRepository = mockk {
        every { topUsers } returns topUsersFlow
    }

    private val followRepository: FollowRepository = mockk {
        every { followedUserIds } returns followedIds
    }

    init {
        Dispatchers.setMain(dispatcher)
    }

    @Test
    fun `state is Loading while topUsers is Loading`() = runTest(dispatcher) {
        // Given / When
        val vm = vm(userId = 1L)
        val state = vm.state.subscribedIn(this)
        advanceUntilIdle()

        // Then
        assertEquals(UserDetailUiState.Loading, state.value)
    }

    @Test
    fun `state is Loaded with isFollowed=true when user is in cache and is followed`() = runTest(dispatcher) {
        // Given
        topUsersFlow.value = TopUsersState.Loaded(listOf(user(7L, "Alice")))
        followedIds.value = setOf(7L)

        // When
        val state = vm(userId = 7L).state.subscribedIn(this)
        advanceUntilIdle()

        // Then
        val loaded = state.value as UserDetailUiState.Loaded
        assertEquals("Alice", loaded.user.displayName)
        assertEquals(true, loaded.user.isFollowed)
    }

    @Test
    fun `state is NotFound when topUsers is Loaded but the id is missing`() = runTest(dispatcher) {
        // Given
        topUsersFlow.value = TopUsersState.Loaded(listOf(user(1L, "Alice")))

        // When
        val state = vm(userId = 99L).state.subscribedIn(this)
        advanceUntilIdle()

        // Then
        assertEquals(UserDetailUiState.NotFound, state.value)
    }

    @Test
    fun `state is Error when topUsers is Failed`() = runTest(dispatcher) {
        // Given
        topUsersFlow.value = TopUsersState.Failed(RuntimeException("offline"))

        // When
        val state = vm(userId = 1L).state.subscribedIn(this)
        advanceUntilIdle()

        // Then
        assertEquals(UserDetailUiState.Error("offline"), state.value)
    }

    @Test
    fun `toggleFollow on an unfollowed user calls follow`() = runTest(dispatcher) {
        // Given
        topUsersFlow.value = TopUsersState.Loaded(listOf(user(7L, "Alice")))
        coEvery { followRepository.follow(7L) } just Runs
        coEvery { followRepository.unfollow(any()) } just Runs

        // When
        val vm = vm(userId = 7L)
        vm.state.subscribedIn(this)
        advanceUntilIdle()
        vm.toggleFollow()
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { followRepository.follow(7L) }
        coVerify(exactly = 0) { followRepository.unfollow(any()) }
    }

    @Test
    fun `toggleFollow on an already-followed user calls unfollow`() = runTest(dispatcher) {
        // Given
        topUsersFlow.value = TopUsersState.Loaded(listOf(user(7L, "Alice")))
        followedIds.value = setOf(7L)
        coEvery { followRepository.follow(any()) } just Runs
        coEvery { followRepository.unfollow(7L) } just Runs

        // When
        val vm = vm(userId = 7L)
        vm.state.subscribedIn(this)
        advanceUntilIdle()
        vm.toggleFollow()
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { followRepository.unfollow(7L) }
        coVerify(exactly = 0) { followRepository.follow(any()) }
    }

    @Test
    fun `missing id in SavedStateHandle fails fast`() {
        val outcome = runCatching {
            UserDetailViewModel(SavedStateHandle(), usersRepository, followRepository)
        }
        assert(outcome.exceptionOrNull() is IllegalArgumentException)
    }

    private fun vm(userId: Long): UserDetailViewModel =
        UserDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf("id" to userId)),
            usersRepository = usersRepository,
            followRepository = followRepository,
        )

    private fun user(id: Long, name: String) =
        User(
            id = id,
            displayName = name,
            reputation = 100,
            profileImageUrl = "http://img/$id",
            websiteUrl = null,
            location = null,
        )

    private fun <T> StateFlow<T>.subscribedIn(scope: TestScope): StateFlow<T> {
        scope.backgroundScope.launch { collect {} }
        return this
    }
}
