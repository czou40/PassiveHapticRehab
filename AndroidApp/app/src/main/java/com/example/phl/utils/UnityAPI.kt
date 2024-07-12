package com.example.phl.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.phl.activities.MainUnityActivity
import com.unity3d.player.UnityPlayer

class UnityAPI {
    enum class Scene {
        GAME_1,
        GAME_2,
        GAME_4,
        None
    }

    companion object {
        fun launchUnityActivity(context: Context, scene: Scene) {
            if (scene == Scene.None) return
            val intent = Intent(context, MainUnityActivity::class.java)
            Log.d("UnityAPI", "Launching Unity Activity with scene: ${scene.name}")
            intent.putExtra("loadScene", scene.name)
            context.startActivity(intent)
        }

        fun loadStartScene(scene: Scene, pause: Boolean = true) {
            when (scene) {
                Scene.GAME_1 -> {
                    UnityPlayer.UnitySendMessage(
                        "GameManager",
                        "ReceiveCommand",
                        if (pause) "pload StartScene1" else "load StartScene1"
                    )
                }

                Scene.GAME_2 -> {
                    UnityPlayer.UnitySendMessage(
                        "GameManager",
                        "ReceiveCommand",
                        if (pause) "pload StartScene2" else "load StartScene2"
                    )
                }

                Scene.GAME_4 -> {
                    UnityPlayer.UnitySendMessage(
                        "GameManager", "ReceiveCommand", if (pause) "pload Game4" else "load Game4"
                    )
                }

                Scene.None -> {
                    // Do nothing
                }
            }
        }

        fun loadGameScene(scene: Scene, pause: Boolean = false) {
            when (scene) {
                Scene.GAME_1 -> {
                    UnityPlayer.UnitySendMessage(
                        "GameManager", "ReceiveCommand", if (pause) "pload Game1" else "load Game1"
                    )
                }

                Scene.GAME_2 -> {
                    UnityPlayer.UnitySendMessage(
                        "GameManager", "ReceiveCommand", if (pause) "pload Game2" else "load Game2"
                    )
                }

                Scene.GAME_4 -> {
                    UnityPlayer.UnitySendMessage(
                        "GameManager", "ReceiveCommand", if (pause) "pload Game4" else "load Game4"
                    )
                }

                Scene.None -> {
                    // Do nothing
                }
            }
        }

        fun pauseGame() {
            UnityPlayer.UnitySendMessage(
                "GameManager", "ReceiveCommand", "pause"
            )
        }

        fun resumeGame() {
            UnityPlayer.UnitySendMessage(
                "GameManager", "ReceiveCommand", "resume"
            )
        }
    }
}