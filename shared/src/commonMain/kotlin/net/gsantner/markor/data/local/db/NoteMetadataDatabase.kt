package net.gsantner.markor.data.local.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
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
