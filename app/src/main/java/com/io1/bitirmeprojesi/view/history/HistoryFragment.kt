package com.io1.bitirmeprojesi.view.history

import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.io1.bitirmeprojesi.HistoryAdapter
import com.io1.bitirmeprojesi.R
import com.io1.bitirmeprojesi.ShowImageFragment
import com.io1.bitirmeprojesi.data.model.HistoryModelItem
import com.io1.bitirmeprojesi.data.service.History
import com.io1.bitirmeprojesi.data.service.SignIn
import com.io1.bitirmeprojesi.data.service.SignUP
import com.io1.bitirmeprojesi.databinding.FragmentHistoryBinding
import com.io1.bitirmeprojesi.databinding.FragmentLoginBinding
import com.io1.bitirmeprojesi.view.auth.token
import com.io1.bitirmeprojesi.view.photo.responseURL
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class HistoryFragment : Fragment() {

    private lateinit var binding : FragmentHistoryBinding
    private lateinit var newRecyclerView: RecyclerView

    val imageList = ArrayList<HistoryModelItem>()
    var imagePath = ""
    var createDate = ""
    var historyId = 0
    var user_id = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding= FragmentHistoryBinding.inflate(inflater,container,false)
        val view = binding.root

        newRecyclerView = binding.recyclerView
        newRecyclerView.layoutManager = LinearLayoutManager(this.context)
        newRecyclerView.setHasFixedSize(true)
        val baseUrL = "https://capstone.gokceonur.com/predicted-images/"
        val url = baseUrL + responseURL

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

        val apiHistory = retrofit.create(History::class.java)


        lifecycleScope.launch {
            val response = apiHistory.gethistory("Bearer $token")

            if (response.isSuccessful){
                response.body()?.forEach {
                  // System.out.println(imagePath)

                   imagePath = baseUrL + it.image_path
                   historyId = it.id
                   user_id = it.user_id
                   createDate = it.create_date

                   val zonedDateTime = ZonedDateTime.parse(createDate)
                   val localDateTime = zonedDateTime.toLocalDateTime()
                   val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss")
                   val formattedDate = localDateTime.format(formatter)

                   imageList.add(HistoryModelItem(formattedDate,historyId,imagePath,user_id))
                }
                System.out.println(imageList)
                getUserData(imageList)

                System.out.println("başardık")
                System.out.println(response)
            }else{
                System.out.println("error body")
                System.out.println(response.errorBody())
            }
        }
        return view
    }

    fun getUserData(historyList : ArrayList<HistoryModelItem>) {
        newRecyclerView.adapter = HistoryAdapter(historyList)
        newRecyclerView.adapter?.notifyDataSetChanged()
    }



}