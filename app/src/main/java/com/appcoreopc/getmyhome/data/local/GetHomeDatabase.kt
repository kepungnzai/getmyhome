package com.appcoreopc.getmyhome.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [AnalysisHistory::class], version = 1)
abstract class GetHomeDatabase : RoomDatabase() {
    abstract fun analysisHistoryDao(): AnalysisHistoryDao
}
