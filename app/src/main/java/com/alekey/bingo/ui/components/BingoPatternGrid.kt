package com.alekey.bingo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BingoPatternGrid(pattern: Array<IntArray>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
    ) {
        // Mostrar la cuadrícula del patrón
        pattern.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                row.forEach { cell ->
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(
                                color = if (cell == 1) Color(0xFF9BF6FF) else Color(0xFFEEEEEE)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (cell == 1) "X" else "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}