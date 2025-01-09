package com.codersworld.awesalibs.mediapicker.ui.activity

import android.Manifest
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.codersworld.awesalibs.R
import com.codersworld.awesalibs.databinding.ActivityImagePickerBinding
import com.codersworld.awesalibs.mediapicker.addFragment
import com.codersworld.awesalibs.mediapicker.dispatchTakePictureIntent
import com.codersworld.awesalibs.mediapicker.getBooleanAttribute
import com.codersworld.awesalibs.mediapicker.getColorAttribute
import com.codersworld.awesalibs.mediapicker.getModel
import com.codersworld.awesalibs.mediapicker.model.Folder
import com.codersworld.awesalibs.mediapicker.model.Image
import com.codersworld.awesalibs.mediapicker.model.PickerConfig
import com.codersworld.awesalibs.mediapicker.model.PickerType
import com.codersworld.awesalibs.mediapicker.registerActivityResult
import com.codersworld.awesalibs.mediapicker.replaceFragment
import com.codersworld.awesalibs.mediapicker.ui.fragment.ImageFragment
import com.codersworld.awesalibs.mediapicker.util.isAtLeast13
import com.codersworld.awesalibs.mediapicker.viewmodel.ImagePickerViewModel
import kotlinx.coroutines.launch

/**
 * ImagePickerActivity to display all the images folder wise.
 * After picking images from here the results are sent back to ImagePicker class
 * and the caller is notified via callback
 */
class ImagePickerActivity : AppCompatActivity(), View.OnClickListener {

    companion object {
        val TAG: String = ImagePickerActivity::class.java.simpleName
    }

