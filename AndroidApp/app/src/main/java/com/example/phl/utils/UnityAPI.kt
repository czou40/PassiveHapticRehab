package com.example.phl.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.phl.activities.MainUnityActivity
import com.example.phl.data.AppDatabase
import com.example.phl.data.unity.BaseUnityGameResultDao
import com.example.phl.data.unity.IUnityGameResult
import com.example.phl.data.unity.ShoulderExtensionFlexionResult
import com.google.gson.Gson
import com.unity3d.player.UnityPlayer
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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

        fun processCommand(context: Context, command: String, callback: CommandCallback) {
            val parts = command.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            assert(parts.size == 3)
            val commandType = parts[0]
            val game = parts[1]
            val json = parts[2]
            Log.d("UnityAPI", "Processing command: $commandType for game: $game with json: $json")
            when (commandType) {
                "sendCompoundScore" -> saveGameResult(context, game, json, callback)
                else ->  {
                    callback.onFailure("Unknown command type: $commandType", CallBackOperation.NONE)
                }
            }
        }

        @Suppress("UNCHECKED_CAST")
        private suspend fun <T:IUnityGameResult> insertResultToDatabase(context: Context, entity:T) {
            val db = AppDatabase.getInstance(context)
            val dao = when (entity) {
                is ShoulderExtensionFlexionResult -> db.shoulderExtensionFlexionResultDao()
                else -> null
            } as BaseUnityGameResultDao<T>
            dao.insert(entity)
            Log.d("UnityAPI", "Inserted game result to database: $entity")
        }

        @OptIn(DelicateCoroutinesApi::class)
        private fun saveGameResult(context: Context, game: String, json: String, callback: CommandCallback) {
            // Save game result to database
            Log.d("UnityAPI", "Saving game result for game: $game with json: $json")
            val dataClass = when (game) {
                "Game1" -> ShoulderExtensionFlexionResult::class
                else -> null
            }
            Log.d("UnityAPI", "Data class: $dataClass")
            if (dataClass != null) {
                val result = Gson().fromJson(json, dataClass.java) as IUnityGameResult
                Log.d("UnityAPI", "Parsed game result: $result")
                // Save result to database
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        insertResultToDatabase(context, result)
                        callback.onSuccess("Game result saved successfully", CallBackOperation.LOAD_PROGRESS_VISUALIZATION)
                    } catch (e: Exception) {
                        Log.e("UnityAPI", "Failed to save game result: ${e.message}")
                        callback.onFailure("Failed to save game result: ${e.message}", CallBackOperation.QUIT)
                    }
                }
            } else {
                Log.e("UnityAPI", "Unknown game: $game")
            }
        }
    }

    enum class CallBackOperation {
        LOAD_PROGRESS_VISUALIZATION,
        QUIT,
        NONE
    }

    interface CommandCallback {
        fun onSuccess(message: String, operation: CallBackOperation)
        fun onFailure(error: String, operation: CallBackOperation)
    }
}