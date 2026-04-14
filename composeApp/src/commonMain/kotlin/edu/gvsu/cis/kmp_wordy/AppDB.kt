package edu.gvsu.cis.kmp_wordy

import androidx.room.ConstructedBy
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.Update
import androidx.room.Upsert
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDAO {
    @Insert
    suspend fun insert(gs: GameSession)

    @Update
    suspend fun modifyTable(gs: GameSession)

    @Upsert
    suspend fun someAction(gs: GameSession)

    @Delete
    suspend fun removeOne(gs: GameSession)

    @Query("DELETE FROM GameSession")
    suspend fun removeAll()

    @Query("SELECT * FROM GameSession")
    suspend fun selectAllAsList(): List<GameSession>

    @Query("SELECT * FROM GameSession")
    fun selectAll(): Flow<List<GameSession>>

    @Query("SELECT * FROM GameSession ORDER BY points DESC")
    suspend fun selectAllSortedByPoints(): List<GameSession>

    @Query("SELECT * FROM GameSession ORDER BY word ASC")
    suspend fun selectAllSortedByAlphabetical(): List<GameSession>

    @Query("SELECT * FROM GameSession ORDER BY length(word) DESC")
    suspend fun selectAllSortedByLength(): List<GameSession>

    @Query("SELECT * FROM GameSession ORDER BY round(time) ASC, numMoves DESC")
    suspend fun selectAllSortedByTimeAndMoves(): List<GameSession>
}

@Database(
    entities = [GameSession::class],
    version = 1,
    exportSchema = true
)
@ConstructedBy(MyDatabaseBuilder::class)
abstract class AppDB: RoomDatabase() {
    abstract fun getDao(): AppDAO
}

expect object MyDatabaseBuilder: RoomDatabaseConstructor<AppDB> {
    override fun initialize(): AppDB
}

fun getDatabaseInstance(builder: RoomDatabase.Builder<AppDB>): AppDB {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
