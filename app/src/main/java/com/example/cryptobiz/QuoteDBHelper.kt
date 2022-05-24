package com.example.cryptobiz

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns

class QuoteDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    object DBContract {
        object Entry : BaseColumns {
            const val TABLE_NAME = "quotations"
            const val COLUMN_NAME_DATETIME = "datetime"
            const val COLUMN_NAME_QUOTE = "quote"
            const val COLUMN_NAME_DELTA = "delta"
        }

        const val SQL_CREATE =
            "CREATE TABLE IF NOT EXISTS ${Entry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INT(10) UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT," +
                    "${Entry.COLUMN_NAME_DATETIME} DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                    "${Entry.COLUMN_NAME_QUOTE} INT(10) NOT NULL DEFAULT 0," +
                    "${Entry.COLUMN_NAME_DELTA} DOUBLE NOT NULL DEFAULT 0)"

        const val SQL_DELETE =
            "DROP TABLE IF EXISTS ${Entry.TABLE_NAME}"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(DBContract.SQL_CREATE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // TODO: needs to be implemented without data loss
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }

    fun onDelete(db: SQLiteDatabase) {
        db.execSQL(DBContract.SQL_DELETE)
    }

    companion object {
        const val DATABASE_VERSION = 0
        const val DATABASE_NAME = "records.db"
    }
}