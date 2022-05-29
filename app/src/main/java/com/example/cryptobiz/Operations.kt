package com.example.cryptobiz

import com.example.cryptobiz.Converters.currencyDoubleToInt

object Operations {
    fun quotationInsert(db: AppDatabase, rub: Double, usd: Double, eur: Double, btc: Double) {
        db.quotationsDao().insert(
            currencyDoubleToInt(rub),
            currencyDoubleToInt(usd),
            currencyDoubleToInt(eur),
            currencyDoubleToInt(btc, 5)
        )
    }
}