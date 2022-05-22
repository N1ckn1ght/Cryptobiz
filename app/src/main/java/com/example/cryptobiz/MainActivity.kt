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
import com.example.cryptobiz.databinding.ActivityMainBinding
import com.google.gson.Gson
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import okio.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var buttonRefresh: Button
    private lateinit var buttonSave: Button
    private val client = OkHttpClient()
    private val currency = "ETH"

    private lateinit var recyclerViewTable: RecyclerView
    private lateinit var tableAdapter: TableAdapter

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.quote = Quotation(
            currency,
            getString(R.string.loading),
            getString(R.string.loading),
            getString(R.string.loading),
            getString(R.string.loading)
        )

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
            // TODO: save quotation to a database
        }

        recyclerViewTable = findViewById(R.id.table)
        tableAdapter = TableAdapter(LayoutInflater.from(this))
        tableAdapter.submitList(mutableListOf())
        recyclerViewTable.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerViewTable.adapter = tableAdapter

        buttonRefresh.performClick()
    }

    private fun getData() {
        val request = Request.Builder()
            .url(
                "https://min-api.cryptocompare.com/data/price?fsym=${currency}&tsyms=RUB,USD,EUR,BTC&extraParams=${
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
                    val data: QuoteJSON = Gson().fromJson(it.string(), QuoteJSON::class.java)
                    binding.quote = Quotation(
                        currency,
                        data.RUB.toString(),
                        data.USD.toString(),
                        data.EUR.toString(),
                        data.BTC.toString()
                    )
                    runOnUiThread{
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