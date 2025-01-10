package com.game.awesa.ui.recorder

import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.PendingRecording
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.media3.common.util.UnstableApi
import com.game.awesa.ui.Awesa
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.ExecutionException
import javax.inject.Inject

@HiltViewModel
@Suppress("LongParameterList")
class CameraViewModel @OptIn(UnstableApi::class)
@Inject constructor(
    @ApplicationContext private val context: Context,
    private val awesa: Awesa
) : ViewModel() {
    private var cameraProviderLiveData: MutableLiveData<ProcessCameraProvider>? = null

    val recordEvent: LiveData<VideoRecordEvent?>
        get() {
            return awesa.recordEvent
        }

    val currentRecording: LiveData<Recording?>
        @OptIn(UnstableApi::class)
        get() {
            return awesa.currentRecording
        }

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

    @OptIn(UnstableApi::class)
    fun setRecording(recording: Recording?) {
        if(awesa.currentRecording.value != null) {
            awesa.currentRecording.value?.stop()
        }

        awesa.recordEvent.value = null
        awesa.currentRecording.value = recording
    }

    @OptIn(UnstableApi::class)
    fun startRecording(pendingRecording: PendingRecording) {
        awesa.startRecording(pendingRecording)
    }

    override fun onCleared() {
        awesa.recordEvent.value = null
        super.onCleared()
    }

    companion object {
        private val TAG = CameraViewModel::class.java.simpleName
    }

}