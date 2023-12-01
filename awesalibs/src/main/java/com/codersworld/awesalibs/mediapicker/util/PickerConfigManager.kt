package com.codersworld.awesalibs.mediapicker.util

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryOwner
import com.codersworld.awesalibs.mediapicker.EXTRA_IMAGE_PICKER_CONFIG
import com.codersworld.awesalibs.mediapicker.getModel
import com.codersworld.awesalibs.mediapicker.model.PickerConfig

internal class PickerConfigManager(registryOwner: SavedStateRegistryOwner) :
    SavedStateRegistry.SavedStateProvider {
    companion object {
        const val PICKER_CONFIG_MANAGER = "picker_config_manage"
    }

    private var pickerConfig = PickerConfig.defaultPicker()

    init {
        val registry = registryOwner.savedStateRegistry
        registryOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_CREATE) {
                if (registry.getSavedStateProvider(PICKER_CONFIG_MANAGER) == null) {
                    registry.registerSavedStateProvider(PICKER_CONFIG_MANAGER, this)
                }
                val previousState = registry.consumeRestoredStateForKey(PICKER_CONFIG_MANAGER)
                if (previousState != null && previousState.containsKey(EXTRA_IMAGE_PICKER_CONFIG)) {
                    pickerConfig = previousState.getModel() ?: PickerConfig.defaultPicker()
                }
            } else if (event == Lifecycle.Event.ON_DESTROY) {
                registry.unregisterSavedStateProvider(PICKER_CONFIG_MANAGER)
            }
        })
    }

    override fun saveState(): Bundle {
        return bundleOf(EXTRA_IMAGE_PICKER_CONFIG to pickerConfig)
    }

    fun getPickerConfig(): PickerConfig {
        return pickerConfig
    }
}
