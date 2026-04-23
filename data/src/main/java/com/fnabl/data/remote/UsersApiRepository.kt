package com.fnabl.data.remote

import com.fnabl.data.remote.dto.toDomain
import com.fnabl.domain.repository.TopUsersState
import com.fnabl.domain.repository.UsersRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

internal class UsersApiRepository @Inject constructor(
    private val api: StackOverflowApi,
) : UsersRepository {

    private val cache = MutableStateFlow<TopUsersState>(TopUsersState.Loading)
    override val topUsers: StateFlow<TopUsersState> = cache.asStateFlow()

    override suspend fun refresh() {
        cache.value = when (val current = cache.value) {
            is TopUsersState.Loaded -> current.copy(isRefreshing = true)
            else -> TopUsersState.Loading
        }
        cache.value = try {
            TopUsersState.Loaded(api.getTopUsers(pageSize = PAGE_SIZE).items.toDomain())
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            TopUsersState.Failed(throwable)
        }
    }

    private companion object {
        const val PAGE_SIZE = 20
    }
}
