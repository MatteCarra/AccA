package mattecarra.accapp.utils

import android.os.Environment
import android.util.Log
import java.io.BufferedWriter
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class Logs
{
    val LOG_NONE = 0
    val LOG_CONSOLE = 1
    val LOG_FILE = 2

    val debugLoggingEnabled = LOG_CONSOLE

    fun d(logtag: String, message: String)
    {
        if (debugLoggingEnabled != LOG_NONE)
        {
            val logtag = "AccA:$logtag"

            if (debugLoggingEnabled == LOG_CONSOLE)
            {
                Log.d(logtag, message)
            }
            else if (debugLoggingEnabled == LOG_FILE)
            {
                Log.d(logtag, message)
                logToFile("[$logtag] DEBUG: $message")
            }
        }
    }

    fun w(logtag: String, message: String)
    {
        if (debugLoggingEnabled != LOG_NONE)
        {
            val logtag = "AccA:$logtag"

            if (debugLoggingEnabled == LOG_CONSOLE)
            {
                Log.w(logtag, message)
            }
            else if (debugLoggingEnabled == LOG_FILE)
            {
                Log.w(logtag, message)
                logToFile("[$logtag] WARNING: $message")
            }
        }
    }

    fun e(logtag: String, message: String)
    {
        if (debugLoggingEnabled != LOG_NONE)
        {
            val logtag = "AccA:$logtag"

            if (debugLoggingEnabled == LOG_CONSOLE)
            {
                Log.e(logtag, message)
            }
            else if (debugLoggingEnabled == LOG_FILE)
            {
                Log.e(logtag, message)
                logToFile("[$logtag] ERROR: $message")
            }
        }
    }

    fun logToFile(msg: String?)
    {
        logToFile(msg!!, "debug_log.txt")
    }

    fun logToFile(msg: String, filename: String)
    {
        if (debugLoggingEnabled == LOG_FILE)
        {
            val curtime: String = SimpleDateFormat("yyyy.MM.dd HH:mm:ss ", Locale.getDefault()).format(Date())

            try
            {
                val out = BufferedWriter(FileWriter(Environment.getExternalStorageDirectory().absolutePath + "/AccA/" + filename, true), 8192)
                out.write(curtime + msg)
                out.newLine()
                out.close()
            }
            catch (e: IOException) {}
        }
    }

}