package com.example.cryptobiz

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.example.cryptobiz.AppDatabase.Companion.instance
import com.example.cryptobiz.Operations.quotationInsert
import com.google.gson.Gson
import okhttp3.*
import okio.IOException
import java.lang.Long.min

class Autosave : Service() {
    private val db = instance(this)
    private val handler = Handler(Looper.getMainLooper())
    private var attempt = 0

    private val save = object : Runnable {
        override fun run() {
            val quotes = getDataSilent()
            if (quotes.RUB == -1.0) {
                attempt += 1
                handler.postDelayed(this, min(INTERVAL, INTERVAL_RETRY * attempt))
                return
            }
            quotationInsert(db, quotes.RUB, quotes.USD, quotes.EUR, quotes.BTC)
            attempt = 0
            handler.postDelayed(this, INTERVAL)
        }

        private fun getDataSilent(): QuotesJSON {
            val client = OkHttpClient()
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
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    response.body?.let {
                        data = Gson().fromJson(it.string(), QuotesJSON::class.java)
                    }
                }
            })

            return data
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        handler.post(save)

        return START_STICKY
    }

    companion object {
        const val TAG = "AutoSaveService"
        const val INTERVAL = 1800000L
        const val INTERVAL_RETRY = 15000L
    }
}