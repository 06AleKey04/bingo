package com.alekey.bingo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alekey.bingo.BingoGameMode

@Composable
fun BingoCell(
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