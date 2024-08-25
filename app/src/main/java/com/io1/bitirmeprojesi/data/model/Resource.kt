package com.io1.bitirmeprojesi.data.model

data class Resource<out T>(
    val status: Status,
    val data: T?,
    val message: String?
){
    enum class Status {
        SUCCESS,
        ERROR,
        LOADING
    }

    companion object{
        fun <T> success(data : T?): Resource<T> {
            return Resource(Status.SUCCESS, data, null)
        }
        fun <T> error(data : T?): Resource<T> {
            return Resource(Status.ERROR, data, null)
        }
        fun <T> loading(data : T?): Resource<T> {
            return Resource(Status.LOADING, data, null)
        }

    }
}