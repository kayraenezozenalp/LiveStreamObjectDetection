package com.io1.bitirmeprojesi.view.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.io1.bitirmeprojesi.data.model.Resource
import com.io1.bitirmeprojesi.data.repository.AuthenticationRepository
import com.io1.bitirmeprojesi.data.service.SignUP
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RegisterViewModel constructor(
    private val authenticationRepository: AuthenticationRepository
): ViewModel() {

    var showError = MutableLiveData<String?>()

    private val _securityRegister = MutableLiveData<Resource<String>>()
    val securityRegister: LiveData<Resource<String>>
        get() = _securityRegister
    val retrofit = Retrofit.Builder()
        .baseUrl("https://example.com/api/") // API'nin temel URL'si
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService = retrofit.create(SignUP::class.java)

    fun makeLoginRequest(email: String, password: String) {
        viewModelScope.launch {
            try {
                // LoginRequest objesi oluşturulması
                val loginRequest = UserCredentials(email, password)
                // Retrofit ile post isteği gönderilmesi
                val response = apiService.signup(loginRequest)
                // Cevabın başarılı olup olmadığının kontrolü
                if (response.isSuccessful) {
                    // Cevaptan tokenin alınması
                    System.out.println(response)
                    val token = response.body()?.accessToken
                    // Tokenin _securityRegister LiveData'sına eklenmesi
                    _securityRegister.postValue(Resource.success(token))
                } else {
                    System.out.println(response)
                    // Başarısız cevap durumunda hata mesajının alınması
                    val errorBody = response.errorBody()?.string()
                    System.out.println(errorBody)
                    // Hata mesajının _securityRegister LiveData'sına eklenmesi
                    _securityRegister.postValue(Resource.error("Error: $errorBody"))
                }
            } catch (e: Exception) {
                // İstek sırasında bir hata oluştuğunda hatanın _securityRegister LiveData'sına eklenmesi
                _securityRegister.postValue(Resource.error("Error: ${e.message}"))
            }
        }
    }
}