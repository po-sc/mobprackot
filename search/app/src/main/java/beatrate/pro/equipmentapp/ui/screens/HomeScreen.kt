package beatrate.pro.equipmentapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(nav: NavController) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Главный экран") })
        }
    ) { inner ->
        Column(
            Modifier
                .padding(inner)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { nav.navigate("equipment") },
                modifier = Modifier
                    .height(56.dp)
                    .padding(16.dp)
            ) {
                Text("Оборудование")
            }
        }
    }
}