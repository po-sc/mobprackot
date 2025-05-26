package beatrate.pro.equipmentapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import beatrate.pro.equipmentapp.data.EquipmentItem
import beatrate.pro.equipmentapp.ui.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentScreen(nav: NavController, vm: EquipmentViewModel = viewModel()) {

    /* state-flows */
    val uiState by vm.ui.collectAsState()
    val query   by vm.query.collectAsState()
    val list    by vm.filtered.collectAsState()
    val history by vm.history.collectAsState()

    /* helpers */
    val kb       = LocalSoftwareKeyboardController.current
    val focusMgr = LocalFocusManager.current
    var fieldFocused by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Оборудование") },
                navigationIcon = {
                    IconButton({ nav.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { inner ->

        when (uiState) {
            EquipmentUiState.Loading -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(inner),
                Alignment.Center
            ) { CircularProgressIndicator() }

            is EquipmentUiState.Error -> {
                val msg = (uiState as EquipmentUiState.Error).msg
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(inner),
                    Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(msg)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = vm::refresh) { Text("Обновить") }
                    }
                }
            }

            is EquipmentUiState.Success -> {
                Column(
                    Modifier
                        .padding(inner)
                        .fillMaxSize()
                ) {
                    /* --- SEARCH FIELD --- */
                    OutlinedTextField(
                        value = query,
                        onValueChange = vm::onQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .onFocusChanged { fieldFocused = it.isFocused },
                        placeholder = { Text("Поиск оборудования") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = {
                                    vm.onQueryChange("")
                                    kb?.hide(); focusMgr.clearFocus()
                                }) { Icon(Icons.Default.Close, null) }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                vm.commitQuery()
                                kb?.hide(); focusMgr.clearFocus()
                            }
                        )
                    )

                    /* --- HISTORY (показываем ТОЛЬКО, когда поле в фокусе) --- */
                    if (fieldFocused && history.isNotEmpty()) {
                        LazyRow(
                            Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(history) { h ->
                                AssistChip(
                                    onClick = {
                                        vm.selectHistory(h)
                                        kb?.hide(); focusMgr.clearFocus()
                                    },
                                    label = { Text(h) }
                                )
                            }
                            item {
                                AssistChip(
                                    onClick = vm::clearHistory,
                                    label  = { Text("Очистить историю") }
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    /* --- RESULTS OR “NOT FOUND” PLACEHOLDER --- */
                    if (list.isEmpty() && query.isNotBlank()) {
                        Box(Modifier.fillMaxSize(), Alignment.Center) {
                            Text("Ничего не найдено")
                        }
                    } else {
                        LazyColumn(
                            Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp)
                        ) {
                            items(list, key = EquipmentItem::id) { item ->
                                EquipmentCard(item) {
                                    vm.addToHistory(item.name)
                                }
                                Spacer(Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ---------- CARD ---------- */
@Composable
private fun EquipmentCard(item: EquipmentItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(item.image),
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(220.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(Modifier.height(16.dp))
            Text(item.name, style = MaterialTheme.typography.titleMedium)
        }
    }
}