package com.anikaitgupta.habitverse.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Insert
    suspend fun insertHabit(habit: Habit): Long

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Update
    suspend fun editHabit(habit: Habit)

    @Query("select* from habits where isDeleted = 0")
    fun getAllHabits(): Flow<List<Habit>>

    @Query("select* from habits where id = :id")
    fun getHabitsById(id: Long): Flow<Habit>

    @Query("select* from habits where syncState != 'SUCCESS'")
    suspend fun getAllPendingAndFailedHabits():List<Habit>

    @Query("SELECT EXISTS(SELECT 1 FROM habits LIMIT 1)")
    suspend fun isUserTableNotEmpty(): Boolean

    @Query("DELETE FROM habits")
    suspend fun deleteAllHabits()

    @Transaction
    @Query("SELECT * FROM habits WHERE isDeleted = 0")
    fun getAllHabitsWithLogs(): Flow<List<HabitWithLogs>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: HabitLog)

    // 1. Mark a specific log as 'Pending Delete'
    // This replaces your old deleteLog query
    @Query("""
        UPDATE habit_logs 
        SET isDeleted = 1, syncState = 'PENDING' 
        WHERE habitId = :habitId AND completionDate = :date
    """)
    suspend fun deleteLog(habitId: Long, date: String)



    @Update
    suspend fun updateLog(log: HabitLog)

    @Delete
    suspend fun permanentlyDeleteLog(log: HabitLog)
    // 2. Fetch specific habit but only with non-deleted logs
    @Transaction
    @Query("SELECT * FROM habits WHERE id = :id AND isDeleted = 0 LIMIT 1")
    suspend fun getHabitWithLogsById(id: Long): HabitWithLogs?

    // 3. Methods for SyncManager to find "Dirty" data
    @Query("SELECT * FROM habit_logs WHERE syncState != 'SUCCESS'")
    suspend fun getPendingLogSyncs(): List<HabitLog>

    @Query("DELETE FROM habit_logs")
    suspend fun deleteAllHabitLogs()

    @Query("select count(*) from habit_logs where completionDate>= :date7DaysBack and completionDate<=:currentDate and isDeleted==0")
    fun getCountOfLogsCompletedIn7Days(currentDate:String,date7DaysBack: String): Flow<Long>

    @Query("select count(*) from habit_logs where completionDate>= :date14DaysBack and completionDate<=:date7DaysBack and isDeleted==0")
    fun getCountOfLogsCompletedInLast7Days(date7DaysBack: String,date14DaysBack: String): Flow<Long>


    @Query("""
    SELECT SUM(MIN(7, julianday(:currentDate) - julianday(createdAt) + 1))
    FROM habits
    WHERE isDeleted = 0
""")
    fun getTotalPossibleCompletionsInLast7Days(currentDate: String): Flow<Long>

    @Query("""
    SELECT SUM(MIN(7, julianday(:date7DaysBack) - julianday(createdAt) + 1))
    FROM habits
    WHERE isDeleted = 0
""")
    fun getTotalPossibleCompletionsInLast7To14Days(date7DaysBack: String):Flow<Long>

    @Transaction
    @Query("""
    SELECT * FROM habits 
    WHERE isDeleted = 0
    ORDER BY id
""")
    fun getAllHabitsWithLogsOrdered(): Flow<List<HabitWithLogs>>

    @Query("""
        UPDATE habit_logs 
        SET isDeleted = 1, syncState =  'PENDING' 
        WHERE completionDate < :thresholdDate AND isDeleted = 0
    """)
    suspend fun markOldLogsAsDeleted(
        thresholdDate: String
    ): Int // Returns the number of rows updated










}