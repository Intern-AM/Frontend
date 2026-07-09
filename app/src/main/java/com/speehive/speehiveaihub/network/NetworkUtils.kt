package com.speehive.speehiveaihub.network

import retrofit2.HttpException
import retrofit2.Response

suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
    return try {
        Result.success(apiCall())
    } catch (e: HttpException) {
        val errorBody = e.response()?.errorBody()?.string()
        Result.failure(AuthError.fromCode(e.code(), errorBody))
    } catch (e: Exception) {
        Result.failure(e)
    }
}

inline fun <reified T> Response<T>.toResult(): Result<T> {
    return if (isSuccessful) {
        val body = body()
        if (body != null) {
            Result.success(body)
        } else if (Unit is T) {
            @Suppress("UNCHECKED_CAST")
            Result.success(Unit as T)
        } else {
            Result.failure(Exception("Response body was null"))
        }
    } else {
        val errorBody = errorBody()?.string()
        Result.failure(AuthError.fromCode(code(), errorBody))
    }
}
