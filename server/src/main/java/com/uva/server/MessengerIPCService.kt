package com.uva.server

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.util.Log
import java.util.concurrent.atomic.AtomicInteger

class MessengerIPCService : Service() {
    private val connections = AtomicInteger(0)
    private val messenger = Messenger(IncomingHandler())

    override fun onBind(intent: Intent?): IBinder? {
        connections.incrementAndGet()
        Log.i(this::class.simpleName, "onBind: $connections")
        return messenger.binder
    }

    class IncomingHandler : Handler() {
        private val clients = ArrayList<Messenger>()

        override fun handleMessage(msg: Message) {
            Log.i(this::class.simpleName, "received ${msg.what} ${msg.data}")
            when (msg.what) {
                123 -> clients.add(msg.replyTo)
                else -> super.handleMessage(msg)
            }
        }
    }
}