    private lateinit var binding: ActivityImagePickerBinding
    private val viewModel: ImagePickerViewModel by viewModels {
        ViewModelProvider.AndroidViewModelFactory(application)
    }
    private lateinit var pickerConfig: PickerConfig
    private var fileUri: Uri? = null
    private var toolBarTitle = ""
    private lateinit var backPressedCallback: OnBackPressedCallback
    private var openCameraAfterPermission: Boolean = false
    private var disableInteraction: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = getColorAttribute(R.attr.ssStatusBarColor)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars =
            getBooleanAttribute(R.attr.ssStatusBarLightMode)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image_picker)
        pickerConfig = intent.getModel() ?: PickerConfig.defaultPicker()
        viewModel.updatePickerConfig(pickerConfig)
        setUI()
        openCameraAfterPermission = pickerConfig.pickerType == PickerType.CAMERA
        pickImage()
        addObserver()
    }

    private fun setUI() {
        backPressedCallback = object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                onBackPressEvent()
            }
        }
        onBackPressedDispatcher.addCallback(this, backPressedCallback)
        updateToolBarTitle(pickerConfig.pickerTitle)
        binding.toolbar.apply {
            clickListener = this@ImagePickerActivity
            imageCameraButton.isVisible = pickerConfig.showCameraIconInGallery
            imageDoneButton.isVisible =
                pickerConfig.isDoneIcon && pickerConfig.allowMultipleSelection
            textDone.isVisible = !pickerConfig.isDoneIcon && pickerConfig.allowMultipleSelection
        }
    }

    private fun addObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.selectedFolder.collect { folder ->
                        openFolder(folder)
                    }
                }
                launch {
                    viewModel.completeSelection.collect { complete ->
                        if (complete) {
                            selectImages()
                        }
                    }
                }
                launch {
                    viewModel.updateImageCount.collect {
                        updateSelectCount()
                    }
                }
                launch {
                    viewModel.disableInteraction.collect {
                        disableInteraction = it
                        binding.progressIndicator.isVisible = it
                    }
                }
            }
        }
    }


    override fun onClick(view: View) {
        if (disableInteraction) {
            return
        }
        when (view.id) {
            R.id.image_back_button -> {
                onBackPressedDispatcher.onBackPressed()
            }
            R.id.image_camera_button -> {
                showCamera()
            }
            R.id.image_done_button, R.id.text_done -> {
                selectImages()
            }
        }
    }

    private fun pickImage() {
        if (openCameraAfterPermission) {
            showCamera()
        } else {
            showGallery()
        }
    }

    private fun updateToolBarTitle(title: String) {
        toolBarTitle = title
        updateSelectCount()
    }

    private fun updateSelectCount() {
        if (pickerConfig.showCountInToolBar && pickerConfig.allowMultipleSelection) {
            binding.toolbar.textTitle.text =
                getString(
                    R.string.str_selected_image_toolbar,
                    toolBarTitle,
                    viewModel.getSelectedImages().size,
                    pickerConfig.maxPickCount
                )
        } else {
            binding.toolbar.textTitle.text = toolBarTitle
        }
    }

    private fun showCamera() {
        fileUri = dispatchTakePictureIntent(onGetImageFromCameraActivityResult)

    }

    /**
     * Apps targeting for Android 13 or higher requires to declare READ_MEDIA_
     * to request the media that other apps have created.
     * The READ_EXTERNAL_STORAGE will not work on Android 13 and higher to access the media created by other apps.
     * If the user previously granted app the READ_EXTERNAL_STORAGE permission,
     * the system automatically grants the granular media permission.
     * [More Details](https://developer.android.com/about/versions/13/behavior-changes-13#granular-media-permissions)
     * So for Android 13+ we are asking for permission READ_MEDIA_IMAGES and
     * below that READ_EXTERNAL_STORAGE to get the images from device.
     */
    private fun showGallery() {
        val permission = if (isAtLeast13()) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (checkForPermission(permission)) {
            replaceFragment(getInitialFragment())
            viewModel.fetchImagesFromMediaStore()
        } else {
            openCameraAfterPermission = false
            askPermission(permission)
        }
    }

    private fun getInitialFragment(): Fragment {
        return ImageFragment.newInstance()
    }

    private fun checkForPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun askPermission(vararg permission: String) {
        permissionResult.launch(arrayOf(*permission))
    }

    private fun openFolder(folder: Folder) {
        updateToolBarTitle(folder.bucketName)
        backPressedCallback.isEnabled = true
        addFragment(ImageFragment.newInstance(folder.bucketId))
    }

    private fun onBackPressEvent() {
        val fragment = supportFragmentManager.findFragmentById(R.id.container_view)
        if (fragment != null && fragment is ImageFragment) {
            updateToolBarTitle(pickerConfig.pickerTitle)
            backPressedCallback.isEnabled = false
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private val permissionResult =
        activityResultRegistry.register(
            "Permission",
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            result?.let { mutableMap ->
                if (mutableMap.entries.all { entry -> entry.value }) {
                    pickImage()
                }
            }
        }


    private val onGetImageFromCameraActivityResult =
        registerActivityResult("Camera", errorCallback = {
            createSingleSelectionResult(null) }
        ) { result ->
            Log.d(TAG, result.data?.getStringExtra(MediaStore.EXTRA_OUTPUT).toString())
            fileUri?.let {
                uri -> checkForCropping(uri)
            } ?: createSingleSelectionResult(null)
        }

    private fun checkForCropping(imageUri: Uri) {
        createSingleSelectionResult(imageUri)
    }


    private fun selectImages() {
        val selectedImages = viewModel.getSelectedImages()
        if (selectedImages.isEmpty()) {
            finish()
        }
        if (pickerConfig.allowMultipleSelection) {
            checkForCompression(selectedImages)
        } else {
            checkForCropping(selectedImages.first().uri)
        }
    }

    private fun checkForCompression(selectedImages: List<Image>) {
        if (pickerConfig.compressImage) {
            lifecycleScope.launch {
                val compressedImage = viewModel.compressImage(selectedImages)
                createMultipleSelectionResult(compressedImage)
            }
        } else {
            createMultipleSelectionResult(selectedImages)
        }
    }

    private fun createMultipleSelectionResult(selectedImages: List<Image>) {
        val intent = Intent()
        if (selectedImages.size == 1) {
            intent.data = selectedImages.first().uri
        } else if (selectedImages.size > 1) {
            val clipData = ClipData.newUri(contentResolver, "ClipData", selectedImages.first().uri)
            selectedImages.takeLast(selectedImages.size - 1)
                .forEach { clipData.addItem(ClipData.Item(it.uri)) }
            intent.clipData = clipData
        }
        sendResult(intent)
    }

    private fun createSingleSelectionResult(uri: Uri?) {
        val intent = Intent()
        intent.data = uri
        sendResult(intent)
    }

    private fun sendResult(intent: Intent) {
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onDestroy() {
         onGetImageFromCameraActivityResult.unregister()
        super.onDestroy()
    }
}
