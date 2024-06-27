// file: com/example/yourapp/ui/ShoulderExtensionFlexionOverlay.kt
package com.example.phl.activities.unity

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.phl.R
import com.example.phl.utils.UnityAPI
import com.unity3d.player.UnityPlayer

@Composable
fun getCurrentActivity(): Activity? {
    val context = LocalContext.current
    return context as? Activity
}
@Composable
@Preview(name = "10-inch Tablet Landscape", widthDp = 1200, heightDp = 750)
fun ShoulderExtensionFlexionOverlay() {
    var visible by remember { mutableStateOf(true) }
    val activity = getCurrentActivity()
    if (!visible) return
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .weight(7f)
                    .fillMaxHeight()
                    .background(Color(0f, 0f, 0f, 0.5f))
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_arrow_back_24),
                        contentDescription = null,
                        Modifier.size(32.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(24.dp))
                    Text(
                        text = "Shoulder Extension & Flexion Test",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    // Left Column
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(R.drawable.shoulder_up),
                            contentDescription = "Push Shoulder Up (Flexion)",
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentScale = ContentScale.FillHeight
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Push Shoulder Up\n(Flexion)",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    }
                    // Right Column
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.shoulder_back),
                            contentDescription = "Push Shoulder Back (Extension)",
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentScale = ContentScale.FillHeight,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Push Shoulder Back\n(Extension)",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(5f)
                    .fillMaxHeight()
                    .background(Color(0xFF2F8A78))
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Why Should I do this",
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "The Shoulder Flexion and Extension test evaluates your shoulder's range of motion. It helps us detect early signs of potential joint issues.",
                    color = Color.White,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
//                        UnityAPI.loadGameScene(UnityAPI.Scene.GAME_1)
                        UnityAPI.resumeGame()
                        visible = false
                              },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC167)),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Let's Begin!",
                        color = Color(0xFF5B2626),
                        fontSize = 32.sp,

                        modifier = Modifier.padding(64.dp, 16.dp)
                    )
                }
            }
        }
    }
}
