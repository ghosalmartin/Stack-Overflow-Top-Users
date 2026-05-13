package com.fnabl.data.remote

import com.fnabl.data.remote.dto.UsersResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

internal interface StackOverflowApi {

    @GET("2.2/users")
    suspend fun getTopUsers(
        @Query("page") page: Int = 1,
        @Query("pagesize") pageSize: Int,
        @Query("sort") sort: String,
        @Query("order") order: String,
        @Query("site") site: String = "stackoverflow",
    ): UsersResponseDto
}
