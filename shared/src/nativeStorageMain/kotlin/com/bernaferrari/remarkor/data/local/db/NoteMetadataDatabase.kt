package com.bernaferrari.remarkor.data.local.db

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
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

fun getNoteMetadataDatabase(
    builder: RoomDatabase.Builder<NoteMetadataDatabase>
): NoteMetadataDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.Default)
        .build()
}
