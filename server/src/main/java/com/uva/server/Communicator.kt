package com.uva.server

import android.content.Context
import android.content.Intent
import android.os.Process
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

object Communicator {

    private val _messages: MutableStateFlow<List<MessageDto>> = MutableStateFlow(listOf())
    val messages: StateFlow<List<MessageDto>> = _messages

    private val _connectionCount = MutableStateFlow(0)
    val connectionCount: StateFlow<Int> = _connectionCount

    private var scope: CoroutineScope = CoroutineScope(SupervisorJob())

    fun sendMessageToAllClient(context: Context, text: String) {
        val serviceIntent = Intent(context, MessengerIPCService::class.java)
        serviceIntent.putExtra("SEND_MESSAGE", text)
        context.startService(serviceIntent)
    }

     fun updateMessages(text: String, pId: Int) = scope.launch {
        val dataList = _messages.value.toMutableList()
        dataList.add(MessageDto(text, pId))
        _messages.emit(dataList)
    }

    fun updateConnections(connections: Int) {
        scope.launch { _connectionCount.emit(connections) }
    }
}
