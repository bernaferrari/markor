package com.bernaferrari.remarkor.data.local.db

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

fun getNoteMetadataDatabaseBuilder(): RoomDatabase.Builder<NoteMetadataDatabase> {
    val baseDir = File(System.getProperty("user.home"), ".markor")
    if (!baseDir.exists()) {
        baseDir.mkdirs()
    }
    val dbFile = File(baseDir, "markor_notes.db")
    return Room.databaseBuilder(
        name = dbFile.absolutePath
    )
}
