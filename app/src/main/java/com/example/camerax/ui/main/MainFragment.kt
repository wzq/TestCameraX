package com.example.camerax.ui.main

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.camera.core.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.camerax.R
import com.example.camerax.ui.gallery.GalleryActivity
import java.io.File
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.system.measureTimeMillis


class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
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
    private val previewView: ImageView? by lazy {
        view?.findViewById<ImageView>(R.id.preview)?.apply { setOnClickListener {
            startActivity(Intent(requireActivity(), GalleryActivity::class.java))
        } }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val dir = requireActivity().externalMediaDirs.first()
        if (dir.isDirectory) {
            val lastImage = dir.listFiles()?.lastOrNull()
            if (lastImage != null) {
                Glide.with(this).load(lastImage).into(previewView!!)
            }
        }
        val executor = ContextCompat.getMainExecutor(requireContext())
        val cameraView = view?.findViewById<TextureView>(R.id.camera) ?: return
        view?.findViewById<View>(R.id.take_photo)?.setOnClickListener {
            imageCapture.takePicture(
                getFile(),
                executor,
                imageSavedListener
            )
        }

        cameraView.post {
//            val metrics = DisplayMetrics().also { cameraView.display.getRealMetrics(it) }
            val screenAspectRatio = aspectRatio(cameraView.width, cameraView.height)
            println()
            val previewConfig = PreviewConfig.Builder().apply {
                setLensFacing(lensFacing)
                setTargetAspectRatio(screenAspectRatio)
                setTargetRotation(cameraView.display.rotation)
            }.build()
            val preview = AutoFitPreviewBuilder.build(previewConfig, cameraView)

            imageCapture = ImageCapture(
                ImageCaptureConfig.Builder()
                    .setLensFacing(lensFacing)
                    .setCaptureMode(ImageCapture.CaptureMode.MAX_QUALITY)
                    .setTargetAspectRatio(screenAspectRatio)
                    .setTargetRotation(cameraView.display.rotation)
                    .build())

            CameraX.bindToLifecycle(viewLifecycleOwner, preview, imageCapture)

        }
    }

    private val imageSavedListener = object : ImageCapture.OnImageSavedListener {
        override fun onImageSaved(file: File) {
            Glide.with(this@MainFragment).load(file).into(previewView!!)
            Toast.makeText(requireContext(), "保存地址:${file.absolutePath}", Toast.LENGTH_SHORT).show()
        }

        override fun onError(
            imageCaptureError: ImageCapture.ImageCaptureError,
            message: String,
            cause: Throwable?
        ) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            cause?.printStackTrace()
        }

    }

    private fun getFile(): File {
        val path = requireActivity().externalMediaDirs.first()
        return File(path, "test${System.currentTimeMillis()}.png")
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
