package com.zybergo.browser.games

import kotlin.random.Random

/**
 * Pure game-state logic, no Android View/Canvas dependency here so it's
 * trivially unit-testable and has zero drawing-related memory footprint.
 * A thin custom View (SnakeView, not included — wire to your layout) reads
 * `snake` and `food` each tick and draws simple filled rects: no sprite
 * bitmaps needed, keeping this in the same "few KB" RAM class as the rest
 * of the app.
 */
class SnakeGame(private val gridWidth: Int = 20, private val gridHeight: Int = 20) {

    enum class Direction { UP, DOWN, LEFT, RIGHT }
    data class Point(val x: Int, val y: Int)

    var snake: MutableList<Point> = mutableListOf(Point(gridWidth / 2, gridHeight / 2))
        private set
    var food: Point = randomFood()
        private set
    var direction: Direction = Direction.RIGHT
        private set
    var score: Int = 0
        private set
    var isGameOver: Boolean = false
        private set

    private var pendingDirection: Direction = direction

    fun setDirection(newDirection: Direction) {
        // Prevent instant U-turn into self.
        val opposite = when (direction) {
            Direction.UP -> Direction.DOWN
            Direction.DOWN -> Direction.UP
            Direction.LEFT -> Direction.RIGHT
            Direction.RIGHT -> Direction.LEFT
        }
        if (newDirection != opposite) pendingDirection = newDirection
    }

    /** Advance the game by one tick. Call on a fixed timer (e.g. every 150ms). */
    fun tick() {
        if (isGameOver) return
        direction = pendingDirection

        val head = snake.first()
        val newHead = when (direction) {
            Direction.UP -> Point(head.x, head.y - 1)
            Direction.DOWN -> Point(head.x, head.y + 1)
            Direction.LEFT -> Point(head.x - 1, head.y)
            Direction.RIGHT -> Point(head.x + 1, head.y)
        }

        if (newHead.x < 0 || newHead.x >= gridWidth || newHead.y < 0 || newHead.y >= gridHeight ||
            snake.contains(newHead)
        ) {
            isGameOver = true
            return
        }

        snake.add(0, newHead)
        if (newHead == food) {
            score += 10
            food = randomFood()
        } else {
            snake.removeAt(snake.size - 1)
        }
    }

    fun reset() {
        snake = mutableListOf(Point(gridWidth / 2, gridHeight / 2))
        direction = Direction.RIGHT
        pendingDirection = Direction.RIGHT
        score = 0
        isGameOver = false
        food = randomFood()
    }

    private fun randomFood(): Point {
        var candidate: Point
        do {
            candidate = Point(Random.nextInt(gridWidth), Random.nextInt(gridHeight))
        } while (snake.contains(candidate))
        return candidate
    }
}
