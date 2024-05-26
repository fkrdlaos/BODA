package org.techtown.boda
import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.techtown.boda.databinding.FragmentCatchBinding
import org.tensorflow.lite.task.vision.detector.Detection
import java.io.File
import java.io.FileOutputStream
import java.util.LinkedList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.content.DialogInterface



class CatchFragment : Fragment(), ObjectDetectorHelper.DetectorListener {

    private val TAG = "ObjectDetection"
    private lateinit var word: String
    private var _fragmentCatchBinding: FragmentCatchBinding? = null
    private val fragmentCatchBinding get() = _fragmentCatchBinding!!

    private lateinit var objectDetectorHelper: ObjectDetectorHelper
    private lateinit var bitmapBuffer: Bitmap
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null

    private lateinit var cameraExecutor: ExecutorService

    private var captureImageFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 전달된 word 값을 받음
        arguments?.let {
            word = it.getString("word").toString()
        }
    }
    override fun onResume() {
        super.onResume()
        Log.i("onResume", "Camera resume")

        if (!PermissionsFragment.hasPermissions(requireContext())) {
            findNavController().navigate(CatchFragmentDirections.actionCatchToPermissions())
        }
    }

    override fun onDestroyView() {
        _fragmentCatchBinding = null
        super.onDestroyView()
        cameraExecutor.shutdown()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        Log.i("onCreateView", "version error")

        _fragmentCatchBinding = FragmentCatchBinding.inflate(inflater, container, false)
        return fragmentCatchBinding.root
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        objectDetectorHelper = ObjectDetectorHelper(
            context = requireContext(),
            objectDetectorListener = this
        )

        cameraExecutor = Executors.newSingleThreadExecutor()

        fragmentCatchBinding.viewFinder.post {
            setUpCamera()
        }

        fragmentCatchBinding.captureButton.setOnClickListener {
            captureImageFlag = true
        }

        fragmentCatchBinding.backButton.setOnClickListener {
            requireActivity().finish()
        }

    }

    private fun updateControlsUi() {
        objectDetectorHelper.clearObjectDetector()
        fragmentCatchBinding.overlay.clear()
    }

    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(
            {
                try {
                    cameraProvider = cameraProviderFuture.get()
                    bindCameraUseCases()
                } catch (e: Exception) {
                    Log.e(TAG, "Camera initialization failed.", e)
                }
            },
            ContextCompat.getMainExecutor(requireContext())
        )
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun bindCameraUseCases() {
        val cameraProvider = cameraProvider ?: throw IllegalStateException("Camera initialization failed.")

        val cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

        preview = Preview.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(fragmentCatchBinding.viewFinder.display.rotation)
            .build()

        imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_4_3)
            .setTargetRotation(fragmentCatchBinding.viewFinder.display.rotation)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor) { image ->
                    if (!::bitmapBuffer.isInitialized) {
                        bitmapBuffer = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
                    }
                    detectObjects(image)
                }
            }

        cameraProvider.unbindAll()

        try {
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
            preview?.setSurfaceProvider(fragmentCatchBinding.viewFinder.surfaceProvider)
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun detectObjects(image: ImageProxy) {
        try {
            bitmapBuffer.copyPixelsFromBuffer(image.planes[0].buffer)
            val imageRotation = image.imageInfo.rotationDegrees
            objectDetectorHelper.detect(bitmapBuffer, imageRotation)
        } finally {
            image.close()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        imageAnalyzer?.targetRotation = fragmentCatchBinding.viewFinder.display.rotation
    }

    override fun onResults(results: MutableList<Detection>?, inferenceTime: Long, imageHeight: Int, imageWidth: Int) {

        var resultSize = (results!!.size)

        activity?.runOnUiThread {
            fragmentCatchBinding.overlay.setResults(results ?: LinkedList(), imageHeight, imageWidth)
            fragmentCatchBinding.overlay.invalidate()

            // Check if the capture flag is set
            if (captureImageFlag && results != null) {
                captureImageFlag = false // Reset the flag
                // Process results and save the desired object
                results.forEach { detection ->
                    if (detection.categories[0].label == word) { // Replace "desired_label" with your label
                        val boundingBox = detection.boundingBox

                        // Ensure the bounding box is within the bitmap boundaries
                        val left = boundingBox.left.coerceAtLeast(0f).toInt()
                        val top = boundingBox.top.coerceAtLeast(0f).toInt()
                        val right = boundingBox.right.coerceAtMost(bitmapBuffer.width.toFloat()).toInt()
                        val bottom = boundingBox.bottom.toInt().coerceAtMost(bitmapBuffer.height)
                        val width = right - left
                        val height = bottom - top
                        if (width > 0 && height > 0) {
                            // Rotate the image back to the correct orientation
                            val matrix = Matrix()
                            matrix.postRotate(90f) // Adjust the rotation if necessary
                            val rotatedBitmap = Bitmap.createBitmap(bitmapBuffer, 0, 0, bitmapBuffer.width, bitmapBuffer.height, matrix, true)

                            // Crop the desired object
                            val croppedBitmap = Bitmap.createBitmap(rotatedBitmap, left, top, width, height)

                            // Save the cropped bitmap
                            saveBitmapToFile(croppedBitmap)
                            CatchDialog.CatchBuilder(this.context)
                                .setPositiveButtonListener { dialog, which ->
                                    dialog.dismiss()
                                    activity?.finish()
                                }
                                .setTitle("CATCH")
                                .setLeftMessage("")
                                .setRightMessage("")
                                .setCenterMessage(word+"를 찾았어!!!")
                                .build()
                                .showDialog()

                        }
                    } else {
                        resultSize--;
                        if(resultSize == 0){
                            NoticeDialog.Builder(this.context)
                                .setTitle("")
                                .setLeftMessage("")
                                .setRightMessage("")
                                .setCenterMessage("찾는 그림이 없어요..")
                                .build()
                                .showDialog()

                        }
                    }
                }

            }
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap) {
        val filename = "cropped_image_${System.currentTimeMillis()}.png"
        val file = File(requireContext().externalMediaDirs.firstOrNull(), filename)

        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
        }

        Toast.makeText(context, "Image saved: ${file.absolutePath}", Toast.LENGTH_SHORT).show()
        saveImgtoRTDB(file.absolutePath)
    }

    private fun saveImgtoRTDB(path: String) {
        RTDatabase.addCatchImgPath(word, path)
    }
    override fun onError(error: String) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }
    }
}
