package com.bernaferrari.remarkor.data.local.db

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase

fun getNoteMetadataDatabaseBuilder(
    context: Context
): RoomDatabase.Builder<NoteMetadataDatabase> {
    val appContext = context.applicationContext
    val dbPath = appContext.getDatabasePath("markor_notes.db").absolutePath
    return Room.databaseBuilder(
        context = appContext,
        name = dbPath
    )
}
