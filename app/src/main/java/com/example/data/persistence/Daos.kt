package com.example.data.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PrintJobDao {
    @Query("SELECT * FROM print_jobs ORDER BY timestamp DESC")
    fun getAllJobs(): Flow<List<PrintJobEntity>>

    @Query("SELECT * FROM print_jobs WHERE id = :id LIMIT 1")
    suspend fun getJobById(id: String): PrintJobEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(job: PrintJobEntity)

    @Update
    suspend fun update(job: PrintJobEntity)

    @Query("DELETE FROM print_jobs WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM print_jobs")
    suspend fun clearAll()
}

@Dao
interface SavedPcDao {
    @Query("SELECT * FROM saved_pc_servers ORDER BY lastConnected DESC")
    fun getAllSavedPcs(): Flow<List<SavedPcEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pc: SavedPcEntity)

    @Query("DELETE FROM saved_pc_servers WHERE id = :id")
    suspend fun deleteById(id: Int)
}
