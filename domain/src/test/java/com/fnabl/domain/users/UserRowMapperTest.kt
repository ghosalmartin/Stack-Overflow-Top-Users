package com.fnabl.domain.users

import com.fnabl.domain.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UserRowMapperTest {
    @Test
    fun `toRow copies fields and sets isFollowed`() {
        // Given
        val user = user(id = 42L, name = "Alice", reputation = 1_000)

        // When
        val row = user.toRow(isFollowed = true)

        // Then
        assertEquals(42L, row.id)
        assertEquals("Alice", row.displayName)
        assertEquals(1_000, row.reputation)
        assertEquals("http://img/42", row.profileImageUrl)
        assertTrue(row.isFollowed)
    }

    @Test
    fun `toRows flags only the users whose ids appear in followed set`() {
        // Given
        val users = listOf(user(1L, "Alice"), user(2L, "Bob"), user(3L, "Carol"))
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

    @Test
    fun `selectUserRow returns the row for the given id with isFollowed propagated`() {
        // Given
        val users = listOf(user(1L, "Alice"), user(2L, "Bob"))

        // When
        val row = users.selectUserRow(id = 2L, isFollowed = true)

        // Then
        assertEquals("Bob", row?.displayName)
        assertEquals(true, row?.isFollowed)
    }

    @Test
    fun `selectUserRow returns null when no user matches the id`() {
        // Given
        val users = listOf(user(1L, "Alice"))

        // When
        val row = users.selectUserRow(id = 99L, isFollowed = false)

        // Then
        assertNull(row)
    }

    @Test
    fun `selectUserRow returns null on an empty list rather than throwing`() {
        // Given / When
        val row = emptyList<User>().selectUserRow(id = 1L, isFollowed = false)

        // Then
        assertNull(row)
    }

    private fun user(
        id: Long,
        name: String,
        reputation: Int = 100,
    ) = User(
        id = id,
        displayName = name,
        reputation = reputation,
        profileImageUrl = "http://img/$id",
        websiteUrl = null,
        location = null,
    )
}
