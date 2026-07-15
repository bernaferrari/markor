package com.bernaferrari.remarkor.data.local.db

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import androidx.sqlite.SQLiteDriver
import kotlinx.coroutines.Dispatchers

@Database(
    entities = [
        NoteEntity::class,
        LabelEntity::class,
        NoteLabelCrossRef::class
    ],
    version = 1,
    exportSchema = true
)
@ConstructedBy(NoteMetadataDatabaseConstructor::class)
abstract class NoteMetadataDatabase : RoomDatabase() {
    abstract fun noteMetadataDao(): NoteMetadataDao
}

@Suppress("KotlinNoActualForExpect")
expect object NoteMetadataDatabaseConstructor : RoomDatabaseConstructor<NoteMetadataDatabase> {
    override fun initialize(): NoteMetadataDatabase
}

/**
 * Build Room 3 DB with a platform [SQLiteDriver].
 * - Android/iOS/JVM: [androidx.sqlite.driver.bundled.BundledSQLiteDriver]
 * - wasmJs: [androidx.sqlite.driver.web.WebWorkerSQLiteDriver] (see Room 3.0 web docs)
 */
fun getNoteMetadataDatabase(
    builder: RoomDatabase.Builder<NoteMetadataDatabase>,
    driver: SQLiteDriver,
): NoteMetadataDatabase {
    return builder
        .setDriver(driver)
        .setQueryCoroutineContext(Dispatchers.Default)
        .build()
}
