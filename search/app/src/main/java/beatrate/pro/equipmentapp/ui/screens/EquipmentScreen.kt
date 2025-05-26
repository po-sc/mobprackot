package beatrate.pro.equipmentapp.ui.screens       // ← ваш package

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import beatrate.pro.equipmentapp.data.EquipmentItem
import beatrate.pro.equipmentapp.ui.EquipmentUiState
import beatrate.pro.equipmentapp.ui.EquipmentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentScreen(
    nav: NavController,
    vm: EquipmentViewModel = viewModel()
) {
    val uiState by vm.ui.collectAsState()
    val query   by vm.query.collectAsState()
    val list    by vm.filtered.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Оборудование") },
                navigationIcon = {
                    IconButton(onClick = { nav.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { inner ->
        when (uiState) {
            EquipmentUiState.Loading ->
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(inner),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

            is EquipmentUiState.Error -> {
                val msg = (uiState as EquipmentUiState.Error).message
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(inner),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Ошибка: $msg")
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = vm::refresh) { Text("Повторить") }
                    }
                }
            }

            is EquipmentUiState.Success -> {
                Column(
                    Modifier
                        .padding(inner)
                        .fillMaxSize()
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = vm::onQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        placeholder = { Text("Поиск оборудования") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search)
                    )

                    LazyColumn(
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        items(list, key = EquipmentItem::id) { item ->
                            EquipmentCard(item)
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

/* ---------- Карточка одного устройства ---------- */
@Composable
private fun EquipmentCard(item: EquipmentItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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