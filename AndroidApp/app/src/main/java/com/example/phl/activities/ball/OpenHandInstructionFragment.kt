package com.example.phl.activities.ball

import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.SurfaceView
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
import com.example.phl.views.MyModelView
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
    lateinit var surfaceView: SurfaceView
    val modelView: MyModelView = MyModelView()

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
        surfaceView = view.findViewById<View>(R.id.surfaceView) as SurfaceView

        modelView.run {
            loadEntity()
            setSurfaceView(surfaceView)
            loadGlb(requireContext(), "hand.glb",1)
            loadIndirectLight(requireContext(), "light.ktx")
        }

        val button = view.findViewById<TextView>(R.id.button)
        button.setOnClickListener {
                val bundle = Bundle()
                bundle.putString("testType", "OpenHand")
                bundle.putString("sessionId", arguments?.getString("sessionId")!!)
                bundle.putDouble("closeHandTestResult", arguments?.getDouble("closeHandTestResult")!!)
                findNavController().navigate(R.id.action_openHandInstructionFragment_to_camera_fragment, bundle)

        }
    }

    override fun onResume() {
        super.onResume()
        modelView.onResume()
    }

    override fun onPause() {
        super.onPause()
        modelView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        modelView.onDestroy()
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