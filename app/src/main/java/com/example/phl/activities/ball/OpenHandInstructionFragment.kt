package com.example.phl.activities.ball

import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.core.view.isGone
import androidx.lifecycle.lifecycleScope
import com.example.phl.R
import com.gorisse.thomas.lifecycle.lifecycle
import io.github.sceneview.SceneView
import io.github.sceneview.loaders.loadHdrIndirectLight
import io.github.sceneview.loaders.loadHdrSkybox
import io.github.sceneview.nodes.ModelNode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.model.Model
import kotlinx.coroutines.launch
import kotlin.math.ceil

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [OpenHandInstructionFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OpenHandInstructionFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var sceneView: SceneView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_open_hand_instruction, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sceneView = view.findViewById<SceneView>(R.id.sceneView)
//            .apply {
////            setLifecycle(lifecycle)
//        }

        lateinit var model: Model

        lifecycleScope.launchWhenCreated  {
            val hdrFile = "background.hdr"
            sceneView.loadHdrIndirectLight(hdrFile, specularFilter = true) {
                intensity(30_000f)
            }
            sceneView.loadHdrSkybox(hdrFile) {
                intensity(50_000f)
            }
            model = sceneView.modelLoader.loadModel("hand.glb")!!
            val modelNode = ModelNode(sceneView, model).apply {
                transform(
                    position = Position(x=-0.2f, y=-1f, z=-3f),
                    rotation = Rotation(y=10f)
                )
                scaleToUnitsCube(20.0f)
                playAnimation(0)
            }
            sceneView.addChildNode(modelNode)
            startCountDown(view, 2000, false) {
                    val bundle = Bundle()
                    bundle.putString("testType", "OpenHand")
                    bundle.putString("sessionId", arguments?.getString("sessionId")!!)
                    bundle.putDouble("closeHandTestResult", arguments?.getDouble("closeHandTestResult")!!)
                    findNavController().navigate(R.id.action_openHandInstructionFragment_to_camera_fragment, bundle)

            }
        }
    }

    private fun startCountDown(view: View, milliSeconds: Long, visible:Boolean, callback:()->Unit){
        // Find your TextView
        val countdownTextView: TextView = view.findViewById(R.id.countdown_text)

        countdownTextView.visibility = if (visible) View.VISIBLE else View.GONE

        // Create a CountdownTimer
        object : CountDownTimer(milliSeconds, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Update TextView
                countdownTextView.text = ceil(millisUntilFinished / 1000.0).toInt().toString()

                // Create a scale animation
                val scaleAnimation = ScaleAnimation(
                    1f, 1.2f, 1f, 1.2f,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
                )
                scaleAnimation.duration = 1000
                // Start the animation
                countdownTextView.startAnimation(scaleAnimation)
            }

            override fun onFinish() {
                // Create a fade out animation
                val fadeOutAnimation = AlphaAnimation(1f, 0f)
                fadeOutAnimation.duration = 500

                // Start the animation
                countdownTextView.startAnimation(fadeOutAnimation)

                // Set visibility to GONE
                countdownTextView.postDelayed({
                    countdownTextView.visibility = View.GONE
                    callback()
                }, 500)
            }
        }.start()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment OpenHandInstructionFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            OpenHandInstructionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}