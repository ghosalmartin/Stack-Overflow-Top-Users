package com.fnabl.thetimestechtest.users

import com.fnabl.domain.users.UserRow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class UserRowUiModelTest {

    init {
        Locale.setDefault(Locale.UK)
    }

    @Test
    fun `maps every field onto the ui model`() {
        // Given
        val row = UserRow(
            id = 42L,
            displayName = "Alice Liddell",
            reputation = 1_234_567,
            profileImageUrl = "http://img/alice.png",
            isFollowed = true,
        )

        // When
        val ui = row.toUiModel()

        // Then
        assertEquals(42L, ui.id)
        assertEquals("Alice Liddell", ui.displayName)
        assertEquals("1,234,567", ui.displayReputation)
        assertEquals("http://img/alice.png", ui.profileImageUrl)
        assertTrue(ui.isFollowed)
    }
}
