// Adapted from https://github.com/Sergiioh/android-model-viewer
package com.example.phl.views

import android.content.Context
import android.view.Choreographer
import android.view.SurfaceView
import com.google.android.filament.Camera
import com.google.android.filament.Skybox
import com.google.android.filament.utils.Float3
import com.google.android.filament.utils.KTX1Loader
import com.google.android.filament.utils.ModelViewer
import com.google.android.filament.utils.Utils
import com.google.android.filament.utils.max
import com.google.android.filament.utils.scale
import com.google.android.filament.utils.translation
import com.google.android.filament.utils.transpose
import java.nio.ByteBuffer


class MyModelView {
    companion object {
        init {
            Utils.init()
        }
    }

    private lateinit var choreographer: Choreographer
    private lateinit var modelViewer: ModelViewer
    private var animation = 0

    fun loadEntity() {
        choreographer = Choreographer.getInstance()
    }

    fun setSurfaceView(mSurfaceView: SurfaceView) {
        modelViewer = ModelViewer(mSurfaceView)
        mSurfaceView.setOnTouchListener(modelViewer)

        //Skybox and background color
        //without this part the scene'll appear broken
        modelViewer.scene.skybox = Skybox.Builder().build(modelViewer.engine)
        modelViewer.scene.skybox?.setColor(1.0f, 1.0f, 1.0f, 1.0f) //White color
    }

    fun loadGlb(context: Context, name: String, animation: Int = 0) {
        this.animation = animation
        val buffer = readAsset(context, name)
        modelViewer.apply {
            loadModelGlb(buffer)
//            transformToUnitCube()
        }
    }

    fun loadIndirectLight(context: Context, ibl: String) {
        // Create the indirect light source and add it to the scene.
        val buffer = readAsset(context, ibl)
        KTX1Loader.createIndirectLight(modelViewer.engine, buffer).apply {
            intensity = 30000f
            modelViewer.scene.indirectLight = this
        }
    }

    fun loadEnvironment(context: Context, ibl: String) {
        // Create the sky box and add it to the scene.
        val buffer = readAsset(context, ibl)
        KTX1Loader.createSkybox(modelViewer.engine, buffer).apply {
            modelViewer.scene.skybox = this
        }
    }

    private fun readAsset(context: Context, assetName: String): ByteBuffer {
        val input = context.assets.open(assetName)
        val bytes = ByteArray(input.available())
        input.read(bytes)
        return ByteBuffer.wrap(bytes)
    }

    private val frameCallback = object : Choreographer.FrameCallback {
        private val startTime = System.nanoTime()
        override fun doFrame(currentTime: Long) {
            val seconds = (currentTime - startTime).toDouble() / 1_000_000_000
            choreographer.postFrameCallback(this)
            modelViewer.animator?.apply {
                if (animationCount > animation) {
                    applyAnimation(animation, seconds.toFloat())
                }
                updateBoneMatrices()
            }
            modelViewer.render(currentTime)
        }
    }

    fun onResume() {
        choreographer.postFrameCallback(frameCallback)
    }

    fun onPause() {
        choreographer.removeFrameCallback(frameCallback)
    }

    fun onDestroy() {
        choreographer.removeFrameCallback(frameCallback)
    }
}