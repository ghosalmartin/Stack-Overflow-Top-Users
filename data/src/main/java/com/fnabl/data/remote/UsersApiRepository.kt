package com.fnabl.data.remote

import com.fnabl.data.remote.dto.toDomain
import com.fnabl.domain.repository.TopUsersState
import com.fnabl.domain.repository.UsersRepository
import com.fnabl.domain.users.SortOrder
import com.fnabl.domain.users.UserSort
import com.fnabl.domain.users.UserSortSelection
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

        private val sortSelection = MutableStateFlow(UserSortSelection.Default)
        override val currentSort: StateFlow<UserSortSelection> = sortSelection.asStateFlow()

        override suspend fun refresh() {
            cache.value =
                when (val current = cache.value) {
                    is TopUsersState.Loaded -> current.copy(isRefreshing = true)
                    else -> TopUsersState.Loading
                }
            cache.value = fetchPage(page = FIRST_PAGE)
        }

        @Suppress("ReturnCount")
        override suspend fun loadMore() {
            val current = cache.value as? TopUsersState.Loaded ?: return
            val next = current.nextPage ?: return
            if (current.isAppending) return

            cache.value = current.copy(isAppending = true)
            cache.value =
                try {
                    val response =
                        api.getTopUsers(
                            page = next,
                            pageSize = PAGE_SIZE,
                            sort = sortSelection.value.sort.toQuery(),
                            order = sortSelection.value.order.toQuery(),
                        )
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

        override suspend fun setSort(selection: UserSortSelection) {
            if (sortSelection.value == selection) return
            sortSelection.value = selection
            cache.value = TopUsersState.Loading
            cache.value = fetchPage(page = FIRST_PAGE)
        }

        private suspend fun fetchPage(page: Int): TopUsersState =
            try {
                val response =
                    api.getTopUsers(
                        page = page,
                        pageSize = PAGE_SIZE,
                        sort = sortSelection.value.sort.toQuery(),
                        order = sortSelection.value.order.toQuery(),
                    )
                TopUsersState.Loaded(
                    users = response.items.toDomain(),
                    nextPage = (page + 1).takeIf { response.hasMore },
                )
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (throwable: Throwable) {
                TopUsersState.Failed(throwable)
            }

        private companion object {
            const val FIRST_PAGE = 1
            const val PAGE_SIZE = 20
        }
    }

private fun UserSort.toQuery(): String =
    when (this) {
        UserSort.REPUTATION -> "reputation"
        UserSort.NAME -> "name"
        UserSort.CREATION -> "creation"
        UserSort.MODIFIED -> "modified"
    }

private fun SortOrder.toQuery(): String =
    when (this) {
        SortOrder.ASC -> "asc"
        SortOrder.DESC -> "desc"
    }
