package com.alekey.bingo

enum class BingoGameMode(val label: String, val pattern: Array<IntArray>) {
    TREE("Árbol", arrayOf(
        intArrayOf(0, 0, 1, 0, 0),
        intArrayOf(0, 1, 1, 1, 0),
        intArrayOf(1, 1, 1, 1, 1),
        intArrayOf(0, 0, 1, 0, 0),
        intArrayOf(0, 0, 1, 0, 0)
    )),
    O("Letra O", arrayOf(
        intArrayOf(0, 1, 1, 1, 0),
        intArrayOf(1, 0, 0, 0, 1),
        intArrayOf(1, 0, 0, 0, 1),
        intArrayOf(1, 0, 0, 0, 1),
        intArrayOf(0, 1, 1, 1, 0)
    )),
    CHESS("Ajedrez", arrayOf(
        intArrayOf(1, 0, 1, 0, 1),
        intArrayOf(0, 1, 0, 1, 0),
        intArrayOf(1, 0, 1, 0, 1),
        intArrayOf(0, 1, 0, 1, 0),
        intArrayOf(1, 0, 1, 0, 1)
    )),
    C("Letra C", arrayOf(
        intArrayOf(1, 1, 1, 1, 1),
        intArrayOf(1, 0, 0, 0, 0),
        intArrayOf(1, 0, 0, 0, 0),
        intArrayOf(1, 0, 0, 0, 0),
        intArrayOf(1, 1, 1, 1, 1)
    )),
    M("Letra M", arrayOf(
        intArrayOf(1, 1, 1, 1, 1),
        intArrayOf(0, 1, 0, 0, 0),
        intArrayOf(0, 0, 1, 0, 0),
        intArrayOf(0, 1, 0, 0, 0),
        intArrayOf(1, 1, 1, 1, 1)
    )),
    DIAMOND("Diamante", arrayOf(
        intArrayOf(0, 0, 1, 0, 0),
        intArrayOf(0, 1, 0, 1, 0),
        intArrayOf(1, 0, 1, 0, 1),
        intArrayOf(0, 1, 0, 1, 0),
        intArrayOf(0, 0, 1, 0, 0)
    )),
    DIAGONALS("Diagonales", arrayOf(
        intArrayOf(1, 0, 0, 0, 1),
        intArrayOf(0, 1, 0, 1, 0),
        intArrayOf(0, 0, 1, 0, 0),
        intArrayOf(0, 1, 0, 1, 0),
        intArrayOf(1, 0, 0, 0, 1)
    )),
    BLACKOUT("Apagón", arrayOf(
        intArrayOf(1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1),
        intArrayOf(1, 1, 1, 1, 1)
    ))
}