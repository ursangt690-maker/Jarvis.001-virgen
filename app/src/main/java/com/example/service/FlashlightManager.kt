package com.example.service

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FlashlightManager(private val context: Context) {

    private val cameraManager: CameraManager? =
        context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager

    private val _isTorchOn = MutableStateFlow(false)
    val isTorchOn: StateFlow<Boolean> = _isTorchOn.asStateFlow()

    private val _isAvailable = MutableStateFlow(false)
    val isAvailable: StateFlow<Boolean> = _isAvailable.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var targetCameraId: String? = null

    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
            super.onTorchModeChanged(cameraId, enabled)
            if (cameraId == targetCameraId) {
                _isTorchOn.value = enabled
            }
        }

        override fun onTorchModeUnavailable(cameraId: String) {
            super.onTorchModeUnavailable(cameraId)
            if (cameraId == targetCameraId) {
                _isTorchOn.value = false
            }
        }
    }

    init {
        detectFlashlight()
        try {
            cameraManager?.registerTorchCallback(torchCallback, null)
        } catch (_: Exception) {}
    }

    private fun detectFlashlight() {
        if (cameraManager == null) {
            _isAvailable.value = false
            _errorMessage.value = "CameraManager service unavailable on this device."
            return
        }

        try {
            val cameraIds = cameraManager.cameraIdList
            for (id in cameraIds) {
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    targetCameraId = id
                    _isAvailable.value = true
                    return
                }
            }
            // Fallback to any camera with flash if no back camera found
            for (id in cameraIds) {
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                if (hasFlash) {
                    targetCameraId = id
                    _isAvailable.value = true
                    return
                }
            }
            _isAvailable.value = false
            _errorMessage.value = "No hardware flash module detected on this device."
        } catch (e: Exception) {
            _isAvailable.value = false
            _errorMessage.value = "Hardware query failed: ${e.localizedMessage}"
        }
    }

    fun toggleFlashlight(): Boolean {
        return setFlashlight(!_isTorchOn.value)
    }

    fun setFlashlight(enabled: Boolean): Boolean {
        val camId = targetCameraId
        if (camId == null || cameraManager == null) {
            _errorMessage.value = "Flashlight hardware not available."
            return false
        }

        return try {
            cameraManager.setTorchMode(camId, enabled)
            _isTorchOn.value = enabled
            _errorMessage.value = null
            true
        } catch (e: CameraAccessException) {
            _errorMessage.value = "Camera access error: ${e.localizedMessage}"
            false
        } catch (e: SecurityException) {
            _errorMessage.value = "Camera permission required for flashlight."
            false
        } catch (e: Exception) {
            _errorMessage.value = "Flashlight error: ${e.localizedMessage}"
            false
        }
    }

    fun cleanup() {
        if (_isTorchOn.value) {
            setFlashlight(false)
        }
        try {
            cameraManager?.unregisterTorchCallback(torchCallback)
        } catch (_: Exception) {}
    }
}
