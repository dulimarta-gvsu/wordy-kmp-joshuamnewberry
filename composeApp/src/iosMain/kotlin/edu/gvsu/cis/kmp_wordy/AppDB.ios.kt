package edu.gvsu.cis.kmp_wordy

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDB> {
    val dbFilePath = documentDirectory() + "/my_room.db"
    return Room.databaseBuilder<AppDB>(
        name = dbFilePath,
    )
}
@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
    // a helper function to determine the directory name
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory, inDomain = NSUserDomainMask,
        appropriateForURL = null, create = true, error = null
    )
    return requireNotNull(documentDirectory?.path)
}