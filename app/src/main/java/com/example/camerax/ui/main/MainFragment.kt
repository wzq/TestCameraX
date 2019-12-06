package com.example.camerax.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.camerax.R
import java.io.File
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private val filePath by lazy {
        File(requireActivity().externalMediaDirs.first(), "test.png")
    }

    private val lensFacing = CameraX.LensFacing.BACK


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    private lateinit var imageCapture: ImageCapture


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val executor = ContextCompat.getMainExecutor(requireContext())
        val cameraView = view?.findViewById<TextureView>(R.id.camera) ?: return
        cameraView.setOnClickListener {
            imageCapture.takePicture(
                filePath,
                executor,
                imageSavedListener
            )
        }

        cameraView.post {
//            val metrics = DisplayMetrics().also { cameraView.display.getRealMetrics(it) }
            val screenAspectRatio = aspectRatio(cameraView.width, cameraView.height)

            val previewConfig = PreviewConfig.Builder().apply {
                setLensFacing(lensFacing)
                setTargetAspectRatio(screenAspectRatio)
                setTargetRotation(cameraView.display.rotation)
            }.build()
            val preview = AutoFitPreviewBuilder.build(previewConfig, cameraView)

            imageCapture = ImageCapture(
                ImageCaptureConfig.Builder()
                    .setLensFacing(lensFacing)
                    .setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                    .setTargetAspectRatio(screenAspectRatio)
                    .setTargetRotation(cameraView.display.rotation)
                    .build())

            CameraX.bindToLifecycle(viewLifecycleOwner, preview, imageCapture)

        }
    }

    private val imageSavedListener = object : ImageCapture.OnImageSavedListener {
        override fun onImageSaved(file: File) {
            println("success --> ${file.absolutePath}")
        }

        override fun onError(
            imageCaptureError: ImageCapture.ImageCaptureError,
            message: String,
            cause: Throwable?
        ) {
            println(message)
            cause?.printStackTrace()
        }

    }


    private fun aspectRatio(width: Int, height: Int): AspectRatio {
        val ratio43 = 4.0 / 3.0
        val ratio169 = 16.0 / 9.0
        val previewRatio = max(width, height).toDouble() / min(width, height)

        if (abs(previewRatio - ratio43) <= abs(previewRatio - ratio169)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }
}
