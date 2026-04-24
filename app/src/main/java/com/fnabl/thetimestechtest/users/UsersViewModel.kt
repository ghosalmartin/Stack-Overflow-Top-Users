package com.fnabl.thetimestechtest.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fnabl.domain.repository.FollowRepository
import com.fnabl.domain.repository.UsersRepository
import com.fnabl.domain.users.UsersIntent
import com.fnabl.domain.users.reduceUsersState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UsersViewModel @Inject constructor(
    private val usersRepository: UsersRepository,
    private val followRepository: FollowRepository,
) : ViewModel() {

    val state: StateFlow<UsersViewState> =
        combine(usersRepository.topUsers, followRepository.followedUserIds, ::reduceUsersState)
            .map { it.toViewState() }
            .onStart { refresh() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
                initialValue = UsersViewState.Loading,
            )

    fun onIntent(intent: UsersIntent) {
        when (intent) {
            UsersIntent.Refresh -> refresh()
            UsersIntent.LoadMore -> loadMore()
            is UsersIntent.ToggleFollow -> toggleFollow(intent.userId)
        }
    }

    private fun refresh() {
        viewModelScope.launch { usersRepository.refresh() }
    }

    private fun loadMore() {
        viewModelScope.launch { usersRepository.loadMore() }
    }

    private fun toggleFollow(userId: Long) {
        viewModelScope.launch {
            val followedIds = followRepository.followedUserIds.first()
            if (userId in followedIds) {
                followRepository.unfollow(userId)
            } else {
                followRepository.follow(userId)
            }
        }
    }

    private companion object {
        const val SUBSCRIPTION_TIMEOUT_MS = 5_000L
    }
}
