package com.zybergo.browser.games

import kotlin.random.Random

/**
 * Classic 2048 logic on a 4x4 IntArray (0 = empty cell). No object grid of
 * boxed Tile objects — a flat IntArray(16) is ~64 bytes, effectively free.
 */
class Game2048 {
    companion object { const val SIZE = 4 }

    var grid = IntArray(SIZE * SIZE)
        private set
    var score: Int = 0
        private set
    var isGameOver: Boolean = false
        private set

    init { reset() }

    fun reset() {
        grid = IntArray(SIZE * SIZE)
        score = 0
        isGameOver = false
        spawnTile()
        spawnTile()
    }

    enum class Move { UP, DOWN, LEFT, RIGHT }

    /** Returns true if the grid actually changed (i.e. move was legal). */
    fun move(direction: Move): Boolean {
        val before = grid.copyOf()
        when (direction) {
            Move.LEFT -> repeat(SIZE) { row -> slideRow(row, reverse = false) }
            Move.RIGHT -> repeat(SIZE) { row -> slideRow(row, reverse = true) }
            Move.UP -> repeat(SIZE) { col -> slideCol(col, reverse = false) }
            Move.DOWN -> repeat(SIZE) { col -> slideCol(col, reverse = true) }
        }
        val changed = !grid.contentEquals(before)
        if (changed) {
            spawnTile()
            isGameOver = !hasAnyMoveLeft()
        }
        return changed
    }

    private fun idx(row: Int, col: Int) = row * SIZE + col

    private fun slideRow(row: Int, reverse: Boolean) {
        val cols = (0 until SIZE).let { if (reverse) it.reversed() else it }
        val values = cols.map { grid[idx(row, it)] }.filter { it != 0 }.toMutableList()
        merge(values)
        cols.forEachIndexed { i, col -> grid[idx(row, col)] = values.getOrElse(i) { 0 } }
    }

    private fun slideCol(col: Int, reverse: Boolean) {
        val rows = (0 until SIZE).let { if (reverse) it.reversed() else it }
        val values = rows.map { grid[idx(it, col)] }.filter { it != 0 }.toMutableList()
        merge(values)
        rows.forEachIndexed { i, row -> grid[idx(row, col)] = values.getOrElse(i) { 0 } }
    }

    private fun merge(values: MutableList<Int>) {
        var i = 0
        while (i < values.size - 1) {
            if (values[i] == values[i + 1]) {
                values[i] = values[i] * 2
                score += values[i]
                values.removeAt(i + 1)
            }
            i++
        }
    }

    private fun spawnTile() {
        val empty = grid.indices.filter { grid[it] == 0 }
        if (empty.isEmpty()) return
        val pos = empty[Random.nextInt(empty.size)]
        grid[pos] = if (Random.nextFloat() < 0.9f) 2 else 4
    }

    private fun hasAnyMoveLeft(): Boolean {
        if (grid.any { it == 0 }) return true
        for (row in 0 until SIZE) for (col in 0 until SIZE) {
            val v = grid[idx(row, col)]
            if (col < SIZE - 1 && v == grid[idx(row, col + 1)]) return true
            if (row < SIZE - 1 && v == grid[idx(row + 1, col)]) return true
        }
        return false
    }
}
