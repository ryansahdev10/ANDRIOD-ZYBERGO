package com.zybergo.browser.games

/**
 * Board cells: 0 = empty, 1 = human (X), 2 = AI (O).
 * Perfect play via minimax => AI never loses (best case for the human is a draw).
 */
class TicTacToeAI(private val aiPlayer: Int = 2, private val humanPlayer: Int = 1) {

    /** Returns the index (0-8) of the AI's chosen move. */
    fun bestMove(board: IntArray): Int {
        var bestScore = Int.MIN_VALUE
        var move = -1
        for (i in board.indices) {
            if (board[i] == 0) {
                board[i] = aiPlayer
                val score = minimax(board, depth = 0, isMaximizing = false)
                board[i] = 0
                if (score > bestScore) {
                    bestScore = score
                    move = i
                }
            }
        }
        return move
    }

    private fun minimax(board: IntArray, depth: Int, isMaximizing: Boolean): Int {
        val winner = winnerOf(board)
        if (winner == aiPlayer) return 10 - depth
        if (winner == humanPlayer) return depth - 10
        if (isBoardFull(board)) return 0

        return if (isMaximizing) {
            var best = Int.MIN_VALUE
            for (i in board.indices) {
                if (board[i] == 0) {
                    board[i] = aiPlayer
                    best = maxOf(best, minimax(board, depth + 1, false))
                    board[i] = 0
                }
            }
            best
        } else {
            var best = Int.MAX_VALUE
            for (i in board.indices) {
                if (board[i] == 0) {
                    board[i] = humanPlayer
                    best = minOf(best, minimax(board, depth + 1, true))
                    board[i] = 0
                }
            }
            best
        }
    }

    companion object {
        private val WIN_LINES = arrayOf(
            intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8), // rows
            intArrayOf(0, 3, 6), intArrayOf(1, 4, 7), intArrayOf(2, 5, 8), // cols
            intArrayOf(0, 4, 8), intArrayOf(2, 4, 6)                       // diagonals
        )

        fun winnerOf(board: IntArray): Int {
            for (line in WIN_LINES) {
                val (a, b, c) = line
                if (board[a] != 0 && board[a] == board[b] && board[b] == board[c]) {
                    return board[a]
                }
            }
            return 0
        }

        fun isBoardFull(board: IntArray): Boolean = board.none { it == 0 }
    }
}
