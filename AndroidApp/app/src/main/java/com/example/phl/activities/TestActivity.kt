package com.example.phl.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.example.phl.activities.ui.theme.PHLTheme

class TestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PHLTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(modifier: Modifier = Modifier) {
    val context = LocalContext.current // Get the local context to use for the Intent

    Column(modifier = modifier) {
        Text(text = "Hello, please choose a game to load!")
        Button(onClick = {
            val intent = Intent(context, MainUnityActivity::class.java)
            intent.putExtra("loadScene", "GAME_1")
            context.startActivity(intent)
        }) {
            Text(text = "Load Game 1")
        }
        Button(onClick = {
            val intent = Intent(context, MainUnityActivity::class.java)
            intent.putExtra("loadScene", "GAME_2") // Corrected to GAME_2
            context.startActivity(intent)
        }) {
            Text(text = "Load Game 2")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PHLTheme {
        Greeting()
    }
}
