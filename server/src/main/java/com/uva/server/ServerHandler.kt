package com.uva.server

import android.os.Handler
import android.os.Looper

abstract class ServerHandler(val looper: Looper, val string: String) : Handler(looper) { // todo naming
    abstract fun sendToAll(string: String)
}