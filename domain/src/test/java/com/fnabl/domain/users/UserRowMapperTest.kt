package com.fnabl.domain.users

import com.fnabl.domain.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UserRowMapperTest {
    @Test
    fun `toRow copies fields and sets isFollowed`() {
        // Given
        val user =
            User(
                id = 42L,
                displayName = "Alice",
                reputation = 1_000,
                profileImageUrl = "http://img/alice.png",
            )

        // When
        val row = user.toRow(isFollowed = true)

        // Then
        assertEquals(42L, row.id)
        assertEquals("Alice", row.displayName)
        assertEquals(1_000, row.reputation)
        assertEquals("http://img/alice.png", row.profileImageUrl)
        assertTrue(row.isFollowed)
    }

    @Test
    fun `toRows flags only the users whose ids appear in followed set`() {
        // Given
        val users =
            listOf(
                User(id = 1L, displayName = "Alice", reputation = 100, profileImageUrl = "http://img/1"),
                User(id = 2L, displayName = "Bob", reputation = 200, profileImageUrl = "http://img/2"),
                User(id = 3L, displayName = "Carol", reputation = 300, profileImageUrl = "http://img/3"),
            )
        val followed = setOf(1L, 3L)

        // When
        val rows = users.toRows(followed)

        // Then
        assertEquals(listOf(true, false, true), rows.map { it.isFollowed })
    }

    @Test
    fun `toRows on empty list returns empty list`() {
        // Given / When
        val rows = emptyList<User>().toRows(setOf(1L))

        // Then
        assertTrue(rows.isEmpty())
    }
}
