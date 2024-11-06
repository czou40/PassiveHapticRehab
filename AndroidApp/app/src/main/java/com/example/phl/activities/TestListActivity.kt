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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.phl.R

import com.example.phl.activities.ui.theme.PHLTheme
import com.example.phl.utils.UnityAPI

class TestListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PreviewUpperLimbTestsScreen()
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

@Composable
fun UpperLimbTestsScreen(
    tests: List<Test>
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF20A086)) // Set background color
            .padding(16.dp)
    ) {

        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Upper Limb Tests",
                style = MaterialTheme.typography.headlineMedium.copy(color = Color.White),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Spacer(Modifier.weight(1f))

            Button(onClick = {
                val intent = Intent(context, ProgressVisualizationActivity::class.java)
                context.startActivity(intent)
            }, modifier =  Modifier
                .background(Color(0xFF20A086))) {
                Text(
                    text = "Progress",
                    style = MaterialTheme.typography.headlineMedium.copy(color = Color.White)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            content = {
                items(tests) { test ->
                    Column(
                        modifier = Modifier
                            .padding(12.dp) // Add padding between items
                    ) {
                        TestCard(test)
                    }
                }
            },
        )

        Spacer(modifier = Modifier.weight(1f))

    }
}

@Composable
fun TestCard(test: Test) {
    val context = LocalContext.current // Get the local context to use for the Intent
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp),
        onClick = {
            if (test.scene != UnityAPI.Scene.None) {
                UnityAPI.launchUnityActivity(context, test.scene)
            }
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White),
        ) {
            Image(
                painter = painterResource(id = test.imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(125.dp)
                    .width(150.dp)
            )
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = test.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.Black,
                )
                Text(
                    text = test.description,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }
    }
}

data class Test(val title: String, val description: String, val imageRes: Int, val scene:UnityAPI.Scene)

@Preview(name = "10-inch Tablet Landscape", widthDp = 1200, heightDp = 750)
@Composable
fun PreviewUpperLimbTestsScreen() {
    val tests = listOf(
        Test("Shoulder Extension & Flexion", "Evaluate the range of motion of your shoulder", R.drawable.shoulder_extension_icon, UnityAPI.Scene.GAME_1),
        Test("Shoulder Rotation", "Evaluate the internal and external rotation ability of your shoulder", R.drawable.shoulder_rotation_icon, UnityAPI.Scene.GAME_2),
//        Test("Elbow Extension & Flexion", "Evaluate the range of motion of your elbow", R.drawable.elbow_extension_icon, UnityAPI.Scene.None),
        Test("Wrist Up & Down", "Evaluate the range of motion of your wrist", R.drawable.wrist_up_icon_compressed, UnityAPI.Scene.GAME_4),
//        Test("Finger-to-Nose", "Evaluate the overall coordination of your upper-limb", R.drawable.finger_to_nose_icon_compressed, UnityAPI.Scene.None),
//        Test("Finger Extension & Flexion", "Evaluate the maximum range of motion of your fingers", R.drawable.finger_extension_icon, UnityAPI.Scene.None),
//        Test("Finger Tapping - Coordination", "Evaluate the overall dexterity and coordination of your fingers", R.drawable.finger_tapping_1_icon, UnityAPI.Scene.None),
//        Test("Finger Tapping - Speed", "Evaluate the dexterity and movements of your fingers", R.drawable.finger_tapping_2_icon, UnityAPI.Scene.None),
    )


    UpperLimbTestsScreen(tests)
}

