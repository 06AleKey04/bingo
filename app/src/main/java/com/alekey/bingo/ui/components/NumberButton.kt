package com.alekey.bingo.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NumberButton(
    number: Int,
    isSelected: Boolean,
    onNumberClick: () -> Unit
) {
    Button(
        onClick = onNumberClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF77DD77) else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier
            .size(36.dp)
            .padding(1.dp),
        contentPadding = PaddingValues(1.dp)
    ) {
        Text(
            text = number.toString(),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}