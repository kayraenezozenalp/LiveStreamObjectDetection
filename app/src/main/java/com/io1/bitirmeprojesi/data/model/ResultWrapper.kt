package com.io1.bitirmeprojesi.data.model

import com.io1.bitirmeprojesi.data.IException

sealed class ResultWrapper<out T> {
    data class Success<out T>(val value: T) : ResultWrapper<T>()
    data class Fail(val value: IException) : ResultWrapper<Nothing>()
}