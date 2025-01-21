package com.alekey.bingo.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alekey.bingo.BingoWinner

@Composable
fun WinnerDialog(
    showDialog: Boolean,
    winners: List<BingoWinner>,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = {
                Text(
                    "¡BINGO!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        "¡Se encontraron cartillas ganadoras!",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    winners.forEach { winner ->
                        Text(
                            "• Cartilla ${winner.card.cardNumber} (ID: ${winner.card.cardId}) [${winner.gameMode.label}]",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { onDismiss() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF77DD77)
                    )
                ) {
                    Text("¡Felicidades!")
                }
            }
        )
    }
}