package com.example.cryptobiz

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import androidx.work.*
import com.example.cryptobiz.Converters.currencyDoubleToInt
import com.example.cryptobiz.databinding.ActivityMainBinding
import com.google.gson.Gson
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import okio.IOException
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var data: QuotesJSON
    private lateinit var db: AppDatabase
    private lateinit var buttonRefresh: Button
    private lateinit var buttonSave: Button
    private val client = OkHttpClient()

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.quote = Quotation(
            getString(R.string.currency),
            getString(R.string.loading),
            getString(R.string.loading),
            getString(R.string.loading),
            getString(R.string.loading)
        )

        val recyclerViewTable = findViewById<RecyclerView>(R.id.table)
        val tableAdapter = TableAdapter(LayoutInflater.from(this))
        recyclerViewTable.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        db = Room.databaseBuilder(this, AppDatabase::class.java, "quotations.db").build()
        buttonRefresh = findViewById(R.id.refresh)
        buttonSave = findViewById(R.id.save)

        buttonRefresh.setOnClickListener {
            Log.d("d/situation", "refresh action performed")
            it.isEnabled = false
            buttonSave.isEnabled = false
            GlobalScope.launch(Dispatchers.IO) {
                getData()
            }
        }
        buttonSave.setOnClickListener {
            Log.d("d/situation", "save action performed")
            it.isEnabled = false
            GlobalScope.launch(Dispatchers.IO) {
                quotationInsert(data.RUB, data.USD, data.EUR, data.BTC)
            }
        }

        buttonRefresh.performClick()

        db.quotationsDao().getAll().observe(this) { quotations ->
            tableAdapter.submitList(quotations)
            recyclerViewTable.adapter = tableAdapter
        }

        val constr = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val autoSaveProc = PeriodicWorkRequestBuilder<Autosave>(1, TimeUnit.HOURS)
            .setConstraints(constr)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 15, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork("Autosave", ExistingPeriodicWorkPolicy.KEEP, autoSaveProc)
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(autoSaveProc.id)
            .observe(this) {
                val data = it.outputData
                GlobalScope.launch(Dispatchers.IO) {
                    quotationInsert(
                        data.getDouble("RUB", 0.0),
                        data.getDouble("USD", 0.0),
                        data.getDouble("EUR", 0.0),
                        data.getDouble("BTC", 0.0)
                    )
                }
            }
    }

    private fun quotationInsert(rub: Double, usd: Double, eur: Double, btc: Double) {
        db.quotationsDao().insert(
            currencyDoubleToInt(rub),
            currencyDoubleToInt(usd),
            currencyDoubleToInt(eur),
            currencyDoubleToInt(btc, 5)
        )
    }

    private fun getData() {
        val request = Request.Builder()
            .url(
                "https://min-api.cryptocompare.com/data/price?fsym=${getString(R.string.currency)}&tsyms=RUB,USD,EUR,BTC&extraParams=${
                    getString(
                        R.string.app_name
                    )
                }"
            )
            .addHeader("Apikey", getString(R.string.api_key))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        applicationContext, applicationContext.getString(
                            R.string.no_connection
                        ), Toast.LENGTH_SHORT
                    ).show()
                    buttonRefresh.isEnabled = true
                }
                Log.d("d/situation", "connection failure")
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                response.body?.let {
                    data = Gson().fromJson(it.string(), QuotesJSON::class.java)
                    binding.quote = Quotation(
                        getString(R.string.currency),
                        data.RUB.toString(),
                        data.USD.toString(),
                        data.EUR.toString(),
                        data.BTC.toString()
                    )
                    runOnUiThread {
                        buttonSave.isEnabled = true
                        Log.d("d/situation", "save is enabled")
                    }
                }
                runOnUiThread {
                    buttonRefresh.isEnabled = true
                    Log.d("d/situation", "refresh is enabled")
                }
            }
        })
    }
}