package com.io1.bitirmeprojesi.view.photo

import android.content.ContentValues
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.YuvImage
import android.media.Image
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.Surface
import android.view.Surface.ROTATION_0
import android.view.Surface.ROTATION_180
import android.view.Surface.ROTATION_270
import android.view.Surface.ROTATION_90
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.io1.bitirmeprojesi.ShowImageFragment
import com.io1.bitirmeprojesi.data.service.PostImage
import com.io1.bitirmeprojesi.databinding.FragmentPhotoBinding
import com.io1.bitirmeprojesi.view.auth.token
import com.io1.bitirmeprojesi.view.camera.CameraFragment
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.WebSocket
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URI
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.experimental.and


class PhotoFragment : Fragment() {
    private lateinit var binding: FragmentPhotoBinding

    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null

    private lateinit var webSocket: WebSocket
    val WEB_SOCKET_URL = "ws://capstone.gokceonur.com"
    private lateinit var webSocketClient: WebSocketClient
    var apiResponseUrl = "https://capstone.gokceonur.com/predicted-images/"
    private var lensFacing = CameraSelector.LENS_FACING_FRONT



    var name : String = ""
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    var request : RequestBody? = null

    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    private fun initWebSocket() {
        val webSocketUrl: URI? = URI(WEB_SOCKET_URL)
        createWebSocketClient(webSocketUrl)
        //Eğer SSL sertifikalı bir websocket dinliyorsak
        //SSl ayarlamasını yapıyoruz.
        //val socketFactory: SSLSocketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory
        //webSocketClient.setSocketFactory(socketFactory)
        webSocketClient.connect()
    }

    private fun createWebSocketClient(webSocketUrl: URI?) {
        webSocketClient = object : WebSocketClient(webSocketUrl) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                System.out.println("açık")
            }

            override fun onMessage(message: String?) {
                System.out.println("onMessage" + message)
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
            }

