package com.codersworld.awesalibs.mediapicker.listener

import android.net.Uri

/**
 * Result listener for the image picker.
 */
interface ImagePickerResultListener {
    fun onImagePick(uri: Uri?)
    fun onMultiImagePick(uris: List<Uri>?)
}