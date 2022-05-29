package com.example.cryptobiz

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Entity(tableName = "quotations")
data class QuotationEntity(
    @PrimaryKey(autoGenerate = true) var id: Int,
    @ColumnInfo(name = "datetime", defaultValue = "CURRENT_TIMESTAMP") var datetime: String,
    @ColumnInfo(name = "quoteRUB", defaultValue = "0") var quoteRUB: Int,
    @ColumnInfo(name = "quoteUSD", defaultValue = "0") var quoteUSD: Int,
    @ColumnInfo(name = "quoteEUR", defaultValue = "0") var quoteEUR: Int,
    // Int is enough because cryptocompare api provides only 5 digits after floating point
    @ColumnInfo(name = "quoteBTC", defaultValue = "0") var quoteBTC: Int
)

@Dao
interface QuotationsDao {
    @Query("SELECT * FROM quotations ORDER BY id DESC LIMIT 512")
    fun getAll(): LiveData<List<QuotationEntity>>

    @Query("INSERT INTO quotations (quoteRUB, quoteUSD, quoteEUR, quoteBTC) VALUES (:RUB, :USD, :EUR, :BTC)")
    fun insert(RUB: Int, USD: Int, EUR: Int, BTC: Int)
}

@Database(entities = [QuotationEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun quotationsDao(): QuotationsDao

    companion object {
        private var INSTANCE: AppDatabase? = null

        internal fun instance(context: Context): AppDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context, AppDatabase::class.java, "quotations.db").build()
            }
            return INSTANCE!!
        }
    }
}