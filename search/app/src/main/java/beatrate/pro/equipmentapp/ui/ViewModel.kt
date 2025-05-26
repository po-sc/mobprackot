package beatrate.pro.equipmentapp.ui     // ← поменяйте, если у вас другой package

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import beatrate.pro.equipmentapp.data.EquipmentItem
import beatrate.pro.equipmentapp.data.EquipmentRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/* ---------- UI-состояние экрана оборудования ---------- */
sealed interface EquipmentUiState {
    object Loading : EquipmentUiState
    data class Success(val list: List<EquipmentItem>) : EquipmentUiState
    data class Error(val message: String) : EquipmentUiState
}

/* ---------- ViewModel ---------- */
class EquipmentViewModel(
    private val repo: EquipmentRepository = EquipmentRepository()
) : ViewModel() {

    /* текст в поле поиска */
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()          // <-- публичный read-only

    /* общее состояние экрана (загрузка / успех / ошибка) */
    private val _ui = MutableStateFlow<EquipmentUiState>(EquipmentUiState.Loading)
    val ui: StateFlow<EquipmentUiState> = _ui.asStateFlow()

    /* отфильтрованный список — обновляется при изменении _query или _ui */
    val filtered: StateFlow<List<EquipmentItem>> = combine(_query, _ui) { q, state ->
        if (state is EquipmentUiState.Success) {
            if (q.isBlank()) state.list
            else state.list.filter { it.name.contains(q, ignoreCase = true) }
        } else emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        refresh()   // первый запрос при создании VM
    }

    fun onQueryChange(text: String) {
        _query.value = text
    }

    fun refresh() = viewModelScope.launch {
        _ui.value = EquipmentUiState.Loading
        runCatching { repo.fetchEquipment() }
            .onSuccess { list ->
                _ui.value = EquipmentUiState.Success(list)
                Log.d("EquipmentVM", "Fetched ${list.size} items")
            }
            .onFailure { e ->
                _ui.value = EquipmentUiState.Error(e.localizedMessage ?: "Unknown error")
                Log.e("EquipmentVM", "Network / parse error", e)
            }
    }
}