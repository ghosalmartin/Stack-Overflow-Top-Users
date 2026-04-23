package com.fnabl.data.di

import com.fnabl.data.local.DataStoreFollowRepository
import com.fnabl.data.remote.UsersApiRepository
import com.fnabl.domain.repository.FollowRepository
import com.fnabl.domain.repository.UsersRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface RepositoryModule {

    @Binds
    @Singleton
    fun bindUsersRepository(apiRepository: UsersApiRepository): UsersRepository

    @Binds
    @Singleton
    fun bindFollowRepository(dataStoreRepository: DataStoreFollowRepository): FollowRepository
}
