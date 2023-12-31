package com.uva.server

import android.app.Service
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.Process
import android.os.RemoteException
import android.util.Log
import java.util.concurrent.atomic.AtomicInteger

class MessengerIPCService : Service() {
    private val connections = AtomicInteger(0)

    private val handler: ServerHandler = object : ServerHandler(Looper.getMainLooper()) {
        private val clients = ArrayList<Messenger>()

        override fun handleMessage(msg: Message) {
            Log.i(this::class.simpleName, "received ${msg.what} ${msg.data}")
            when (msg.what) {
                123 -> {
                    clients.add(msg.replyTo)
                    sendMessage("You are registered. Hello!", msg.replyTo)
                }
                else -> {
                    val data = msg.data
                    val message = data.getString("MESSAGE")
                    val pId = data.getInt("PID")
                    Communicator.updateMessages(message.orEmpty(), pId)
                    super.handleMessage(msg)
                }
            }
        }

        override fun sendToAll(string: String) {
            clients.forEach {
                sendMessage(string, it)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getStringExtra("SEND_MESSAGE")?.let {
            handler.sendToAll(it)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun sendMessage(text: String, messenger: Messenger) {
        val message = Message.obtain(handler)
        val bundle = Bundle()
        bundle.putString("MESSAGE", text)
        bundle.putInt("PID", Process.myPid())
        message.data = bundle
        try {
            messenger.send(message) // todo if
            Communicator.updateMessages(text, Process.myPid())
        } catch (e: RemoteException) {
            e.printStackTrace()
        } finally {
            message.recycle()
        }
    }

    private val messenger = Messenger(handler)

    override fun onBind(intent: Intent?): IBinder? {
        Communicator.updateConnections(connections.incrementAndGet())
        Log.i(this::class.simpleName, "onBind: $connections")
        return messenger.binder
    }

    override fun unbindService(conn: ServiceConnection) {
        super.unbindService(conn)
        Communicator.updateConnections(connections.decrementAndGet())
    }
}
