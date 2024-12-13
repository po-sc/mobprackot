package ru.mirea.prac9

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudentInfoScreen()
        }
    }
}

@Composable
fun StudentInfoScreen() {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
                .background(Color(0xFF81C784), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Шкляр Пётр Львович",
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
                .background(Color(0xFFE57373), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "ИКБО-35-22",
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StudentInfoScreenPreview() {
    StudentInfoScreen()
}