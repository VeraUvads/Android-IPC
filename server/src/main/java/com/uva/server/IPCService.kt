package com.uva.server

import android.app.Service
import android.content.Intent
import android.os.*
import java.util.concurrent.atomic.AtomicInteger

class IPCService : Service() {
    private val connections = AtomicInteger(0)

    override fun onBind(intent: Intent?): IBinder? {
        connections.incrementAndGet()
        return binder
    }

    private val binder = object : RemoteService.Stub() {
        override fun getPid(): Int {
            return Process.myPid()
        }
    }
}
