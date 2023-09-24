package com.uva.client

import android.os.Bundle
import android.os.Process
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.uva.MessageDto
import com.uva.client.ui.theme.Harmonies
import com.uva.client.ui.theme.IPCTheme

@ExperimentalMaterial3Api
class ClientActivity : ComponentActivity() {
    private var ipc: IPCPublisher? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ipc = IPCPublisher()

        setContent {
            IPCTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        val context = LocalContext.current
                        val connected by ipc!!.connected.collectAsState()
                        if (!connected) {
                            Button(
                                modifier = Modifier.wrapContentSize().align(CenterHorizontally),
                                onClick = { ipc?.connectTwoWay(context.applicationContext) },
                            ) {
                                Text(text = "Connect")
                            }
                        }
                        val messages: List<MessageDto> by ipc!!.messages.collectAsState()

                        LazyColumn() {
                            items(messages) {
                                ChatMessageItem(it)
                            }
                        }
                        Row(
                            modifier = Modifier.weight(0.9f).padding(8.dp),
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            var text by remember { mutableStateOf("") }
                            TextField(
                                value = text,
                                onValueChange = { text = it },
                                modifier = Modifier.weight(0.9f).wrapContentHeight(),
                            )
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
    }

    @Composable
    fun ChatMessageItem(
        message: MessageDto,
        enterAnimation: AnimationSpec<Float> = spring(
            dampingRatio = Spring.DampingRatioLowBouncy, // Adjust this value for the desired bounce effect
            stiffness = Spring.StiffnessLow,
        ),
    ) {
        var showMessage by remember { mutableStateOf(false) }
        val isMyMessage by remember { mutableStateOf(message.pId == Process.myPid()) }

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

        Column() {
            Row {
                if (!isMyMessage) {
                    Spacer(modifier = Modifier.weight(0.1F))
                }
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .weight(0.9F)
                        .graphicsLayer(
                            translationY = if (isMyMessage) translationY else -translationY,
                            scaleX = scale,
                            scaleY = scale,
                            alpha = alpha,
                        )
                        .background(
                            color = if (isMyMessage) Harmonies else Color.LightGray,
                            shape = RoundedCornerShape(8.dp),
                        )
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                ) {
                    Text(
                        text = message.text,
                        modifier = Modifier.padding(16.dp, 8.dp),
                    )
                }
                if (isMyMessage) {
                    Spacer(modifier = Modifier.weight(0.1F))
                }
            }
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
