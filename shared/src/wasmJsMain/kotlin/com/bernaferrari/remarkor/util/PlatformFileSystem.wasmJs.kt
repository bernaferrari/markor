package com.bernaferrari.remarkor.util

import com.bernaferrari.remarkor.data.local.BrowserStorage
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.Buffer
import okio.FileMetadata
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.Sink
import okio.Source
import okio.Timeout
import okio.fakefilesystem.FakeFileSystem

/**
 * Browser notebook filesystem: FakeFileSystem + localStorage snapshot so notes
 * survive refresh. Room 3 (WebWorkerSQLiteDriver / OPFS) owns structured metadata.
 */
@Serializable
private data class FsSnapshot(
    val directories: Set<String> = emptySet(),
    val files: Map<String, String> = emptyMap(),
)

private const val FS_KEY = "markor.web.fs.v1"
private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

private fun normalize(path: Path): String {
    val s = path.toString().replace('\\', '/')
    return when {
        s.isEmpty() || s == "." -> "/"
        s.startsWith("/") -> s.trimEnd('/').ifEmpty { "/" }
        else -> "/${s.trimEnd('/')}"
    }
}

private fun loadOrSeed(): FakeFileSystem {
    val fake = FakeFileSystem()
    val raw = BrowserStorage.getString(FS_KEY)
    val snapshot = if (raw.isNullOrBlank()) {
        null
    } else {
        runCatching { json.decodeFromString<FsSnapshot>(raw) }.getOrNull()
    }
    if (snapshot == null || (snapshot.files.isEmpty() && snapshot.directories.isEmpty())) {
        fake.createDirectories("/Notebook".toPath())
        persist(fake)
    } else {
        snapshot.directories.sortedBy { it.count { c -> c == '/' } }.forEach { dir ->
            runCatching { fake.createDirectories(dir.toPath()) }
        }
        snapshot.files.forEach { (path, content) ->
            val p = path.toPath()
            p.parent?.let { runCatching { fake.createDirectories(it) } }
            fake.write(p) { writeUtf8(content) }
        }
    }
    return fake
}

private fun FakeFileSystem.collectSnapshot(): FsSnapshot {
    val directories = linkedSetOf<String>()
    val files = linkedMapOf<String, String>()

    fun walk(dir: Path) {
        val dirKey = normalize(dir)
        if (dirKey != "/") directories.add(dirKey)
        for (child in list(dir)) {
            val meta = metadata(child)
            if (meta.isDirectory) {
                walk(child)
            } else {
                files[normalize(child)] = read(child) { readUtf8() }
            }
        }
    }

    val root = "/".toPath()
    if (exists(root)) {
        walk(root)
    }
    return FsSnapshot(directories = directories, files = files)
}

private fun persist(fs: FakeFileSystem) {
    runCatching {
        BrowserStorage.setString(FS_KEY, json.encodeToString(fs.collectSnapshot()))
    }
}

private class SavingSink(
    private val delegate: Sink,
    private val onClose: () -> Unit,
) : Sink {
    override fun write(source: Buffer, byteCount: Long) {
        delegate.write(source, byteCount)
    }

    override fun flush() {
        delegate.flush()
    }

    override fun timeout(): Timeout = delegate.timeout()

    override fun close() {
        delegate.close()
        onClose()
    }
}

private class PersistingFileSystem(
    private val fake: FakeFileSystem,
) : FileSystem() {
    private fun save() = persist(fake)

    override fun canonicalize(path: Path): Path = fake.canonicalize(path)
    override fun metadataOrNull(path: Path): FileMetadata? = fake.metadataOrNull(path)
    override fun list(dir: Path): List<Path> = fake.list(dir)
    override fun listOrNull(dir: Path): List<Path>? = fake.listOrNull(dir)
    override fun openReadOnly(file: Path) = fake.openReadOnly(file)
    override fun openReadWrite(file: Path, mustCreate: Boolean, mustExist: Boolean) =
        fake.openReadWrite(file, mustCreate, mustExist)

    override fun source(file: Path): Source = fake.source(file)

    override fun sink(file: Path, mustCreate: Boolean): Sink =
        SavingSink(fake.sink(file, mustCreate)) { save() }

    override fun appendingSink(file: Path, mustExist: Boolean): Sink =
        SavingSink(fake.appendingSink(file, mustExist)) { save() }

    override fun createDirectory(dir: Path, mustCreate: Boolean) {
        fake.createDirectory(dir, mustCreate)
        save()
    }

    override fun atomicMove(source: Path, target: Path) {
        fake.atomicMove(source, target)
        save()
    }

    override fun delete(path: Path, mustExist: Boolean) {
        fake.delete(path, mustExist)
        save()
    }

    override fun createSymlink(source: Path, target: Path) {
        fake.createSymlink(source, target)
        save()
    }
}

actual val platformFileSystem: FileSystem = PersistingFileSystem(loadOrSeed())
