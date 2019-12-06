package com.example.camerax.ui.main

import android.graphics.Matrix
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.camerax.R
import kotlinx.android.synthetic.main.main_fragment.*
import java.io.File
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private val filePath by lazy {
        File(
            requireActivity().externalMediaDirs.first(), "test.png"
//            requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
//            "test.png"
        )
    }

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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val lensFacing = CameraX.LensFacing.BACK
        val executor = ContextCompat.getMainExecutor(requireContext())
        
        camera.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            updateTransform(camera)
        }
        view?.post {

            val metrics = DisplayMetrics().also { camera.display.getRealMetrics(it) }
            val screenAspectRatio = aspectRatio(metrics.widthPixels, metrics.heightPixels)
            println("ratio ---> ${screenAspectRatio.name}")

            val viewFinderConfig = PreviewConfig.Builder().apply {
                setLensFacing(lensFacing)
                // We request aspect ratio but no resolution to let CameraX optimize our use cases
                setTargetAspectRatio(screenAspectRatio)
                // Set initial target rotation, we will have to call this again if rotation changes
                // during the lifecycle of this use case
                setTargetRotation(camera.display.rotation)
            }.build()
//            val preview = AutoFitPreviewBuilder.build(viewFinderConfig, camera)

            val preview = Preview(viewFinderConfig).also {
                it.setOnPreviewOutputUpdateListener { et ->
                    val parent = camera.parent as? ViewGroup
                    parent?.removeAllViews()
                    parent?.addView(camera)
                    camera.surfaceTexture = et.surfaceTexture
                    updateTransform(camera)
                }
            }

            val imageCapture = ImageCapture(
                ImageCaptureConfig.Builder()
                    .setTargetRotation(camera.display.rotation)
                    .setLensFacing(lensFacing)
                    .setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                    .setTargetAspectRatio(screenAspectRatio)
                    .build())

//            val analyzerConfig = ImageAnalysisConfig.Builder().apply {
//                setLensFacing(lensFacing)
//                // In our analysis, we care more about the latest image than analyzing *every* image
//                setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
//                // Set initial target rotation, we will have to call this again if rotation changes
//                // during the lifecycle of this use case
//                setTargetRotation(camera.display.rotation)
//            }.build()
//
//            val imageAnalyzer = ImageAnalysis(analyzerConfig).apply {
//                setAnalyzer(executor,
//                    LuminosityAnalyzer { luma ->
//                        // Values returned from our analyzer are passed to the attached listener
//                        // We log image analysis results here --
//                        // you should do something useful instead!
//                        (analyzer as LuminosityAnalyzer).framesPerSecond
//                    })
//            }


            CameraX.bindToLifecycle(viewLifecycleOwner, preview, imageCapture)

            message.setOnClickListener {
                imageCapture.takePicture(
                    filePath,
                    executor,
                    imageSavedListener
                )
            }
        }
    }

    private fun updateTransform(viewFinder: TextureView) {
        val matrix = Matrix()
        // Compute the center of the view finder
        val centerX: Float = viewFinder.getWidth() / 2f
        val centerY: Float = viewFinder.getHeight() / 2f
        val rotations = floatArrayOf(0f, 90f, 180f, 270f)
        // Correct preview output to account for display rotation
        val rotationDegrees = rotations[viewFinder.getDisplay().getRotation()]
        matrix.postRotate(-rotationDegrees, centerX, centerY)
        // Finally, apply transformations to our TextureView
        viewFinder.setTransform(matrix)
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
        val ratio_4_3 = 4.0 / 3.0
        val ratio_16_9 = 16.0 / 9.0
        val previewRatio = max(width, height).toDouble() / min(width, height)

        if (abs(previewRatio - ratio_4_3) <= abs(previewRatio - ratio_16_9)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }
}
