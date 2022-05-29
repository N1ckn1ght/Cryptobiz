package com.example.cryptobiz

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.example.cryptobiz.AppDatabase.Companion.instance
import com.example.cryptobiz.Operations.quotationInsert
import com.google.gson.Gson
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import okio.IOException
import java.lang.Integer.min
import java.util.concurrent.CountDownLatch

class Autosave : Service() {
    private val context: Context = this
    private val handler = Handler(Looper.getMainLooper())
    private var attempt = 0
    private var started = false

    @OptIn(DelicateCoroutinesApi::class)
    private val save = object : Runnable {
        override fun run() {
            Log.d(TAG, "autosave is on the run")
            val quotes = getDataSilent()
            if (quotes.RUB == -1.0) {
                attempt = min(attempt + 1, 1200)
                Log.d(
                    TAG,
                    "autosave has failed, retry after ${INTERVAL_RETRY * attempt / 1000} seconds"
                )
                handler.postDelayed(this, INTERVAL_RETRY * attempt)
                return
            }

            GlobalScope.launch(Dispatchers.IO) {
                quotationInsert(instance(context), quotes.RUB, quotes.USD, quotes.EUR, quotes.BTC)
            }

            attempt = 0
            Log.d(TAG, "autosave has succeeded, retry after ${INTERVAL / 1000} seconds")
            handler.postDelayed(this, INTERVAL)
        }

        private fun getDataSilent(): QuotesJSON {
            val client = OkHttpClient()
            val countDownLatch = CountDownLatch(1)
            var data = QuotesJSON(-1.0, 0.0, 0.0, 0.0)

            val request = Request.Builder()
                .url(
                    "https://min-api.cryptocompare.com/data/price?fsym=${
                        applicationContext.getString(
                            R.string.currency
                        )
                    }&tsyms=RUB,USD,EUR,BTC&extraParams=${
                        applicationContext.getString(
                            R.string.app_name
                        )
                    }"
                )
                .addHeader("Apikey", applicationContext.getString(R.string.api_key))
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.d(TAG, e.toString())
                    countDownLatch.countDown()
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    response.body?.let {
                        data = Gson().fromJson(it.string(), QuotesJSON::class.java)
                    }
                    countDownLatch.countDown()
                }
            })

            countDownLatch.await()
            return data
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (started) {
            Log.d(TAG, "found instance of a service")
        } else {
            createNotification()
            started = true
            handler.post(save)
            Log.d(TAG, "created instance of a service")
        }
        return START_STICKY
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun createNotification() {
        val notificationChannel = NotificationChannel(
            TAG,
            "Autosave Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(notificationChannel)

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        val notification: Notification = Notification.Builder(this, TAG)
            .setContentTitle(getText(R.string.notification_title))
            .setContentText(getText(R.string.notification_message))
            .setSmallIcon(android.R.drawable.sym_def_app_icon)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    companion object {
        const val TAG = "AutoSaveService"
        const val INTERVAL = 600000L
        const val INTERVAL_RETRY = 15000L

        const val NOTIFICATION_ID = 1337
    }
}