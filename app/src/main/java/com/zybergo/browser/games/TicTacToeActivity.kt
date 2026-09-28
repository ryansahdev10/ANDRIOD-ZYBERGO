package com.zybergo.browser.games

import android.app.Activity
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView

class TicTacToeActivity : Activity() {

    private val board = IntArray(9) // 0 empty, 1 human, 2 AI
    private val ai = TicTacToeAI()
    private lateinit var cells: List<Button>
    private lateinit var statusText: TextView
    private var gameOver = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
        }

        statusText = TextView(this).apply {
            text = "Your move (X)"
            textSize = 20f
            gravity = Gravity.CENTER
        }
        root.addView(statusText)

        val grid = GridLayout(this).apply {
            rowCount = 3
            columnCount = 3
        }

        cells = (0 until 9).map { index ->
            Button(this).apply {
                textSize = 28f
                layoutParams = GridLayout.LayoutParams().apply {
                    width = 200; height = 200
                    rowSpec = GridLayout.spec(index / 3)
                    columnSpec = GridLayout.spec(index % 3)
                }
                setOnClickListener { onCellTapped(index) }
            }
        }
        cells.forEach { grid.addView(it) }
        root.addView(grid)

        val resetButton = Button(this).apply {
            text = "Reset"
            setOnClickListener { resetGame() }
        }
        root.addView(resetButton)

        setContentView(root)
    }

    private fun onCellTapped(index: Int) {
        if (gameOver || board[index] != 0) return

        board[index] = 1 // human
        cells[index].text = "X"

        if (checkEnd()) return

        // AI move (minimax is fast enough on a 3x3 board to run on the main thread).
        val aiMove = ai.bestMove(board)
        if (aiMove >= 0) {
            board[aiMove] = 2
            cells[aiMove].text = "O"
        }
        checkEnd()
    }

    private fun checkEnd(): Boolean {
        val winner = TicTacToeAI.winnerOf(board)
        return when {
            winner == 1 -> { statusText.text = "You win?! (bug if so)"; gameOver = true; true }
            winner == 2 -> { statusText.text = "AI wins"; gameOver = true; true }
            TicTacToeAI.isBoardFull(board) -> { statusText.text = "Draw"; gameOver = true; true }
            else -> { statusText.text = "Your move (X)"; false }
        }
    }

    private fun resetGame() {
        board.fill(0)
        cells.forEach { it.text = "" }
        gameOver = false
        statusText.text = "Your move (X)"
    }
}
