package com.bernaferrari.remarkor.data.local.db

import androidx.room3.Room
import androidx.room3.RoomDatabase
import platform.Foundation.NSHomeDirectory

fun getNoteMetadataDatabaseBuilder(): RoomDatabase.Builder<NoteMetadataDatabase> {
    val dbFilePath = NSHomeDirectory() + "/note_metadata.db"
    return Room.databaseBuilder<NoteMetadataDatabase>(
        name = dbFilePath
    )
}
