package net.gsantner.markor.data.local.db

import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.NSHomeDirectory

fun getNoteMetadataDatabaseBuilder(): RoomDatabase.Builder<NoteMetadataDatabase> {
    val dbFilePath = NSHomeDirectory() + "/note_metadata.db"
    return Room.databaseBuilder<NoteMetadataDatabase>(
        name = dbFilePath
    )
}
