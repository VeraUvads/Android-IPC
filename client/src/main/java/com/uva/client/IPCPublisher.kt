package com.uva.client

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.Process
import android.os.RemoteException
import android.util.Log
import com.uva.server.RemoteService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class IPCPublisher {
    private var scope: CoroutineScope? = null

    private var remoteService: RemoteService? = null

    private val connected = AtomicBoolean(false)

    private var serverMessenger: Messenger? = null
    private var clientMessenger: Messenger? = null

    private val _messages: MutableStateFlow<List<String>> = MutableStateFlow(listOf())
    val messages: StateFlow<List<String>> = _messages

    private val handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            // Update UI with remote process info
            val bundle = msg.data
            val text = bundle.getString("MESSAGE")
            val pId = bundle.getInt("PID")
            text?.let { updateMessages(it, pId) }
            Log.i("dsdsdsd", "handleMessage:$bundle")
        }
    }

    private fun updateMessages(text: String, pId: Int) {
        scope?.launch {
            val dataList = _messages.value.toMutableList()
            dataList.add(text)
            _messages.emit(dataList)
        }
    }

    private val connection = object : ServiceConnection {

        // Called when the connection with the service is established.
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Log.i("dsdsd", "onServiceConnected: ")
            connected.set(true)

            remoteService = RemoteService.Stub.asInterface(service)
            clientMessenger = Messenger(handler)

            scope = CoroutineScope(SupervisorJob())
            serverMessenger = Messenger(service)
            runCatching {
                val msg: Message = Message.obtain(
                    null,
                    123, // register client
                )
                msg.replyTo = clientMessenger
                serverMessenger?.send(msg)
            }
        }

        // Called when the connection with the service disconnects unexpectedly.
        override fun onServiceDisconnected(className: ComponentName) {
            connected.set(false)
            remoteService = null
            serverMessenger = null
            scope?.cancel()
            scope = null
        }
    }

    fun connectAidl(context: Context) {
        if (connected.get()) return
        val intent = Intent("aidl_server")
        val pack = RemoteService::class.java.`package` // todo?
        Log.i(context.packageName, "connect package $pack")
        pack?.let {
            intent.setPackage(pack.name)
            context.applicationContext.bindService(
                intent,
                connection,
                Context.BIND_AUTO_CREATE,
            )
        }
    }

    fun connectTwoWay(context: Context) {
        if (connected.get()) return
        val intent = Intent("two_way_messages")
        val pack = "com.uva.server" // todo?
        Log.i(context.packageName, "connect package $pack")
        intent.setPackage(pack)
        context.applicationContext.bindService(
            intent,
            connection,
            Context.BIND_AUTO_CREATE,
        )
    }

    fun disconnect(context: Context) {
        if (!connected.get()) return
        context.applicationContext?.unbindService(connection)
    }

    fun sendMessageToServer(text: String, context: Context) {
        require(connected.get())
        val message = Message.obtain(handler)
        val bundle = Bundle()
        updateMessages(text, Process.myPid())
        bundle.putString("MESSAGE", text)
        bundle.putInt("PID", Process.myPid())
        message.data = bundle
        try {
            serverMessenger?.send(message) // todo if
        } catch (e: RemoteException) {
            e.printStackTrace()
        } finally {
            message.recycle()
        }
    }
}
