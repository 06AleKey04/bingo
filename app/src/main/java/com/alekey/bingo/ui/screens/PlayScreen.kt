package com.alekey.bingo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alekey.bingo.SettingsViewModel
import com.alekey.bingo.ui.components.NumberButton
import com.alekey.bingo.ui.dialogs.ConfirmationDialog
import com.alekey.bingo.ui.dialogs.WinnerDialog

@Composable
fun PlayScreen(settingsViewModel: SettingsViewModel) {
    val selectedNumbers by settingsViewModel.selectedNumbers.collectAsState()
    val winningCards by settingsViewModel.winningCards.collectAsState()
    var showWinnerDialog by remember { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var numberToDeselect by remember { mutableStateOf<Int?>(null) }

    // Efecto para mostrar el diálogo cuando hay ganadores
    LaunchedEffect(winningCards) {
        if (winningCards.isNotEmpty()) {
            showWinnerDialog = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 15.dp, start = 8.dp, end = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Encabezados BINGO
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("B", "I", "N", "G", "O").forEach { letter ->
                Text(
                    text = letter,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
        }

        // Grid de números
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val columns = listOf(1..15, 16..30, 31..45, 46..60, 61..75)

            columns.forEach { numbers ->
                Column(
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    numbers.forEach { number ->
                        NumberButton(
                            number = number,
                            isSelected = number in selectedNumbers,
                            onNumberClick = {
                                if (number in selectedNumbers) {
                                    numberToDeselect = number
                                    showConfirmationDialog = true
                                } else {
                                    settingsViewModel.selectNumber(number)
                                }
                            }
                        )
                    }
                }
            }
        }

        // Botón de reinicio
        Button(
            onClick = { showResetDialog = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red
            ),
            modifier = Modifier
                .padding(8.dp)
                .height(40.dp)
        ) {
            Text(
                text = "Reiniciar partida",
                fontSize = 14.sp
            )
        }

        // Contador
        Text(
            text = "Números seleccionados: ${selectedNumbers.size} / 75",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }

    // Diálogos
    ConfirmationDialog(
        showDialog = showConfirmationDialog,
        onDismiss = {
            showConfirmationDialog = false
            numberToDeselect = null
        },
        onConfirm = {
            settingsViewModel.deselectNumber(numberToDeselect ?: 0)
            showConfirmationDialog = false
            numberToDeselect = null
        },
        title = "Confirmación",
        message = "¿Seguro que desea deseleccionar el número $numberToDeselect?"
    )

    ConfirmationDialog(
        showDialog = showResetDialog,
        onDismiss = { showResetDialog = false },
        onConfirm = {
            settingsViewModel.resetSelectedNumbers()
            showResetDialog = false
        },
        title = "Confirmación",
        message = "¿Seguro que desea reiniciar la partida?"
    )

    WinnerDialog(
        showDialog = showWinnerDialog && winningCards.isNotEmpty(),
        winners = winningCards,
        onDismiss = { showWinnerDialog = false }
    )
}