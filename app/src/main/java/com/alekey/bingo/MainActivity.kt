package com.alekey.bingo

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels {
        ViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContent {
            val navController = rememberNavController()
            var selectedItem by remember { mutableStateOf(0) }

            val items = listOf(
                NavigationItem("settings", "Ajustes", Icons.Filled.Settings),
                NavigationItem("play", "Jugar", Icons.Filled.VideogameAsset)
            )

            Scaffold(
                bottomBar = {
                    NavigationBar {
                        items.forEachIndexed { index, item ->
                            NavigationBarItem(
                                icon = { Icon(item.icon, contentDescription = item.title) },
                                label = { Text(item.title) },
                                selected = selectedItem == index,
                                onClick = {
                                    selectedItem = index
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.startDestinationId)
                                        launchSingleTop = true
                                    }
                                }
                            )
                        }
                    }
                }
            ) { paddingValues ->
                NavHost(
                    navController = navController,
                    startDestination = "settings",
                    modifier = Modifier.padding(paddingValues)
                ) {
                    composable("settings") {
                        SettingsScreen(settingsViewModel = settingsViewModel) // Pasar el ViewModel
                    }
                    composable("play") {
                        PlayScreen(settingsViewModel = settingsViewModel) // Pasar el ViewModel
                    }
                }
            }
        }
    }

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


    @Composable
    private fun BingoCell(
        value: Int?,
        onValueChange: (Int?) -> Unit,
        column: Int,
        focusRequester: FocusRequester,
        nextFocusRequester: FocusRequester?,
        selectedNumbers: Set<Int> = emptySet(), // Añadido
        gameMode: BingoGameMode = BingoGameMode.C, // Añadido
        row: Int // Añadido
    ) {
        var textFieldValue by remember(value) { mutableStateOf(value?.toString() ?: "") }

        // Determinar el color de fondo basado en el estado
        val backgroundColor = when {
            value != null && selectedNumbers.contains(value) && gameMode.pattern[row][column] == 1 ->
                Color(0xFFBFFCC6) // Verde claro para números seleccionados que coinciden con el patrón
            value != null && selectedNumbers.contains(value) ->
                Color(0xFFFFFFD1) // Amarillo claro para números seleccionados que no coinciden con el patrón
            else -> Color.Transparent
        }

        Box(
            modifier = Modifier
                .size(48.dp)
                .padding(2.dp)
        ) {
            BasicTextField(
                value = textFieldValue,
                onValueChange = { newText ->
                    if (newText.isEmpty()) {
                        textFieldValue = ""
                        onValueChange(null)
                    } else if (newText.all { it.isDigit() } && newText.length <= 2) {
                        textFieldValue = newText
                        onValueChange(newText.toIntOrNull())
                    }
                },
                modifier = Modifier
                    .fillMaxSize()
                    .focusRequester(focusRequester)
                    .onKeyEvent { keyEvent ->
                        if (keyEvent.key == Key.Enter || keyEvent.key == Key.Tab) {
                            nextFocusRequester?.requestFocus()
                            true
                        } else {
                            false
                        }
                    }
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = MaterialTheme.shapes.small
                    )
                    .background(color = backgroundColor, shape = MaterialTheme.shapes.small)
                    .padding(4.dp),
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = if (nextFocusRequester != null) ImeAction.Next else ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onNext = { nextFocusRequester?.requestFocus() }
                ),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        innerTextField()
                    }
                }
            )
        }
    }

    @Composable
    fun PlayScreen(settingsViewModel: SettingsViewModel) {
        val selectedNumbers by settingsViewModel.selectedNumbers.collectAsState()
        val winningCards by settingsViewModel.winningCards.collectAsState()
        var showWinnerDialog by remember { mutableStateOf(false) }

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

            var showConfirmationDialog by remember { mutableStateOf(false) }
            var numberToDeselect by remember { mutableStateOf<Int?>(null) }

            // Grid de números
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val columns = listOf(
                    1..15,
                    16..30,
                    31..45,
                    46..60,
                    61..75
                )

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

            if (showConfirmationDialog) {
                AlertDialog(
                    onDismissRequest = { showConfirmationDialog = false },
                    title = { Text("Confirmación") },
                    text = { Text("¿Seguro que desea deseleccionar el número $numberToDeselect?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                settingsViewModel.deselectNumber(numberToDeselect ?: 0)
                                showConfirmationDialog = false
                                numberToDeselect = null
                            }
                        ) {
                            Text("Sí")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = {
                                showConfirmationDialog = false
                                numberToDeselect = null
                            }
                        ) {
                            Text("No")
                        }
                    }
                )
            }

            var showDialog by remember { mutableStateOf(false) }

            // Botón de reinicio
            Button(
                onClick = { showDialog = true },
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

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Confirmación") },
                    text = { Text("¿Seguro que desea reiniciar la partida?") },
                    confirmButton = {
                        Button(
                            onClick = {
                                settingsViewModel.resetSelectedNumbers()
                                showDialog = false
                            }
                        ) {
                            Text("Sí")
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { showDialog = false }
                        ) {
                            Text("No")
                        }
                    }
                )
            }

            // Contador
            Text(
                text = "Números seleccionados: ${selectedNumbers.size} / 75",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (showWinnerDialog && winningCards.isNotEmpty()) {
                AlertDialog(
                    onDismissRequest = { showWinnerDialog = false },
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
                            winningCards.forEach { winner ->
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
                            onClick = { showWinnerDialog = false },
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
    }

    @Composable
    private fun NumberButton(
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

}

data class NavigationItem(
    val route: String,
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)