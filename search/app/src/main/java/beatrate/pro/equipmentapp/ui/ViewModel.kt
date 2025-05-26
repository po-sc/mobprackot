package beatrate.pro.equipmentapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import beatrate.pro.equipmentapp.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/* --- UI-state --- */
sealed interface EquipmentUiState {
    object Loading : EquipmentUiState
    data class Success(val list: List<EquipmentItem>) : EquipmentUiState
    data class Error(val msg: String) : EquipmentUiState
}

class EquipmentViewModel @JvmOverloads constructor(
    app: Application,
    private val repo: EquipmentRepository = EquipmentRepository()
) : AndroidViewModel(app) {

    private val ctx get() = getApplication<Application>()

    /* --- поиск --- */
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    /* --- история --- */
    private val _history = MutableStateFlow(SearchHistoryManager.get(ctx))
    val history: StateFlow<List<String>> = _history.asStateFlow()

    /* --- сеть / ошибки --- */
    private val _ui = MutableStateFlow<EquipmentUiState>(EquipmentUiState.Loading)
    val ui: StateFlow<EquipmentUiState> = _ui.asStateFlow()

    /* --- фильтр --- */
    val filtered: StateFlow<List<EquipmentItem>> = combine(_query, _ui) { q, state ->
        if (state is EquipmentUiState.Success) {
            if (q.isBlank()) state.list
            else state.list.filter { it.name.contains(q, true) }
        } else emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init { refresh() }

    /* -------- публичные методы -------- */
    fun onQueryChange(t: String) { _query.value = t }

    fun commitQuery() {
        val q = _query.value.trim(); if (q.isBlank()) return
        SearchHistoryManager.add(ctx, q)
        _history.value = SearchHistoryManager.get(ctx)
    }

    fun selectHistory(q: String) { _query.value = q }

    fun clearHistory() {
        SearchHistoryManager.clear(ctx)
        _history.value = emptyList()
    }

    fun refresh() = viewModelScope.launch {
        _ui.value = EquipmentUiState.Loading
        runCatching { repo.fetchEquipment() }
            .onSuccess { _ui.value = EquipmentUiState.Success(it) }
            .onFailure { _ui.value = EquipmentUiState.Error(it.localizedMessage ?: "Ошибка сети") }
    }

    /** при клике по карточке добавляем её title в историю */
    fun addToHistory(name: String) {
        SearchHistoryManager.add(ctx, name)
        _history.value = SearchHistoryManager.get(ctx)
    }
}