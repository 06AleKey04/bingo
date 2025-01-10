package com.alekey.bingo

data class BingoWinner(
    val card: BingoCard,
    val gameMode: BingoGameMode
)

class BingoValidator {
    fun validateWin(
        card: BingoCard,
        selectedNumbers: Set<Int>,
        gameMode: BingoGameMode
    ): Boolean {
        val pattern = gameMode.pattern
        val numbers = card.numbers

        // Para cada posición en el patrón
        for (row in pattern.indices) {
            for (col in pattern[row].indices) {
                // Si esta posición es parte del patrón (1)
                if (pattern[row][col] == 1) {
                    // Convertir de coordenadas de matriz a posición lineal en el cartón
                    val position = col * 5 + row
                    val currentNumber = numbers[position]

                    // Si es la posición central (null o 0) la consideramos como marcada
                    if (position == 12) continue

                    // Si no hay número en esta posición o el número no ha sido seleccionado
                    if (currentNumber == null || !selectedNumbers.contains(currentNumber)) {
                        return false
                    }
                }
            }
        }

        return true
    }

    fun checkAllCards(
        cards: List<BingoCard>,
        selectedNumbers: Set<Int>,
        gameMode: BingoGameMode
    ): List<BingoWinner> {
        return cards.filter { card ->
            validateWin(card, selectedNumbers, gameMode)
        }.map { card ->
            BingoWinner(card, gameMode)
        }
    }
}