package com.anikaitgupta.habitverse.domain

import com.anikaitgupta.habitverse.data.db.HabitLog
import com.anikaitgupta.habitverse.data.db.HabitWithLogs
import com.anikaitgupta.habitverse.data.network.GeminiResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class HabitUseCase @Inject constructor(private val habitRepository: HabitRepository) {
    suspend fun insertHabit(habitDomainModel: HabitDomainModel): Long{
        return habitRepository.insertHabit(habitDomainModel)
    }
    suspend fun deleteHabit(habitDomainModel: HabitDomainModel){
        habitRepository.deleteHabit(habitDomainModel)
    }
    suspend fun editHabit(habitDomainModel: HabitDomainModel){
        habitRepository.editHabit(habitDomainModel)
    }
    /*fun getAllHabits(): Flow<List<HabitDomainModel>>{
        return habitRepository.getAllHabits()
    }
    fun getHabitsById(id: Int): Flow<HabitDomainModel>{
        return habitRepository.getHabitsById(id)
    }*/
     fun getAllHabitsWithLogs(): Flow<List<HabitWithLogs>> {
        //TODO("Not yet implemented")
        return habitRepository.getAllHabitsWithLogs()
    }

     suspend fun insertLog(log: HabitLog) {
         val thresholdDate = LocalDate.now().minusDays(14).toString()
        habitRepository.insertLog(log, thresholdDate)
    }

     suspend fun deleteLog(habitId: Long, date: String) {
        //TODO("Not yet implemented)
        habitRepository.deleteLog(habitId,date)
    }

     suspend fun getHabitWithLogsById(id: Long): HabitWithLogs? {
        //TODO("Not yet implemented")
        return habitRepository.getHabitWithLogsById(id)
    }
     /*fun getCountOfLogsCompletedIn7Days(
        currentDate: String,
        date7DaysBack: String
    ): Flow<Long> {
        return habitRepository.getCountOfLogsCompletedIn7Days(currentDate,date7DaysBack)
    }

     fun getCountOfLogsCompletedInLast7Days(
        date7DaysBack: String,
        date14DaysBack: String
    ): Flow<Long> {
        return habitRepository.getCountOfLogsCompletedInLast7Days(date7DaysBack,date14DaysBack)
    }

     fun getTotalPossibleCompletionsInLast7Days(currentDate: String): Flow<Long> {
        return habitRepository.getTotalPossibleCompletionsInLast7Days(currentDate)
    }

     fun getTotalPossibleCompletionsInLast7To14Days(date7DaysBack: String): Flow<Long> {
        return habitRepository.getTotalPossibleCompletionsInLast7To14Days(date7DaysBack)
    }*/
     fun getAnalyticsData(): Flow<HabitAnalyticsData> = combine(
         habitRepository.getCountOfLogsCompletedIn7Days(
             LocalDate.now().toString(),
             LocalDate.now().minusDays(6).toString()
         ),
         habitRepository.getCountOfLogsCompletedInLast7Days(
             LocalDate.now().minusDays(7).toString(),
             LocalDate.now().minusDays(13).toString()
         ),
         habitRepository.getTotalPossibleCompletionsInLast7Days(
             LocalDate.now().toString()
         ),
         habitRepository.getTotalPossibleCompletionsInLast7To14Days(
             LocalDate.now().minusDays(7).toString()
         )
     ) { logsCurr, logsPrev, totalCurr, totalPrev ->

         // Do your calculations here
         val completionRateThisWeek = if (totalCurr > 0) (logsCurr.toDouble() / totalCurr)*100 else 0.0
         val completionRateLastWeek = if (totalPrev > 0) (logsPrev.toDouble() / totalPrev)*100 else 0.0
         val trend = completionRateThisWeek - completionRateLastWeek

         HabitAnalyticsData(
             logCountThisWeek = logsCurr,
             logCountLastWeek = logsPrev,
             totalPossibleThisWeek = totalCurr,
             totalPossibleLastWeek = totalPrev,
             completionRateThisWeek = completionRateThisWeek,
             completionRateLastWeek = completionRateLastWeek,
             trend = trend
         )
     }
     fun getRecoveryRate(): Flow<Double> {
        //return habitRepository.getAllHabitsWithLogsOrdered()
        val recoveryRate=habitRepository.getAllHabitsWithLogsOrdered().map {list->
            list.map{habitWithLogs ->
                habitWithLogs.copy(
                    logs=habitWithLogs.logs.sortedBy{
                        it.completionDate
                    }
                )
            }
        }.map { list->
            calculateRecoveryRate(list)
        }
         return recoveryRate
    }
    fun calculateRecoveryRate(habitsWithLogs: List<HabitWithLogs>): Double{
        var totalNumberOfGaps:Long =0
        var sumOfAllGaps:Long =0
        for(habitWithLogs in habitsWithLogs){
            var habitLogs=habitWithLogs.logs
            for(i in 0 until habitLogs.size-1){
                val current = LocalDate.parse(habitLogs[i].completionDate)
                val next= LocalDate.parse(habitLogs[i+1].completionDate)
                val daysDifference = ChronoUnit.DAYS.between(current, next) // returns Long
                if(daysDifference>1){
                    sumOfAllGaps+=daysDifference
                    totalNumberOfGaps++
                }
            }
        }
        if(totalNumberOfGaps==0L){
            return 0.0
        }
        return (sumOfAllGaps.toDouble()/totalNumberOfGaps.toDouble())
    }
    fun calculateNumberOfHabitsWithStreak():Flow<Long>{
        val date3DaysBack = LocalDate.now().minusDays(2).toString() // -2 because today counts as day 1
        val today = LocalDate.now().toString()
        val streakCount=habitRepository.getAllHabitsWithLogsOrdered().map { list->
            list.map{habitWithLogs ->
                habitWithLogs.copy(
                    logs=habitWithLogs.logs.filter{log->
                        log.completionDate in date3DaysBack..today
                    }
                )
            }
        }.map{list->
            streakHelper(list)
        }
        return streakCount
    }
    fun streakHelper(habitsWithLogs: List<HabitWithLogs>):Long{
        var streakCtr:Long = 0L
        for(habitWithLogs in habitsWithLogs){
            if(habitWithLogs.logs.size ==3){
                streakCtr++
            }
        }
        return streakCtr
    }
    suspend fun getTipsForHabitImprovement(apiKey:String,chatMessageList: List<ChatMessage>): GeminiResult{
        return habitRepository.getTipsForHabitImprovement(apiKey,chatMessageList)
    }
}