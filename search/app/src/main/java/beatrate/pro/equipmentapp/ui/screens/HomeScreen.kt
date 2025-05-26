package beatrate.pro.equipmentapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import beatrate.pro.equipmentapp.data.EquipmentItem
import beatrate.pro.equipmentapp.ui.EquipmentUiState
import beatrate.pro.equipmentapp.ui.EquipmentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    nav: NavController,
    vm: EquipmentViewModel = viewModel()
) {
    val uiState by vm.ui.collectAsState()

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Главный экран") }) }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {

            /* ---------- Горизонтальный список ---------- */
            when (uiState) {
                EquipmentUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.padding(top = 32.dp))
                }
                is EquipmentUiState.Error -> {
                    Text(
                        text = "Не удалось загрузить оборудование",
                        modifier = Modifier.padding(32.dp)
                    )
                }
                is EquipmentUiState.Success -> {
                    val list = (uiState as EquipmentUiState.Success).list
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(list, key = EquipmentItem::id) { item ->
                            MiniEquipmentCard(item) {
                                nav.navigate("equipment")      // переход на экран поиска
                            }
                        }
                    }
                }
            }

            /* ---------- Кнопка «Оборудование» (оставляем) ---------- */
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = { nav.navigate("equipment") },
                modifier = Modifier
                    .height(56.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Text("Оборудование")
            }
        }
    }
}

/* ───── маленькая карточка без подписи ───── */
@Composable
private fun MiniEquipmentCard(
    item: EquipmentItem,
    onClick: () -> Unit
) {
    Image(
        painter = rememberAsyncImagePainter(item.image),
        contentDescription = item.name,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    )
}