package mattecarra.accapp.utils

import android.annotation.SuppressLint
import android.os.Environment
import android.util.Log
import mattecarra.accapp.MainApplication
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class LogExt
{
    private val LOG_NONE = 0
    private val LOG_CONSOLE = 1
    private val LOG_FILE = 2
    private val debugEnabled = MainApplication.mDEBUG
    private val LOG_NAME = "debug_log.txt"

    fun s(tag: String, message: String)
    {
        log("S", tag, message)
    }

    fun d(tag: String, message: String)
    {
        log("D", tag, message)
    }

    fun w(tag: String, message: String)
    {
       log("W", tag, message)
    }

    fun e(tag: String, message: String)
    {
        log("E", tag, message)
    }

    @SuppressLint("LogNotTimber")
    private fun log(level: String, tag: String, message: String)
    {
        if (debugEnabled == LOG_NONE && level != "S") return

        when(level)
        {
            "D","S" -> Log.d("AccA:$tag", message)
            "W" -> Log.w("AccA:$tag", message)
            "E" -> Log.e("AccA:$tag", message)
        }

        if (debugEnabled == LOG_FILE) logToFile("[$level/AccA:$tag] $message")
    }

    //----------------------------------------------------------------------

    private fun logToFile(msg: String?)
    {
        logToFile(msg!!, LOG_NAME)
    }

    private fun logToFile(msg: String, filename: String)
    {
        if (debugEnabled != LOG_FILE) return

        val mTime: String = SimpleDateFormat("yyyy.MM.dd HH:mm:ss ", Locale.getDefault()).format(Date())
        val mPath = Environment.getExternalStorageDirectory().absolutePath + "/AccA/"

        try
        {
            File(mPath).mkdirs()
            val out = BufferedWriter(FileWriter(mPath+filename, true), 8192)
            out.write(mTime + msg)
            out.newLine()
            out.close()
        }
        catch (e: IOException)
        {
           // Log.e("AccA:LogExt", e.toString())
        }
    }

    //------------------------------------------------------------------------

}