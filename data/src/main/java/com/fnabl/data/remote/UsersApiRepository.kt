package com.fnabl.data.remote

import com.fnabl.data.remote.dto.toDomain
import com.fnabl.domain.repository.TopUsersState
import com.fnabl.domain.repository.UsersRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

internal class UsersApiRepository
    @Inject
    constructor(
        private val api: StackOverflowApi,
    ) : UsersRepository {
        private val cache = MutableStateFlow<TopUsersState>(TopUsersState.Loading)
        override val topUsers: StateFlow<TopUsersState> = cache.asStateFlow()

        override suspend fun refresh() {
            cache.value =
                when (val current = cache.value) {
                    is TopUsersState.Loaded -> current.copy(isRefreshing = true)
                    else -> TopUsersState.Loading
                }
            cache.value =
                try {
                    val response = api.getTopUsers(page = FIRST_PAGE, pageSize = PAGE_SIZE)
                    TopUsersState.Loaded(
                        users = response.items.toDomain(),
                        nextPage = (FIRST_PAGE + 1).takeIf { response.hasMore },
                    )
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (throwable: Throwable) {
                    TopUsersState.Failed(throwable)
                }
        }

        @Suppress("ReturnCount")
        override suspend fun loadMore() {
            val current = cache.value as? TopUsersState.Loaded ?: return
            val next = current.nextPage ?: return
            if (current.isAppending) return

            cache.value = current.copy(isAppending = true)
            cache.value =
                try {
                    val response = api.getTopUsers(page = next, pageSize = PAGE_SIZE)
                    current.copy(
                        users = current.users + response.items.toDomain(),
                        nextPage = (next + 1).takeIf { response.hasMore },
                        isAppending = false,
                    )
                } catch (cancellation: CancellationException) {
                    cache.value = current.copy(isAppending = false)
                    throw cancellation
                } catch (throwable: Throwable) {
                    current.copy(isAppending = false)
                }
        }

        private companion object {
            const val FIRST_PAGE = 1
            const val PAGE_SIZE = 20
        }
    }
