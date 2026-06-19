package com.bernaferrari.remarkor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bernaferrari.remarkor.domain.repository.ISettingsRepository
import com.bernaferrari.remarkor.domain.usecase.InitializeNotebookUseCase
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class IntroViewModel(
    private val settingsRepository: ISettingsRepository,
    private val initializeNotebookUseCase: InitializeNotebookUseCase,
) : ViewModel() {

    val isFirstRun = settingsRepository.isFirstRun

    suspend fun setStorageMode(isExternal: Boolean, internalPath: String) {
        initializeNotebookUseCase(isExternal, internalPath)
    }

    fun markIntroSeen() {
        viewModelScope.launch {
            settingsRepository.setFirstRun(false)
        }
    }
}