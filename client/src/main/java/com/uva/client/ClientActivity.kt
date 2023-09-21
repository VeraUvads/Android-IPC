package com.uva.client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.uva.client.ui.theme.Harmonies
import com.uva.client.ui.theme.IPCTheme

@ExperimentalMaterial3Api
class ClientActivity : ComponentActivity() {
    private var ipc: IPCPublisher? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ipc = IPCPublisher()
//        ipc!!.connect(this)
//        val list = mutableListOf<String>()
//        lifecycleScope.launch {
//            ipc!!.messages
//                .collect {
//                    list.add(it)
//                }
//        }

        setContent {
            IPCTheme {
//                val message = ipc?.messages?.collectAsState(initial = "Test")
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Column {
                        val context = LocalContext.current
                        Button(
                            modifier = Modifier.wrapContentSize(),
                            onClick = { ipc?.connectTwoWay(context.applicationContext) },
                        ) {
                            Text(text = "Connect")
                        }
                        val messages: List<String> by ipc!!.messages.collectAsState()

                        LazyColumn() {
                            items(messages) {
                                ChatMessageItem(it)
                            }
                        }
                        var text by remember { mutableStateOf("") }
                        TextField(value = text, onValueChange = { text = it })
                        Button(
                            modifier = Modifier.wrapContentSize(),
                            onClick = {
                                ipc?.sendMessageToServer(text, context)
                                text = ""
                            },
                        ) {
                            Text(text = "Send")
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ChatMessageItem(
        message: String,
        enterAnimation: AnimationSpec<Float> = spring(
            dampingRatio = Spring.DampingRatioLowBouncy, // Adjust this value for the desired bounce effect
            stiffness = Spring.StiffnessLow,
        ),
    ) {
        var showMessage by remember { mutableStateOf(false) }

        LaunchedEffect(message) {
            showMessage = true
        }

        val alpha by animateFloatAsState(
            targetValue = if (showMessage) 1f else 0f,
            animationSpec = enterAnimation,
            label = "",
        )
        val translationY by animateFloatAsState(
            targetValue = if (showMessage) 0f else 100f, // Adjust the value for the desired vertical pop-up distance
            animationSpec = enterAnimation,
            label = "",
        )
        val scale by animateFloatAsState(
            targetValue = if (showMessage) 1f else 0f,
            animationSpec = enterAnimation,
            label = "",
        )
        Row(
            modifier = Modifier
                .padding(8.dp)
                .graphicsLayer(
                    translationY = if (true) translationY else -translationY,
                    scaleX = scale,
                    scaleY = scale,
                    alpha = alpha,
                )
                .background(
                    color = if (true) Harmonies else Color.LightGray,
                    shape = RoundedCornerShape(8.dp),
                )
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
        ) {
            Text(
                text = message,
                modifier = Modifier.padding(16.dp, 8.dp),
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ipc?.disconnect(this)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    IPCTheme {
        Greeting("Android")
    }
}
