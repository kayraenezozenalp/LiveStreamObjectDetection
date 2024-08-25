package com.io1.bitirmeprojesi.view.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.google.gson.Gson
import com.io1.bitirmeprojesi.R
import com.io1.bitirmeprojesi.data.service.SignIn
import com.io1.bitirmeprojesi.data.service.SignUP
import com.io1.bitirmeprojesi.databinding.FragmentRegisterBinding
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(inflater,container,false)
        val view = binding.root

        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) = Unit
            override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) = Unit
            override fun getAcceptedIssuers() = arrayOf<java.security.cert.X509Certificate>()
        })
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())
        val sslSocketFactory = sslContext.socketFactory

        val retrofit = Retrofit.Builder()
            .baseUrl("https://capstone.gokceonur.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                    .hostnameVerifier { _, _ -> true }
                    .build()
            )
            .build()

        val apiRegister = retrofit.create(SignUP::class.java)



        binding.button.setOnClickListener {
            val email = binding.email.text.toString()
            val pass = binding.passET.text.toString()
            val confirmPass = binding.confirmPassEt.text.toString()


            if (email.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty()) {
                if (pass == confirmPass) {
                    val registerRequest = UserCredentials(email, pass)
                    lifecycleScope.launch {
                        val response = apiRegister.signup(registerRequest)
                        if(response.isSuccessful){
                            System.out.println(response)
                            val action = RegisterFragmentDirections.actionRegisterFragmentToLogin()
                            Navigation.findNavController(view).navigate(action)
                        }else{
                            val errorBody = response.errorBody()?.string()
                            System.out.println(errorBody)
                        }
                    }



                } else {
                    Toast.makeText(requireContext(), "Password is not matching", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Empty Fields Are not Allowed !!", Toast.LENGTH_SHORT).show()

            }
        }

        binding.textView.setOnClickListener {
            val action = RegisterFragmentDirections.actionRegisterFragmentToLogin()
            Navigation.findNavController(view).navigate(action)
        }

        return view
    }

}