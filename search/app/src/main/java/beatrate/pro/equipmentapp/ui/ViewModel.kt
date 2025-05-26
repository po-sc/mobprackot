package beatrate.pro.equipmentapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import beatrate.pro.equipmentapp.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/* ---------- UI-state ---------- */
sealed interface EquipmentUiState {
    object Loading : EquipmentUiState
    data class Success(val list: List<EquipmentItem>) : EquipmentUiState
    data class Error(val message: String) : EquipmentUiState
}

/* ---------- ViewModel ---------- */
class EquipmentViewModel @JvmOverloads constructor(   // ← аннотация стоит здесь!
    app: Application,
    private val repo: EquipmentRepository = EquipmentRepository()
) : AndroidViewModel(app) {

    /* текст поиска */
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    /* история */
    private val _history = MutableStateFlow(
        SearchHistoryManager.getHistory(app)
    )
    val history: StateFlow<List<String>> = _history.asStateFlow()

    /* загрузка / ошибка */
    private val _ui = MutableStateFlow<EquipmentUiState>(EquipmentUiState.Loading)
    val ui: StateFlow<EquipmentUiState> = _ui.asStateFlow()

    /* отфильтрованный список */
    val filtered: StateFlow<List<EquipmentItem>> = combine(_query, _ui) { q, state ->
        if (state is EquipmentUiState.Success) {
            if (q.isBlank()) state.list
            else state.list.filter { it.name.contains(q, true) }
        } else emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init { refresh() }

    /* --- публичные методы --- */

    fun onQueryChange(text: String) { _query.value = text }

    /** сохранить запрос в историю при нажатии Search */
    fun commitQuery() {
        val q = _query.value.trim()
        if (q.isBlank()) return
        SearchHistoryManager.addQuery(getApplication(), q)
        _history.value = SearchHistoryManager.getHistory(getApplication())
    }

    fun selectHistoryItem(q: String) { _query.value = q }

    fun clearHistory() {
        SearchHistoryManager.clear(getApplication())
        _history.value = emptyList()
    }

    fun refresh() = viewModelScope.launch {
        _ui.value = EquipmentUiState.Loading
        runCatching { repo.fetchEquipment() }
            .onSuccess { _ui.value = EquipmentUiState.Success(it) }
            .onFailure { _ui.value = EquipmentUiState.Error(it.localizedMessage ?: "Unknown error") }
    }
}