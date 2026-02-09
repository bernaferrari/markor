package net.gsantner.markor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import net.gsantner.markor.domain.model.Document
import net.gsantner.markor.domain.repository.IDocumentRepository
import okio.Path
import okio.Path.Companion.toPath

class EditorViewModel(
    private val documentRepository: IDocumentRepository,
    private val metadataRepository: net.gsantner.markor.data.local.db.NoteMetadataRepository
) : ViewModel() {

    suspend fun loadDocument(filePath: String): Document? {
        val path = filePath.toPath()
        return documentRepository.loadDocument(path)
    }

    fun saveDocument(document: Document, content: String) {
        viewModelScope.launch {
            documentRepository.saveDocument(document, content)
        }
    }

    fun createNewDocument(path: Path, content: String = "") {
        viewModelScope.launch {
            documentRepository.createDocument(path, content)
        }
    }

    suspend fun renameDocument(document: Document, newName: String): Boolean {
        return documentRepository.renameDocument(document, newName)
    }

    fun setColor(path: String, color: Int?) {
        viewModelScope.launch {
            metadataRepository.setColor(path, color)
        }
    }
    
    fun setArchived(path: String, archived: Boolean) {
        viewModelScope.launch {
            metadataRepository.setArchived(path, archived)
        }
    }
}
