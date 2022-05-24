package com.example.cryptobiz

import androidx.room.*

@Entity(tableName = "records")
data class RecordEntity(
    @PrimaryKey(autoGenerate = true) var id: Int,
    @ColumnInfo(name = "datetime") var datetime: String,
    @ColumnInfo(name = "quote") var quote: Int,
    @ColumnInfo(name = "delta") var delta: Double
)

@Dao
interface RecordsDao {
   @Query("SELECT * FROM records ORDER BY id DESC")
   fun getAll(): List<RecordEntity>
   @Insert
   fun insert(vararg record: RecordEntity)
   @Delete
   fun delete(record: RecordEntity)
   @Update
   fun update(vararg record: RecordEntity)
}