package com.io1.bitirmeprojesi.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.io1.bitirmeprojesi.data.IException
import com.io1.bitirmeprojesi.data.model.ResultWrapper
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel(){

    fun <T> makeNetworkRequest(
        requestFunc : suspend () -> ResultWrapper<T?>,
        onSuccess : ((value : T?) -> Unit)? = null,
        onFail : ((value: IException) -> Unit)? = null
    ){
        viewModelScope.launch {
            when (val response = requestFunc.invoke()) {
                is ResultWrapper.Fail ->{
                    onFail?.invoke(response.value)
                }
                is ResultWrapper.Success ->{
                    onSuccess?.invoke(response.value)
                }

                else -> {
                    System.out.println("something went wrong")
                }
            }
        }
    }
}