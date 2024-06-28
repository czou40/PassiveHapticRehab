package com.example.phl.utils

import com.unity3d.player.UnityPlayer

class UnityAPI {
    enum class Scene {
        GAME_1,
        GAME_2,
        GAME_4
    }

    companion object {
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