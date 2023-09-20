package com.uva.client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
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
//                    val items by remember(message?.value) {
//                        message?.value?.let { list.add(it) }
//                        mutableStateOf(list)
//                    }
//                    LazyColumn() {
//                        items(items) {
//                            Text(
//                                text = it,
//                                modifier = Modifier.border(1.dp, Color.Black),
//                            )
//                        }
//                    }
                        var text by remember { mutableStateOf("") }
                        TextField(value = text, onValueChange = { text = it })
                        Button(
                            modifier = Modifier.wrapContentSize(),
                            onClick = { ipc?.sendMessageToServer(text, context) },
                        ) {
                            Text(text = "Send")
                        }
                    }
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
