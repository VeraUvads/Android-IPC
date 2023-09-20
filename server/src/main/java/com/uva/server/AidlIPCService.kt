package com.uva.server

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Process
import android.util.Log
import java.util.concurrent.atomic.AtomicInteger

class AidlIPCService : Service() {
    private val connections = AtomicInteger(0)

    override fun onBind(intent: Intent?): IBinder? {
        connections.incrementAndGet()
        Log.i(this::class.simpleName, "onBind: $connections")
        return binder
    }

    private val binder = object : RemoteService.Stub() {
        override fun getPid(): Int {
            return Process.myPid()
        }

        override fun doSmth(): Int {
            Log.i(this::class.simpleName, "I am doing smth")
            return connections.get()
        }

        override fun sendMessage(message: String?) {
            Log.i(this::class.simpleName, "received $message")
        }
    }
}
