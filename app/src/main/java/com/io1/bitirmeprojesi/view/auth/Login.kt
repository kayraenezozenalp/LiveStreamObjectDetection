package com.io1.bitirmeprojesi.view.auth

import android.net.http.SslError
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.SslErrorHandler
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.google.gson.Gson
import com.io1.bitirmeprojesi.data.service.SignIn
import com.io1.bitirmeprojesi.data.service.SignUP
import com.io1.bitirmeprojesi.databinding.FragmentLoginBinding
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

data class UserCredentials(val email: String, val password: String)

class Login : Fragment() {

    private lateinit var ViewModel : RegisterViewModel

    private lateinit var binding : FragmentLoginBinding
    var email = ""
    var password = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //ViewModel = ViewModelProvider(this).get(RegisterViewModel::class.java)
        binding = FragmentLoginBinding.inflate(inflater,container,false)
        val view = binding.root



        binding.email.addTextChangedListener {
            email = it.toString()
        }
        binding.passET.addTextChangedListener {
            password = it.toString()
        }

        val userCredentials = UserCredentials(email, password)
        val gson = Gson()
        val json = gson.toJson(userCredentials)
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

        val apiService = retrofit.create(SignUP::class.java)
        val apiLogin = retrofit.create(SignIn::class.java)


        val url = "https://api.capstone.onurgokce.com/signup"
        val loginurl = "https://api.capstone.onurgokce.com/login"
        val googleurl = "https://capstone.gokceonur.com/auth/google"
        val facebookurl = "https://capstone.gokceonur.com/auth/facebook"
        System.out.println("dagad")
      /*  binding.register.setOnClickListener {
            lifecycleScope.launch {
                    val loginRequest = UserCredentials(email, password)
                    val response = apiService.signup(loginRequest)
                    System.out.println("response")
                    if (response.isSuccessful) {
                        System.out.println(response)
                        val responseBody = response.body()?.toString()
                        responseBody.let {
                            System.out.println(token)
                        }
                    } else {

                        System.out.println(response)
                        val errorBody = response.errorBody()?.string()
                        System.out.println(errorBody)
                    }
            }
        }

       */

        binding.register.setOnClickListener {
            val action = LoginDirections.actionLoginToRegisterFragment()
            Navigation.findNavController(view).navigate(action)
        }


        binding.login.setOnClickListener {
            lifecycleScope.launch {
                val loginRequest = UserCredentials(email, password)
                val response = apiLogin.signin(loginRequest)
                if (response.isSuccessful) {
                    System.out.println("denemeeeee")
                    val accessToken = response.body()?.accessToken
                    System.out.println(accessToken)
                    token = accessToken
                    val action = LoginDirections.actionLoginToHistoryFragment()
                    Navigation.findNavController(view).navigate(action)

                } else {
                    System.out.println(response)
                    val errorBody = response.errorBody()?.string()
                    System.out.println(errorBody)
                }


            }
        }
        binding.imageButton4.setOnClickListener {
            binding.login.visibility = View.GONE
            binding.authView.settings.userAgentString = System.getProperty("http.agent")
            binding.authView.settings.javaScriptEnabled = true
            binding.authView.settings.cacheMode = WebSettings.LOAD_DEFAULT
            binding.authView.visibility = View.VISIBLE
            binding.authView.loadUrl(googleurl)
            binding.authView.webViewClient = object : WebViewClient() {
                override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                    // SSL hatasını göz ardı et
                    handler?.proceed()
                }
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)

                    // Sayfa tamamlandığında kontrol ediyoruz
                    if (url != null && url.startsWith("https://capstone.gokceonur.com/auth/google/callback")) {
                        // Eğer bu URL'e yönlendirilmişsek, kullanıcı OAuth izni vermiştir.
                        // Burada accessToken'ı alabiliriz, örneğin JavaScript kullanarak
                        view?.evaluateJavascript(
                            "(function() { return document.documentElement.innerText; })();"
                        ) { result ->
                            token = result.substringAfter(":\\\"").substringBefore("\\\"}")
                            System.out.println(result.length)
                            System.out.println("AccessToken: $token")
                            // WebView'i kapat
                            view?.loadUrl("about:blank")
                            binding.authView.visibility = View.GONE
                            val action = LoginDirections.actionLoginToHistoryFragment()
                            Navigation.findNavController(view).navigate(action)
                        }
                    }
                }

            }

        }

        binding.imageButton5.setOnClickListener {
            binding.login.visibility = View.GONE
            binding.authView.settings.apply {
                userAgentString = System.getProperty("http.agent")
                javaScriptEnabled = true
                domStorageEnabled = true
                cacheMode = WebSettings.LOAD_DEFAULT
            }
            binding.authView.visibility = View.VISIBLE
            binding.authView.loadUrl(facebookurl)
            binding.authView.webViewClient = object : WebViewClient() {
                override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                    // SSL hatasını göz ardı et
                    handler?.proceed()
                }
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)

                    // Sayfa tamamlandığında kontrol ediyoruz
                    if (url != null && url.startsWith("https://capstone.gokceonur.com/auth/facebook/callback")) {
                        // Eğer bu URL'e yönlendirilmişsek, kullanıcı OAuth izni vermiştir.
                        // Burada accessToken'ı alabiliriz, örneğin JavaScript kullanarak
                        view?.evaluateJavascript(
                            "(function() { return document.documentElement.innerText; })();"
                        ) { result ->
                            // AccessToken'ı aldık, bunu kullanabiliriz
                            // Örneğin, bu veriyi bir API isteğiyle sunucuya gönderebiliriz
                            // ya da başka bir işlem yapabiliriz.
                            token = result.substringAfter(":\\\"").substringBefore("\\\"}")
                            System.out.println(result.length)
                            System.out.println("AccessToken: $token")
                            // WebView'i kapat
                            view?.loadUrl("about:blank")
                            binding.authView.visibility = View.GONE
                        }
                    }
                }
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
    }

    private fun initView() {


    }
}
public var token : String? = ""