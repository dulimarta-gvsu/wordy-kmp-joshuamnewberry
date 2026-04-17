package edu.gvsu.cis.kmp_wordy

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDB> {
    val appContext = context.applicationContext
    val dbFilePath = appContext.getDatabasePath("my_room.db")
    return Room.databaseBuilder<AppDB>(
        context = appContext,
        name = dbFilePath.absolutePath
    )
}