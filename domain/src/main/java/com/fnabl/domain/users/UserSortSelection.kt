package com.fnabl.domain.users

enum class UserSort {
    REPUTATION,
    NAME,
    CREATION,
    MODIFIED,
}

enum class SortOrder {
    ASC,
    DESC,
}

data class UserSortSelection(
    val sort: UserSort,
    val order: SortOrder,
) {
    companion object {
        val Default = UserSortSelection(sort = UserSort.REPUTATION, order = SortOrder.DESC)
    }
}
