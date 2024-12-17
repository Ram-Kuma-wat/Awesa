package com.game.awesa.ui.recorder

import android.content.Context
import android.util.Log
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Recording
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.codersworld.awesalibs.beans.matches.MatchesBean
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.ExecutionException
import javax.inject.Inject

@HiltViewModel
@Suppress("LongParameterList")
class CameraViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private var cameraProviderLiveData: MutableLiveData<ProcessCameraProvider>? = null

    private var mMatchBean: MatchesBean.InfoBean? = savedState[CameraActivity.EXTRA_MATCH_BEAN]
    private var mHalf: Int? = savedState[CameraActivity.EXTRA_MATCH_HALF]

    private var _currentRecording: MutableLiveData<Recording?> = MutableLiveData()

    val currentRecording: LiveData<Recording?> = _currentRecording

    // Handle any errors (including cancellation) here.
    val processCameraProvider: LiveData<ProcessCameraProvider>?
        get() {
            if (cameraProviderLiveData == null) {
                cameraProviderLiveData = MutableLiveData()
                val cameraProviderFuture =
                    ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener(
                    {
                        try {
                            cameraProviderLiveData?.setValue(cameraProviderFuture.get())
                        } catch (e: ExecutionException) {
                            // Handle any errors (including cancellation) here.
                            Log.e(TAG, "Unhandled exception", e)
                        } catch (e: InterruptedException) {
                            Log.e(TAG, "Unhandled exception", e)
                        }
                    },
                    ContextCompat.getMainExecutor(context)
                )
            }
            return cameraProviderLiveData
        }

    fun setRecording(recording: Recording?) {
        if(_currentRecording.value != null) {
            _currentRecording.value?.stop()
        }
        _currentRecording.value = recording
    }

    companion object {
        private val TAG = CameraViewModel::class.java.simpleName
    }

}