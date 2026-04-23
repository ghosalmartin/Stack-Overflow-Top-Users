package com.fnabl.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.fnabl.domain.repository.FollowRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

internal class DataStoreFollowRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : FollowRepository {

    override val followedUserIds: Flow<Set<Long>> =
        dataStore.data
            .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
            .map { prefs ->
                prefs[FOLLOWED_IDS_KEY]
                    ?.mapNotNull(String::toLongOrNull)
                    ?.toSet()
                    .orEmpty()
            }

    override suspend fun follow(userId: Long) {
        dataStore.edit { it[FOLLOWED_IDS_KEY] = it[FOLLOWED_IDS_KEY].orEmpty() + userId.toString() }
    }

    override suspend fun unfollow(userId: Long) {
        dataStore.edit { it[FOLLOWED_IDS_KEY] = it[FOLLOWED_IDS_KEY].orEmpty() - userId.toString() }
    }

    private companion object {
        val FOLLOWED_IDS_KEY = stringSetPreferencesKey("followed_user_ids")
    }
}
