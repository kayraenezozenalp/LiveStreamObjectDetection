package com.io1.bitirmeprojesi.data.repository

import com.io1.bitirmeprojesi.data.model.AuthResponseModel
import com.io1.bitirmeprojesi.data.model.ResultWrapper
import com.io1.bitirmeprojesi.view.auth.UserCredentials

interface AuthenticationRepository {

    suspend fun  loginWithCredentials(
        securityModel : UserCredentials
    ): ResultWrapper<AuthResponseModel>
}