package com.voiceapp.project

sealed class Result<out T> {

    object InProgress : Result<Nothing>()

    data class Success<out T>(val success: T) : Result<T>()

    object Error : Result<Nothing>()
}