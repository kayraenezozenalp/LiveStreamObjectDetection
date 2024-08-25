package com.io1.bitirmeprojesi.view.camera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.io1.bitirmeprojesi.R
import com.io1.bitirmeprojesi.databinding.FragmentLiveWebBinding


class LiveWebFragment : Fragment() {

    private lateinit var binding : FragmentLiveWebBinding
    private val REQUEST_CAMERA_PERMISSION = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        askForPermissions()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

       binding = FragmentLiveWebBinding.inflate(layoutInflater,container,false)
       val view = binding.root

        binding.liveWeb.settings.javaScriptEnabled = true
        binding.liveWeb.settings.domStorageEnabled = true
        binding.liveWeb.settings.mediaPlaybackRequiresUserGesture = false

        binding.liveWeb.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) {
                request.grant(request.resources)
            }
        }

       binding.liveWeb.loadUrl("https://capstone.gokceonur.com/real-time-object-detection")


       return view
    }

    private fun askForPermissions() {
        activity?.let {
            if (ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(it, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                    it,
                    arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
                    REQUEST_CAMERA_PERMISSION
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission is granted. Continue the action or workflow in your app.
            } else {
                // Permission is denied. Show a message to the user.
            }
        }
    }



}