            override fun onError(ex: Exception?) {
                System.out.println("error")
            }
        }
    }

    private fun sendMessage(data: ByteArray) {
        try {
            if (::webSocketClient.isInitialized && webSocketClient.isOpen) {
                System.out.println("Sending image data to WebSocket.")
                webSocketClient.send(data)
            } else {

            }
        } catch (e: Exception) {

        }
    }

    override fun onResume() {
        super.onResume()
        initWebSocket()
    }

    override fun onPause() {
        super.onPause()
        webSocketClient.close()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPhotoBinding.inflate(layoutInflater, container, false)
        val view = binding.root
        System.out.println("izin istemedik daha")
        if(allPermissionsGranted()){
            System.out.println("asdjlfas")
            startCamera()
        }else{
            requestPermissions()
            System.out.println("izin istiyom")
        }

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

        binding.imageCaptureButton.setOnClickListener {
            takePhoto()
        }

        binding.cameraReverse.setOnClickListener {
                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                    CameraSelector.LENS_FACING_FRONT
                } else {
                    CameraSelector.LENS_FACING_BACK
                }
                startCamera()
        }

        binding.videoCaptureButton.setOnClickListener {
            val apiPost = retrofit.create(PostImage::class.java)
            lifecycleScope.launch {

                System.out.println(requestBody)
                System.out.println(token)
               val response = requestBody?.let { it1 -> apiPost.postimage("Bearer $token", it1) }
                if (response != null) {
                    if(response.isSuccessful){
                        response.body()?.apply {
                            imageURL = apiResponseUrl + this.filename
                            val bottomSheetFragment = ShowImageFragment()
                            bottomSheetFragment.show(this@PhotoFragment.parentFragmentManager,bottomSheetFragment.tag)
                            System.out.println("apiresponseurl " + apiResponseUrl)
                           /* Picasso.with(requireContext())
                                .load(apiResponseUrl)
                                .resize(widht,height)
                                .into(binding.imageView2)

                            */
                        }
                    }
                }
                System.out.println(response)
            }
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME,name)
            put(MediaStore.MediaColumns.MIME_TYPE,"image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        val contentResolver = requireActivity().contentResolver
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    System.out.println(exc)
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults){

                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    image = output.savedUri
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                    val realPath = output.savedUri?.let { getRealPathFromURI(it) }
                    val file = File(realPath)
                    val arr = File(realPath).inputStream().use { it.readBytes() }
                    System.out.println("arrrrrr")
                    System.out.println(arr)
                  //  sendMessage(arr)
                    System.out.println("fileee")
                    System.out.println(file)
                    System.out.println("savedUri")
                    System.out.println(output.savedUri)
                    requestBody = createRequestBody(file,"image.jpg")
                    convertBinary()
                }
            }
        )
    }

    private fun captureVideo() {}

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()

            val imageAnalyzer = ImageAnalysis.Builder()
                .setTargetResolution(Size(1125, 2005))
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, PhotoFragment.LuminosityAnalyzer { luma ->

                    })
                }

            //val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(this,cameraSelector,preview, imageCapture,imageAnalyzer)
            }catch (exc : Exception) {
                Log.e(TAG,"Use case binding failed",exc)
            }
        },ContextCompat.getMainExecutor(requireContext()))
    }

    private fun getRealPathFromURI(contentUri: Uri): String? {
        var cursor: Cursor? = null
        return try {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            cursor = requireActivity().contentResolver.query(contentUri, projection, null, null, null)
            val columnIndex = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor?.moveToFirst()
            System.out.println("columIndex")
            System.out.println(columnIndex)
            columnIndex?.let { cursor?.getString(it) }
        } finally {
            cursor?.close()

        }
    }
    private fun createRequestBody(file: File, name: String): RequestBody {

        return MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", name, file.asRequestBody("image/*".toMediaTypeOrNull()))
            .build()
    }
    private fun createRequest(byteArray: ByteArray, name: String): RequestBody {
        System.out.println(byteArray)
        val requestBody = byteArray.toRequestBody("image/*".toMediaTypeOrNull())
        // Dosya adını belirle
        val name = "image.jpg" // İstediğiniz bir dosya adı verebilirsiniz

        // MultipartBody.Part oluştur
        return  MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image",name,requestBody)
            .build()
    }

     class LuminosityAnalyzer(private val listener: LumaListener) : ImageAnalysis.Analyzer {

        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()    // Rewind the buffer to zero
            val data = ByteArray(remaining())
            System.out.println(data)
            get(data)   // Copy the buffer into a byte array
            return data // Return the byte array
        }

        @ExperimentalGetImage
        override fun analyze(imageProxy: ImageProxy) {
            bitmap = imageProxy.image?.toBitmap()
            val rotatedBitmap =
                bitmap?.let { rotateBitmap(it, 270f) }
            System.out.println("rotated" +rotatedBitmap?.width)
            System.out.println("rotated height" + rotatedBitmap?.height)// 90 derece saat yönünde döndürme
            bitmapBox = bitmap?.drawBox("person",30f,Color.RED,100,100,200,200)
            byteArray = bitmap?.toByteArray()
            val buffer = imageProxy.planes[0].buffer
            val data = buffer.toByteArray()
            val pixels = data.map { it.toInt() and 0xFF }
            val luma = pixels.average()

            listener(luma)
            imageProxy.close()
        }

        fun Bitmap.drawBox(text: String,textSizee: Float, textColor: Int,left: Int, top: Int, right: Int, bottom: Int): Bitmap {
            val mutableBitmap = this.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(mutableBitmap)
            val paint = Paint().apply {
                color = Color.GREEN // Kutu rengi (Yeşil)
                style = Paint.Style.STROKE
                strokeWidth = 4f
            }

            val rect = Rect(left, top, right, bottom) // Boyutları scale kadar büyüt
            canvas.drawRect(rect, paint)
            val textPaint = Paint().apply {
                color = Color.RED // Metin rengi
                textSize = 20F // Metin boyutu
                isAntiAlias = true // Düzgünlüğü sağlar
                typeface = Typeface.DEFAULT_BOLD // Metin tipi
            }
            val textX = (left + right) / 2f - textPaint.measureText(text) / 2f
            val textY = (top + bottom) / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
            canvas.drawText(text, textX, textY, textPaint)
            return mutableBitmap
        }

        fun Bitmap.toByteArray(): ByteArray {
            val outputStream = ByteArrayOutputStream()
            this.compress(Bitmap.CompressFormat.PNG, 100, outputStream) // PNG formatında sıkıştır
            return outputStream.toByteArray()
        }

        fun Image.toBitmap(): Bitmap {
            val yBuffer = planes[0].buffer // Y
            val vuBuffer = planes[2].buffer // VU

            val ySize = yBuffer.remaining()
            val vuSize = vuBuffer.remaining()

            val nv21 = ByteArray(ySize + vuSize)

            yBuffer.get(nv21, 0, ySize)
            vuBuffer.get(nv21, ySize, vuSize)

            val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
            val out = ByteArrayOutputStream()
            yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 50, out)
            val imageBytes = out.toByteArray()
            return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        }
        fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
            val matrix = Matrix()
            matrix.postRotate(angle)
            return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
        }
    }

    private fun convertBinary(){
        imageAnalyzer?.setAnalyzer(cameraExecutor,LuminosityAnalyzer{luma ->
            Log.d(TAG, "Average luminosity: $luma")
            imageAnalyzer!!.clearAnalyzer()
        })

    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(),it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CAMERAX"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.RECORD_AUDIO
            ).apply {
                if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.P){
                    add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && it.value == false)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(requireContext(),
                    "Permission request denied",
                    Toast.LENGTH_SHORT).show()
            } else {
                startCamera()
            }
        }



}
typealias LumaListener = (luma: Double) -> Unit
private var image : Uri ?= null
var byteArray : ByteArray? = null
var bitmap : Bitmap? = null
var requestBody : RequestBody ?= null
var responseURL = ""
var imageURL = ""
var mediaImage: Image? = null
var bitmapBox : Bitmap? = null