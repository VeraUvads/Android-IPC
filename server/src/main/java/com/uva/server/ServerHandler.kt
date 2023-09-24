package com.uva.server

import android.os.Handler
import android.os.Looper

abstract class ServerHandler(looper: Looper) : Handler(looper) { // todo naming
    abstract fun sendToAll(string: String)
}