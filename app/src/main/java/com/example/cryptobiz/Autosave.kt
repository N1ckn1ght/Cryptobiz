package com.example.cryptobiz

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.gson.Gson
import okhttp3.*
import okio.IOException

class Autosave(private val appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val quotes = getDataSilent()
        if (quotes.RUB == -1.0) {
            return Result.retry()
        }
        val data = workDataOf(
            "RUB" to quotes.RUB,
            "USD" to quotes.USD,
            "EUR" to quotes.EUR,
            "BTC" to quotes.BTC
        )
        return Result.success(data)
    }

    private fun getDataSilent(): QuotesJSON {
        val client = OkHttpClient()
        var data = QuotesJSON(-1.0, 0.0, 0.0, 0.0)

        val request = Request.Builder()
            .url(
                "https://min-api.cryptocompare.com/data/price?fsym=${appContext.getString(R.string.currency)}&tsyms=RUB,USD,EUR,BTC&extraParams=${
                    appContext.getString(
                        R.string.app_name
                    )
                }"
            )
            .addHeader("Apikey", appContext.getString(R.string.api_key))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("d/situation", "connection failure")
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