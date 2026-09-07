package com.example.data.db

import androidx.room.*
import com.example.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE date LIKE :yearMonthPrefix || '%' ORDER BY date DESC, id DESC")
    fun getTransactionsByMonth(yearMonthPrefix: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE date LIKE :yearPrefix || '%' ORDER BY date DESC, id DESC")
    fun getTransactionsByYear(yearPrefix: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("DELETE FROM transactions WHERE date LIKE :yearMonthPrefix || '%'")
    suspend fun deleteTransactionsByMonth(yearMonthPrefix: String)

    @Query("DELETE FROM transactions WHERE isSample = 1")
    suspend fun deleteSampleTransactions()

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    @Query("SELECT DISTINCT title FROM transactions WHERE type = :type ORDER BY id DESC LIMIT 15")
    fun getRecentTransactionTitles(type: String): Flow<List<String>>

    @Query("SELECT * FROM transactions WHERE isFavorite = 1 ORDER BY title ASC")
    fun getFavoriteTransactions(): Flow<List<TransactionEntity>>
}
