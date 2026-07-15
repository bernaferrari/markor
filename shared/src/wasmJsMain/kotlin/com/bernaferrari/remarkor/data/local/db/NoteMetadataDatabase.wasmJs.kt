package com.bernaferrari.remarkor.data.local.db

import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.web.WebWorkerSQLiteDriver
import org.w3c.dom.Worker

/**
 * Room 3 web setup (documented in Room 3.0 release notes):
 *
 * ```
 * Room.databaseBuilder<Db>("music.db")
 *   .setDriver(WebWorkerSQLiteDriver(createWorker()))
 *   .build()
 * ```
 *
 * Worker implements the WebWorkerSQLiteDriver protocol using SQLite WASM + OPFS.
 * Requires Cross-Origin-Opener-Policy / Cross-Origin-Embedder-Policy (see
 * webApp/webpack.config.d/coop-coep.js and webApp/vercel.json).
 *
 * @see https://developer.android.com/jetpack/androidx/releases/room3#3.0.0
 * @see https://github.com/danysantiago/room-web-demo
 */
fun getNoteMetadataDatabaseBuilder(): RoomDatabase.Builder<NoteMetadataDatabase> =
    Room.databaseBuilder<NoteMetadataDatabase>(name = "markor_notes.db")

fun createWebWorkerSQLiteDriver(): WebWorkerSQLiteDriver =
    WebWorkerSQLiteDriver(createSqliteWebWorker())

/**
 * Official worker URL pattern from Room 3 docs / room-web-demo.
 * Package name must match shared/sqlite-web-worker/package.json ("sqlite-web-worker").
 */
@OptIn(ExperimentalWasmJsInterop::class)
private fun createSqliteWebWorker(): Worker =
    js("""new Worker(new URL("sqlite-web-worker/worker.js", import.meta.url))""")
