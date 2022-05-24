package com.example.cryptobiz

import android.content.Context
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

class QuoteDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    object DBContract {
        object Entry : BaseColumns {
            const val TABLE_NAME = "quotations"
            const val COLUMN_NAME_DATETIME = "datetime"
            const val COLUMN_NAME_
        }
    }
}