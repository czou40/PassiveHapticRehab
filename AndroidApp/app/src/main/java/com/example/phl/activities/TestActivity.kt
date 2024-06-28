package com.example.phl.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.phl.R
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
        Button(onClick = {
            val intent = Intent(context, MainUnityActivity::class.java)
            intent.putExtra("loadScene", "GAME_4")
            context.startActivity(intent)
        }) {
            Text(text = "Load Game 4")
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

@Composable
fun UpperLimbTestsScreen(
    tests: List<Test>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1D9981)) // Set background color
            .padding(16.dp)
    ) {
        Text(
            text = "Upper Limb Tests",
            style = MaterialTheme.typography.headlineMedium.copy(color = Color.White),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(tests) { test ->
                TestCard(test)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { /* Handle start button click */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Start")
        }
    }
}

@Composable
fun TestCard(test: Test) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Image(
                painter = painterResource(id = test.imageRes),
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .padding(8.dp)
            )
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = test.title,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = test.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

data class Test(val title: String, val description: String, val imageRes: Int)

@Preview(showBackground = true)
@Composable
fun PreviewUpperLimbTestsScreen() {
    val tests = listOf(
        Test("Shoulder Extension & Flexion", "Evaluate the range of motion of your shoulder", R.drawable.instruction4),
        Test("Shoulder Rotation", "Evaluate the internal and external rotation ability of your shoulder", R.drawable.instruction4),
        Test("Elbow Extension & Flexion", "Evaluate the range of motion of your elbow", R.drawable.instruction4),
        Test("Wrist Up & Down", "Evaluate the range of motion of your wrist", R.drawable.instruction4)
    )
    UpperLimbTestsScreen(tests)
}

