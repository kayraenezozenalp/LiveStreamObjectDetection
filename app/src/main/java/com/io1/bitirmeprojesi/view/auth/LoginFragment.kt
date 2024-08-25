package com.io1.bitirmeprojesi.view.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import com.io1.bitirmeprojesi.R
import com.io1.bitirmeprojesi.databinding.FragmentLogin2Binding
import com.io1.bitirmeprojesi.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {
    private lateinit var binding : FragmentLogin2Binding
    val url = "https://api.capstone.onurgokce.com"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLogin2Binding.inflate(inflater,container,false)
        val view = binding.root


        val webSettings: WebSettings = binding.web.settings
        webSettings.javaScriptEnabled = true // JavaScript'i etkinleştir
        webSettings.allowContentAccess = true // İçerik erişimine izin ver
        webSettings.allowFileAccess = true // Dosya erişimine izin ver
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        binding.web.loadUrl(url)
        return view
    }


}