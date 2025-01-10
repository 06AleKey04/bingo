package com.alekey.bingo

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BingoCard(
    val cardNumber: Int,
    var cardId: String = "",
    var numbers: MutableList<Int?> = MutableList(25) { null }
)

sealed class BingoValidationError {
    data class EmptyCell(val position: Int) : BingoValidationError()
    data class InvalidRange(val position: Int, val value: Int) : BingoValidationError()
    data class DuplicateValue(val position: Int, val value: Int) : BingoValidationError()
}

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val sharedPreferences = application.getSharedPreferences("BingoPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _numberCards = MutableStateFlow<Int?>(null)
    val numberCards: StateFlow<Int?> = _numberCards.asStateFlow()

    private val _displayNumber = MutableStateFlow<String>("")
    val displayNumber: StateFlow<String> = _displayNumber.asStateFlow()

    private val _isSynced = MutableStateFlow(true)
    val isSynced: StateFlow<Boolean> = _isSynced.asStateFlow()

    private val _bingoCards = MutableStateFlow<List<BingoCard>>(emptyList())
    val bingoCards: StateFlow<List<BingoCard>> = _bingoCards.asStateFlow()

    private val _selectedCardIndex = MutableStateFlow(0)
    val selectedCardIndex: StateFlow<Int> = _selectedCardIndex.asStateFlow()

    private val _tempCardNumbers = MutableStateFlow<MutableList<Int?>>(MutableList(25) { null })
    val tempCardNumbers: StateFlow<MutableList<Int?>> = _tempCardNumbers.asStateFlow()

    private val _hasUnsavedChanges = MutableStateFlow(false)
    val hasUnsavedChanges: StateFlow<Boolean> = _hasUnsavedChanges.asStateFlow()

    private val _selectedNumbers = MutableStateFlow<Set<Int>>(emptySet())
    val selectedNumbers: StateFlow<Set<Int>> = _selectedNumbers.asStateFlow()

    private val _gameMode = MutableStateFlow<BingoGameMode>(BingoGameMode.C)
    val gameMode: StateFlow<BingoGameMode> = _gameMode

    private val _gameModeLabel = MutableStateFlow(BingoGameMode.C.label)
    val gameModeLabel: StateFlow<String> = _gameModeLabel

    private val validator = BingoValidator()

    private val _winningCards = MutableStateFlow<List<BingoWinner>>(emptyList())
    val winningCards: StateFlow<List<BingoWinner>> = _winningCards.asStateFlow()

    private val _currentModeWinners = MutableStateFlow<List<BingoWinner>>(emptyList())
    val currentModeWinners: StateFlow<List<BingoWinner>> = _currentModeWinners.asStateFlow()

    private val _blackoutWinners = MutableStateFlow<List<BingoWinner>>(emptyList())
    val blackoutWinners: StateFlow<List<BingoWinner>> = _blackoutWinners.asStateFlow()

    init {
        loadSavedState()
    }

    private fun loadSavedState() {
        // Cargar número de tarjetas
        _numberCards.value = sharedPreferences.getInt("numberCards", 0)
        _displayNumber.value = _numberCards.value?.toString() ?: ""

        // Cargar tarjetas de bingo
        val cardsJson = sharedPreferences.getString("bingoCards", null)
        if (cardsJson != null) {
            val type = object : TypeToken<List<BingoCard>>() {}.type
            _bingoCards.value = gson.fromJson(cardsJson, type)
        }

        // Cargar números seleccionados
        val selectedNumbersJson = sharedPreferences.getString("selectedNumbers", null)
        if (selectedNumbersJson != null) {
            val type = object : TypeToken<Set<Int>>() {}.type
            _selectedNumbers.value = gson.fromJson(selectedNumbersJson, type)
        }

        // Cargar modo de juego
        val gameModeString = sharedPreferences.getString("gameMode", BingoGameMode.C.name)
        val savedGameMode = BingoGameMode.valueOf(gameModeString!!)
        _gameMode.value = savedGameMode
        _gameModeLabel.value = savedGameMode.label

        // Cargar índice de tarjeta seleccionada
        _selectedCardIndex.value = sharedPreferences.getInt("selectedCardIndex", 0)

        // Cargar números temporales si hay tarjetas
        if (_bingoCards.value.isNotEmpty() && _selectedCardIndex.value < _bingoCards.value.size) {
            _tempCardNumbers.value = _bingoCards.value[_selectedCardIndex.value].numbers.toMutableList()
        }

        // Verificar ganadores después de cargar todo
        checkWinningCards()
    }

    private fun saveState() {
        sharedPreferences.edit().apply {
            // Guardar número de tarjetas
            putInt("numberCards", _numberCards.value ?: 0)

            // Guardar tarjetas de bingo
            putString("bingoCards", gson.toJson(_bingoCards.value))

            // Guardar números seleccionados
            putString("selectedNumbers", gson.toJson(_selectedNumbers.value))

            // Guardar modo de juego
            putString("gameMode", _gameMode.value.name)

            // Guardar índice de tarjeta seleccionada
            putInt("selectedCardIndex", _selectedCardIndex.value)

            apply()
        }
    }

    fun updateDisplayNumber(number: String) {
        _displayNumber.value = number
        _isSynced.value = number.toIntOrNull() == _numberCards.value
        Log.d("ViewModel", "Display number updated to: $number")
    }

    fun syncNumbers() {
        val newValue = _displayNumber.value.toIntOrNull() ?: return
        val currentCards = _bingoCards.value.toMutableList()

        when {
            newValue > currentCards.size -> {
                val additionalCards = (currentCards.size until newValue).map { index ->
                    BingoCard(
                        cardNumber = index + 1,
                        cardId = "",
                        numbers = MutableList(25) { null }
                    )
                }
                _bingoCards.value = currentCards + additionalCards
            }
            newValue < currentCards.size -> {
                _bingoCards.value = currentCards.take(newValue)
            }
        }

        _numberCards.value = newValue
        _isSynced.value = true

        if (_bingoCards.value.isNotEmpty()) {
            _selectedCardIndex.value = 0
            _tempCardNumbers.value = _bingoCards.value[0].numbers.toMutableList()
        }

        saveState()
    }

    fun updateCardId(cardNumber: Int, newId: String) {
        _hasUnsavedChanges.value = true
        _bingoCards.value = _bingoCards.value.map {
            if (it.cardNumber == cardNumber) it.copy(cardId = newId)
            else it
        }
        saveState()
    }

    fun updateCardNumber(position: Int, number: Int?) {
        if (position != 12) { // Solo evitamos la posición central
            _hasUnsavedChanges.value = true
            val currentNumbers = _tempCardNumbers.value.toMutableList()
            currentNumbers[position] = number
            _tempCardNumbers.value = currentNumbers
        }
    }

    fun setSelectedCard(index: Int) {
        if (index >= 0 && index < (_bingoCards.value.size)) {
            _selectedCardIndex.value = index
            // Crear una nueva lista para evitar referencias compartidas
            _tempCardNumbers.value = _bingoCards.value[index].numbers.toMutableList()
        }
    }

    fun selectNumber(number: Int) {
        _selectedNumbers.value = _selectedNumbers.value + number
        checkWinningCards() // Verificar después de cada selección
        saveState()
    }

    fun deselectNumber(number: Int) {
        _selectedNumbers.value = _selectedNumbers.value - number
        checkWinningCards() // Verificar después de cada deselección
        saveState()
    }

    private fun checkWinningCards() {
        // Verificar ganadores en el modo actual (si no es BLACKOUT)
        if (_gameMode.value != BingoGameMode.BLACKOUT) {
            val newCurrentWinners = validator.checkAllCards(
                cards = _bingoCards.value,
                selectedNumbers = _selectedNumbers.value,
                gameMode = _gameMode.value
            )
            _currentModeWinners.value = newCurrentWinners
        }

        // Verificar ganadores de BLACKOUT
        val newBlackoutWinners = validator.checkAllCards(
            cards = _bingoCards.value,
            selectedNumbers = _selectedNumbers.value,
            gameMode = BingoGameMode.BLACKOUT
        )
        _blackoutWinners.value = newBlackoutWinners

        // Combinar todos los ganadores para la lista principal
        _winningCards.value = (_currentModeWinners.value + _blackoutWinners.value).distinctBy {
            "${it.card.cardId}-${it.gameMode}" // Clave única que combina cardId y modo de juego
        }
    }

    fun hasWonInCurrentMode(cardId: String): Boolean {
        return _currentModeWinners.value.any { it.card.cardId == cardId }
    }

    fun hasWonBlackout(cardId: String): Boolean {
        return _blackoutWinners.value.any { it.card.cardId == cardId }
    }

    fun resetSelectedNumbers() {
        _selectedNumbers.value = emptySet()
        checkWinningCards() // También verificamos al resetear
        saveState()
    }

    fun saveCardNumbers() {
        val currentIndex = _selectedCardIndex.value
        val currentCards = _bingoCards.value.toMutableList()

        if (currentIndex >= 0 && currentIndex < currentCards.size) {
            // Al guardar la cartilla, aseguramos que la posición central sea 0
            val newNumbers = _tempCardNumbers.value.toMutableList()
            newNumbers[12] = 0 // Asignar 0 a la posición central

            currentCards[currentIndex] = currentCards[currentIndex].copy(
                numbers = newNumbers
            )
            _bingoCards.value = currentCards.toList()
            _hasUnsavedChanges.value = false
        }
        saveState()
    }

    fun setGameMode(mode: BingoGameMode) {
        _gameMode.value = mode
        _gameModeLabel.value = mode.label
        saveState()
    }

    private fun validateCardNumbers(): List<BingoValidationError> {
        val errors = mutableListOf<BingoValidationError>()
        val numbers = _tempCardNumbers.value

        // Mapeo de columnas y sus rangos válidos
        val columnRanges = mapOf(
            0 to 1..15,    // B (primera columna)
            1 to 16..30,   // I (segunda columna)
            2 to 31..45,   // N (tercera columna)
            3 to 46..60,   // G (cuarta columna)
            4 to 61..75    // O (quinta columna)
        )

        // Verificar cada posición en la cuadrícula
        for (row in 0..4) {
            for (col in 0..4) {
                val position = col * 5 + row  // Cambiado de row * 5 + col a col * 5 + row

                // Saltar la posición central
                if (position == 12) continue

                val value = numbers[position]

                // Verificar celda vacía
                if (value == null) {
                    errors.add(BingoValidationError.EmptyCell(position))
                    continue
                }

                // Verificar rango según la columna
                val validRange = columnRanges[col] ?: continue
                if (value !in validRange) {
                    errors.add(BingoValidationError.InvalidRange(position, value))
                }

                // Verificar duplicados en la misma columna
                val duplicatesInColumn = (0..4)
                    .map { r -> col * 5 + r }  // Ajustado para la nueva lógica de posición
                    .filter { it != 12 } // Excluir posición central
                    .filter { it != position } // Excluir posición actual
                    .mapNotNull { pos -> numbers[pos] }
                    .contains(value)

                if (duplicatesInColumn) {
                    errors.add(BingoValidationError.DuplicateValue(position, value))
                }
            }
        }

        return errors
    }

    fun tryToSaveCardNumbers(): List<BingoValidationError> {
        val errors = validateCardNumbers()
        if (errors.isEmpty()) {
            saveCardNumbers()
        }
        return errors
    }

    override fun onCleared() {
        super.onCleared()
        saveState()
    }

}