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
import android.os.RemoteException
import android.util.Log
import com.uva.server.RemoteService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import java.util.concurrent.atomic.AtomicBoolean

class IPCPublisher {
    private var scope: CoroutineScope? = null

    private var remoteService: RemoteService? = null

    private val connected = AtomicBoolean(false)

    private var serverMessenger: Messenger? = null
    private var clientMessenger: Messenger? = null

    private val _messages: MutableSharedFlow<String> = MutableSharedFlow()
    val messages: SharedFlow<String> = _messages

    private val handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            // Update UI with remote process info
            val bundle = msg.data
//            scope?.launch {
//                val text = bundle.getString("MESSAGE") ?: ""
//                _messages.emit(text)
//            }
            Log.i("dsdsdsd", "handleMessage:$bundle")
        }
    }
    private val connection = object : ServiceConnection {

        // Called when the connection with the service is established.
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Log.i("dsdsd", "onServiceConnected: ")
            connected.set(true)

            remoteService = RemoteService.Stub.asInterface(service)
            clientMessenger = Messenger(handler)

//            scope = CoroutineScope(SupervisorJob())
            serverMessenger = Messenger(service)
            runCatching {
                val msg: Message = Message.obtain(
                    null,
                    123,
                )
                msg.replyTo = clientMessenger
                serverMessenger?.send(msg)
            }
        }

        // Called when the connection with the service disconnects unexpectedly.
        override fun onServiceDisconnected(className: ComponentName) {
            connected.set(false)
//            remoteService = null
//            serverMessenger = null
//            scope?.cancel()
//            scope = null
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
//        bindService(
//            Intent(
//                this,
//                MessengerService.class
//            ), mConnection, Context.BIND_AUTO_CREATE
//        )
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
//        val message = Message.obtain(handler)
        val message = Message.obtain(handler)
        val bundle = Bundle()
        bundle.putString("MESSAGE", text)
//        bundle.putString("PACKAGE_NAME", "com.uva.server.RemoteService")
//        bundle.putInt("DIP", Process.myPid())
        message.data = bundle
//        message.replyTo =
//            clientMessenger // we offer our Messenger object for communication to be two-way
        try {
            serverMessenger?.send(message)
//            remoteService?.sendMessage(text)
        } catch (e: RemoteException) {
            e.printStackTrace()
        } finally {
            message.recycle()
        }
    }

//    fun getMessages(): Flow<String> {
//    }
}
