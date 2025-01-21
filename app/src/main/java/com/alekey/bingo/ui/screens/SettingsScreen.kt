package com.alekey.bingo.ui.screens

import androidx.compose.animation.animateColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alekey.bingo.BingoGameMode
import com.alekey.bingo.BingoValidationError
import com.alekey.bingo.SettingsViewModel
import com.alekey.bingo.ui.components.BingoCell
import com.alekey.bingo.ui.components.BingoPatternGrid

@Composable
fun SettingsScreen(settingsViewModel: SettingsViewModel) {
    Column {
        val numberCards by settingsViewModel.numberCards.collectAsStateWithLifecycle()
        val displayNumber by settingsViewModel.displayNumber.collectAsStateWithLifecycle()
        val isSynced by settingsViewModel.isSynced.collectAsStateWithLifecycle()
        val bingoCards by settingsViewModel.bingoCards.collectAsStateWithLifecycle()
        val selectedCardIndex by settingsViewModel.selectedCardIndex.collectAsStateWithLifecycle()
        val tempCardNumbers by settingsViewModel.tempCardNumbers.collectAsStateWithLifecycle()
        var showValidationDialog by remember { mutableStateOf(false) }
        var validationErrors by remember { mutableStateOf<List<BingoValidationError>>(emptyList()) }

        val infiniteTransition = rememberInfiniteTransition(label = "")
        val alpha = if (!isSynced) {
            infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 0.3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(500),
                    repeatMode = RepeatMode.Reverse
                ), label = ""
            ).value
        } else {
            1f
        }

        val indicatorColor by animateColorAsState(
            if (isSynced) Color.Green else Color.Red,
            label = ""
        )

        val selectedNumbers by settingsViewModel.selectedNumbers.collectAsStateWithLifecycle()
        val gameMode by settingsViewModel.gameMode.collectAsStateWithLifecycle()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Cantidad de tarjetas",
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = displayNumber,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            settingsViewModel.updateDisplayNumber(newValue)
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .width(100.dp)
                        .height(50.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = indicatorColor.copy(alpha = alpha),
                        unfocusedBorderColor = indicatorColor.copy(alpha = alpha)
                    )
                )
                Button(
                    onClick = { settingsViewModel.syncNumbers() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSystemInDarkTheme()) Color(0xFFFFCE63) else Color(0xFF10316B)
                    )
                ) {
                    Icon(
                        Icons.Default.Sync,
                        contentDescription = "Actualizar",
                        modifier = Modifier.size(24.dp),
                        tint = if (isSystemInDarkTheme()) Color.Black else Color.White
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    var expanded by remember { mutableStateOf(false) }

                    OutlinedTextField(
                        value = if (bingoCards.isNotEmpty())
                            "Tarjeta ${selectedCardIndex + 1}: ${bingoCards[selectedCardIndex].cardId}"
                        else "Seleccione una tarjeta",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Número de tarjeta") },
                        trailingIcon = {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Filled.KeyboardArrowDown, "Mostrar tarjetas")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        bingoCards.forEachIndexed { index, card ->
                            DropdownMenuItem(
                                text = {
                                    Text("Tarjeta ${index + 1}: ${card.cardId}")
                                },
                                onClick = {
                                    settingsViewModel.setSelectedCard(index)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = if (bingoCards.isNotEmpty()) bingoCards[selectedCardIndex].cardId else "",
                    onValueChange = { newId ->
                        settingsViewModel.updateCardId(selectedCardIndex + 1, newId)
                    },
                    label = { Text("ID de tarjeta") },
                    modifier = Modifier.weight(1f)
                )
            }

            val hasUnsavedChanges by settingsViewModel.hasUnsavedChanges.collectAsStateWithLifecycle()

            Button(
                onClick = {
                    val errors = settingsViewModel.tryToSaveCardNumbers()
                    if (errors.isNotEmpty()) {
                        validationErrors = errors
                        showValidationDialog = true
                    }
                },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (hasUnsavedChanges) {
                        val infiniteTransition = rememberInfiniteTransition(label = "")
                        val color by infiniteTransition.animateColor(
                            initialValue = Color.Red,
                            targetValue = Color(0xFFFF6B6B),
                            animationSpec = infiniteRepeatable(
                                animation = tween(500),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = ""
                        )
                        color
                    } else {
                        if (isSystemInDarkTheme()) Color(0xFFFFCE63) else Color(0xFF10316B)
                    }
                )
            ) {
                Text(
                    "Actualizar Tarjeta",
                    color = if (hasUnsavedChanges || !isSystemInDarkTheme()) Color.White else Color.Black
                )
            }

            if (showValidationDialog) {
                AlertDialog(
                    onDismissRequest = { showValidationDialog = false },
                    title = { Text("Error de Validación") },
                    text = {
                        Column {
                            Text("Se encontraron los siguientes errores:")
                            Spacer(modifier = Modifier.height(8.dp))
                            validationErrors.forEach { error ->
                                val errorMessage = when (error) {
                                    is BingoValidationError.EmptyCell -> {
                                        val col = error.position / 5
                                        val row = error.position % 5
                                        val letra = when(col) {
                                            0 -> "B"
                                            1 -> "I"
                                            2 -> "N"
                                            3 -> "G"
                                            else -> "O"
                                        }
                                        "• Celda vacía en Columna $letra, Fila ${row + 1}"
                                    }
                                    is BingoValidationError.InvalidRange -> {
                                        val col = error.position / 5
                                        val letra = when(col) {
                                            0 -> "B"
                                            1 -> "I"
                                            2 -> "N"
                                            3 -> "G"
                                            else -> "O"
                                        }
                                        val validRange = when(col) {
                                            0 -> "1-15"
                                            1 -> "16-30"
                                            2 -> "31-45"
                                            3 -> "46-60"
                                            else -> "61-75"
                                        }
                                        "• Número ${error.value} inválido en Columna $letra (debe estar entre $validRange)"
                                    }
                                    is BingoValidationError.DuplicateValue -> {
                                        val col = error.position / 5
                                        val row = error.position % 5
                                        val letra = when(col) {
                                            0 -> "B"
                                            1 -> "I"
                                            2 -> "N"
                                            3 -> "G"
                                            else -> "O"
                                        }
                                        "• Número ${error.value} duplicado en Columna $letra, Fila ${row + 1}"
                                    }
                                }
                                Text(
                                    text = errorMessage,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = { showValidationDialog = false }) {
                            Text("Entendido")
                        }
                    }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("B", "I", "N", "G", "O").forEach { letter ->
                        Text(
                            text = letter,
                            modifier = Modifier
                                .width(48.dp)
                                .padding(vertical = 8.dp),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                val focusRequesters = remember {
                    List(25) { FocusRequester() }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val focusRequesters = remember {
                        List(25) { FocusRequester() }
                    }

                    for (row in 0 until 5) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (col in 0 until 5) {
                                val position = row + (col * 5) // Changed from row * 5 + col to handle vertical navigation
                                val nextPosition = when {
                                    position == 12 -> 13 // Skip star cell
                                    position >= 24 -> position // Stay on last cell
                                    else -> position + 1
                                }

                                if (position == 12) {
                                    Box(
                                        modifier = Modifier.size(48.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = "Free Space",
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                } else {
                                    BingoCell(
                                        value = tempCardNumbers[position],
                                        onValueChange = { newValue ->
                                            settingsViewModel.updateCardNumber(position, newValue)
                                        },
                                        column = col,
                                        row = row,
                                        focusRequester = focusRequesters[position],
                                        nextFocusRequester = if (position < 24) focusRequesters[nextPosition] else null,
                                        selectedNumbers = selectedNumbers,
                                        gameMode = gameMode
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Menú desplegable para seleccionar el modo de juego en la parte derecha
            var expanded by remember { mutableStateOf(false) }
            val gameMode by settingsViewModel.gameMode.collectAsStateWithLifecycle()
            val gameModeLabel by settingsViewModel.gameModeLabel.collectAsState()

            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(180.dp)  // Establece el ancho de la columna del modo de juego
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    OutlinedTextField(
                        value = gameModeLabel,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Modo de juego") },
                        trailingIcon = {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Filled.KeyboardArrowDown, "Mostrar opciones")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        BingoGameMode.values().forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode.label) }, // Mostrar el label
                                onClick = {
                                    settingsViewModel.setGameMode(mode)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Cuadrícula de Bingo que muestra el patrón seleccionado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                val pattern = gameMode.pattern
                BingoPatternGrid(pattern = pattern)
            }
        }
    }
}