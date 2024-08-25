package com.io1.bitirmeprojesi.view.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.graphics.Typeface
import android.graphics.YuvImage
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.params.StreamConfigurationMap
import android.media.Image
import android.media.MediaRecorder
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.util.Size
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Surface
import android.view.Surface.ROTATION_0
import android.view.Surface.ROTATION_270
import android.view.Surface.ROTATION_90
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.io1.bitirmeprojesi.R
import com.io1.bitirmeprojesi.databinding.FragmentCameraBinding
import com.io1.bitirmeprojesi.view.photo.PhotoFragment
import com.io1.bitirmeprojesi.view.photo.bitmap
import com.io1.bitirmeprojesi.view.photo.bitmapBox
import com.io1.bitirmeprojesi.view.photo.byteArray
import com.io1.bitirmeprojesi.view.photo.mediaImage
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.URI
import java.nio.ByteBuffer
import java.util.Arrays
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CameraFragment : Fragment() {

    private lateinit var binding : FragmentCameraBinding
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null
   // private var lensFacing = CameraSelector.LENS_FACING_FRONT
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        initWebSocket()


        binding = FragmentCameraBinding.inflate(inflater,container,false)
        val view = binding.root
        startCamera()

     /*   binding.imageeee.setOnClickListener {

            byteArray?.let { sendMessage(it) }
        }

      */

     /*   val overlayView = binding.overlayView


        val dummyBoxes = listOf(
            Pair("Test1", RectF(100f, 100f, 200f, 200f)),
            Pair("Test2", RectF(300f, 300f, 400f, 400f))
        )
        overlayView.setBoxes(dummyBoxes)

      */

        binding.cameraReverse.setOnClickListener {
            binding.cameraReverse.setOnClickListener {
                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                    CameraSelector.LENS_FACING_FRONT
                } else {
                    CameraSelector.LENS_FACING_BACK
                }
                startCamera()
            }
        }

        binding.videoCaptureButton.setOnClickListener {
            if(isFirst) {
                byteArray?.let { sendMessage(it) }
            }
            isFirst = false
        }
        cameraExecutor = Executors.newSingleThreadExecutor()

        return view
    }



    private fun initWebSocket() {
        val webSocketUrl: URI? = URI(WEB_SOCKET_URL)
        createWebSocketClient(webSocketUrl)
        webSocketClient.connect()
    }
    fun adjustRectForResolution(rect: RectF, originalWidth: Float, originalHeight: Float, newWidth: Float, newHeight: Float): RectF {
        val scaleX = newWidth / originalWidth
        val scaleY = newHeight / originalHeight

        val adjustedRect = if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
            RectF(
                originalWidth - rect.right,
                rect.top,
                originalWidth - rect.left,
                rect.bottom
            )
        } else {
            rect
        }

        return RectF(
            adjustedRect.left * scaleX,
            adjustedRect.top * scaleY,
            adjustedRect.right * scaleX,
            adjustedRect.bottom * scaleY
        )

    }

    private fun createWebSocketClient(webSocketUrl: URI?) {
        webSocketClient = object : WebSocketClient(webSocketUrl) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.d(TAG, "onOpen")
            }

            override fun onMessage(message: String?) {
                Log.d(TAG, "onMessage: $message")
                var x1 : Double
                var y1 : Double
                var x2 : Double
                var y2 : Double
                var newx2 : Double
                var newy2 : Double
                var responseName : String
                var confidence : String
                System.out.println("onMessage" + message)
                val jsonArray = JSONArray(message)
                byteArray?.let { sendMessage(it) }
                val boxes = mutableListOf<Triple<String, RectF, String>>()

                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val boxObject = jsonObject.getJSONObject("box")
                    responseName = jsonObject.getString("name")
                    confidence = jsonObject.getDouble("confidence").toString()
                    System.out.println("confidence" + confidence)
                    x1 = boxObject.getDouble("x1")
                    y1 = boxObject.getDouble("y1")
                    x2 = boxObject.getDouble("x2")
                    y2 = boxObject.getDouble("y2")
                    System.out.println("x1 " + x1)
                    System.out.println("x2 " + x2)
                    System.out.println("y1 " + y1)
                    System.out.println("y2 " + y2)

                    newx2 = x2-x1
                    newy2 = y2-y1

                    //x1 , y1,x2,y290.79407
                    val rect = RectF( x1.toFloat(), y1.toFloat(), x2.toFloat() , y2.toFloat())
                    System.out.println(rect)
                    val originalWidth = 1080f
                    val originalHeight = 1920f
                    val newWidth = binding.overlayView.width.toFloat()
                    val newHeight = binding.overlayView.height.toFloat()
                    System.out.println("overlay scale ne" + binding.overlayView.scaleX)
                    System.out.println("overlay widht" + binding.overlayView.width.toFloat())
                    System.out.println("overlay height" + binding.overlayView.height.toFloat())
                    System.out.println("prewiew widht" + binding.viewFinder.width.toFloat())
                    System.out.println("prewiew height" + binding.viewFinder.height.toFloat())
                    val newrect = adjustRectForResolution(rect, originalWidth, originalHeight, newWidth, newHeight)
                    boxes.add(Triple(responseName, newrect,confidence))

                }

                val overlayView = binding.overlayView
                activity?.runOnUiThread {
                    overlayView.setBoxes(boxes)
                }

              /*  rotatedBitmap?.let { bitmap ->
                    val scaledBoxes = scaleCoordinates(boxes, bitmap)
                    val drawnBitmap = bitmap.drawBoxes(scaledBoxes,Color.RED)
                    binding.imageView2.setImageBitmap(drawnBitmap)
                }

               */

                jsonArray.remove(0)
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                Log.d(TAG, "onClose")
            }

            override fun onError(ex: Exception?) {
                Log.e(TAG, "onError: ${ex?.message}")
                System.out.println("error")
            }
        }
    }

 /*   fun drawDetectionBox(canvas: Canvas, detection: DetectionBox, paint: Paint) {
        val left = detection.topLeftX.coerceAtMost(detection.bottomLeftX)
        val top = detection.topLeftY.coerceAtMost(detection.topRightY)
        val right = detection.topRightX.coerceAtLeast(detection.bottomRightX)
        val bottom = detection.bottomLeftY.coerceAtLeast(detection.bottomRightY)

        canvas.drawRect(left, top, right, bottom, paint)
    }

  */
  /*  private fun scaleCoordinates(boxes: List<Pair<String, RectF>>, bitmap: Bitmap): List<Pair<String, RectF>> {
        val scaleX = binding.imageView2.width.toFloat() / bitmap.width
        val scaleY = binding.imageView2.height.toFloat() / bitmap.height

        return boxes.map { (text, rect) ->
            val scaledRect = RectF(
                rect.left * scaleX,
                rect.top * scaleY,
                rect.right * scaleX,
                rect.bottom * scaleY
            )
            Pair(text, scaledRect)
        }
    }

   */

    fun Bitmap.drawBoxes(boxes: List<Pair<String, RectF>>, textColor: Int): Bitmap {
        val mutableBitmap = this.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        val paint = Paint().apply {
            color = Color.GREEN
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }

        for ((text, rect) in boxes) {
            canvas.drawRect(rect, paint)
            val textPaint = Paint().apply {
                color = Color.RED
                textSize = 20F
                isAntiAlias = true
                typeface = Typeface.DEFAULT_BOLD
            }
            val textX = rect.left.toFloat()
            val textY = rect.top.toFloat() - textPaint.descent()
            canvas.drawText(text, textX, textY, textPaint)
        }

        return mutableBitmap
    }

    private fun sendMessage(byteArray : ByteArray) {
        try {
            if (::webSocketClient.isInitialized && webSocketClient.isOpen) {
                Log.d(TAG, "Sending image data to WebSocket.")
                System.out.println("Sending image data to WebSocket.")
                webSocketClient.send(byteArray)
            } else {
                Log.e(TAG, "WebSocket is not initialized or not open.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error while sending image data: ${e.message}")
        }
    }



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
                .setTargetResolution(Size(1080, 1920))
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, LuminosityAnalyzer { luma ->
                        Log.d(TAG, "Average luminosity: $luma")
                    })
                }

           // val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this,cameraSelector,preview, imageCapture,imageAnalyzer)
            }catch (exc : Exception) {
                System.out.println(exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }


    class LuminosityAnalyzer(private val listener: LumaListener) : ImageAnalysis.Analyzer {

        private fun ByteBuffer.toByteArray(): ByteArray {
            rewind()
            val data = ByteArray(remaining())
            get(data)
            return data
        }

        @ExperimentalGetImage
        override fun analyze(imageProxy: ImageProxy) {
                bitmap = imageProxy.image?.toBitmap()
            if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                rotatedBitmap = bitmap?.let { rotateBitmap(it, 270f) }
            } else {
                rotatedBitmap = bitmap?.let { rotateBitmap(it, 90f) }
            }
             //   rotatedBitmap =
             //       bitmap?.let { rotateBitmap(it, 90f) }
                byteArray = rotatedBitmap?.toByteArray()

            System.out.println("image widht " + rotatedBitmap?.width)
            System.out.println("image height " + rotatedBitmap?.height)


            val buffer = imageProxy.planes[0].buffer
            val data = buffer.toByteArray()
            val pixels = data.map { it.toInt() and 0xFF }
            val luma = pixels.average()
            listener(luma)
            imageProxy.close()
        }

        fun Bitmap.toByteArray(): ByteArray {
            val outputStream = ByteArrayOutputStream()
            this.compress(Bitmap.CompressFormat.PNG, 100, outputStream) // PNG formatında sıkıştır
            return outputStream.toByteArray()
        }

        fun Image.toBitmap(): Bitmap {
            val yBuffer = planes[0].buffer
            val vuBuffer = planes[2].buffer

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



    companion object {
        private const val TAG = "CameraPreviewFragment"
    }

}
private lateinit var webSocket: WebSocket
val WEB_SOCKET_URL = "ws://capstone.gokceonur.com"
private lateinit var webSocketClient: WebSocketClient
typealias LumaListener = (luma: Double) -> Unit
var sendImage : String = ""
val jsonObject = JSONObject()
var lensFacing = CameraSelector.LENS_FACING_FRONT
var rotatedBitmap : Bitmap? = null
private var isFirst: Boolean = true
var box : Bitmap? = null