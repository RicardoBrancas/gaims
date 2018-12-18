package gaims.boxes

import gaims.GridProblem
import gaims.search.SearchProblem
import gaims.search.astarSearch
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.OutputStream
import javax.swing.Action

const val byte_zero = 0.toByte()

class BoxesProblem : GridProblem<List<Pair<BoxesProblem.BoxesState, Move>>>,
    SearchProblem<BoxesProblem.BoxesState, Move> {

    val c: Int
    var stateCounter = 0

    constructor(n: Int, m: Int, c: Int) : super(n, m) {
        this.c = c
    }

    constructor(input: InputStream) : super(input) {
        val i = ObjectInputStream(input)
        c = i.readInt()
    }

    override fun initial(): BoxesState {
        return BoxesState(grid)
    }

    override fun cost(c: Int, x: BoxesState, a: Move, y: BoxesState): Int = c + 1

    override fun actions(state: BoxesState): Iterator<Move> {
        val actions = ArrayList<Move>()

        for (i in 0 until n) {
            for (j in 0 until m) {
                if (state[i, j] != byte_zero) {
                    if (j > 0)
                        actions.add(Move(Pair(i, j), Direction.DOWN))

                    if (i < n - 1)
                        actions.add(Move(Pair(i, j), Direction.RIGHT))

                    if (j < m - 1)
                        actions.add(Move(Pair(i, j), Direction.UP))

                    if (i > 0)
                        actions.add(Move(Pair(i, j), Direction.LEFT))
                }
            }
        }

        return actions.iterator()
    }

    override fun result(state: BoxesState, action: Move): BoxesState = state(action)

    override fun is_goal(state: BoxesState): Boolean {
        return state.empty()
    }

    override fun solve(): List<Pair<BoxesState, Move>>? {
        val path = astarSearch(this) { node -> heuristic(node.state) }?.path()?.toList()
            ?: return null

        val solution = ArrayList<Pair<BoxesState, Move>>()
        for (i in 0 until path.size - 1)
            solution.add(Pair(path[i].state, path[i + 1].action as Move))

        return solution
    }

    private fun heuristic(state: BoxesState): Int {
        return 1
    }

    override operator fun set(i: Int, j: Int, v: Byte) {
        if (v in 0..c)
            super.set(i, j, v)
        else
            throw IllegalArgumentException()
    }

    override fun save(output: OutputStream) {
        super.save(output)
        val o = ObjectOutputStream(output)
        o.writeInt(c)
    }

    inner class BoxesState(private val grid: ByteArray) : Comparable<BoxesState> {
        private val id = stateCounter++

        override fun compareTo(other: BoxesState): Int {
            return id.compareTo(other.id)
        }

        fun empty(): Boolean {
            return grid.all { i -> i == byte_zero }
        }

        operator fun invoke(action: Move): BoxesState {
            val state = BoxesState(grid.copyOf())
            val pos = action.position(action.direction)

            val tmp = state[action.position]
            state[action.position] = state[pos]
            state[pos] = tmp

            state.compact()
            return state
        }

        private fun compact() {
            var changed = false

            for (i in 0 until n)
                for (j in 0 until m - 1)
                    if (this[i, j] == byte_zero && this[i, j + 1] != byte_zero) {
                        changed = true
                        this[i, j] = this[i, j + 1]
                        this[i, j + 1] = 0
                    }

            if (changed)
                return compact()

            val positions = ArrayList<Pair<Int, Int>>()

            for (i in 0 until n - 2)
                for (j in 0 until m) {
                    val color = this[i, j]
                    if (color == byte_zero)
                        continue

                    var seen = 0
                    for (k in i until n)
                        if (this[k, j] == color)
                            seen++
                        else
                            break

                    if (seen >= 3) {
                        changed = true
                        var k = i
                        while (seen > 0) {
                            positions.add(Pair(k, j))
                            k++
                            seen--
                        }
                    }
                }

            for (i in 0 until n)
                for (j in 0 until m - 2) {
                    val color = this[i, j]
                    if (color == byte_zero)
                        continue

                    var seen = 0
                    for (k in j until m)
                        if (this[i, k] == color)
                            seen++
                        else
                            break

                    if (seen >= 3) {
                        changed = true
                        var k = j
                        while (seen > 0) {
                            positions.add(Pair(i, k))
                            k++
                            seen--
                        }
                    }
                }

            for (position in positions) {
                this[position] = 0
            }

            if (changed)
                return compact()
        }

        operator fun set(i: Int, j: Int, value: Byte) {
            grid[i * m + j] = value
        }

        operator fun set(pos: Pair<Int, Int>, value: Byte) {
            this[pos.first, pos.second] = value
        }

        operator fun get(i: Int, j: Int): Byte {
            return grid[i * m + j]
        }

        operator fun get(pos: Pair<Int, Int>): Byte {
            return this[pos.first, pos.second]
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as BoxesState

            if (!grid.contentEquals(other.grid)) return false

            return true
        }

        override fun hashCode(): Int {
            return grid.contentHashCode()
        }
    }
}

internal operator fun Pair<Int, Int>.invoke(direction: Direction): Pair<Int, Int> {
    return when (direction) {
        Direction.LEFT -> Pair(this.first - 1, this.second)
        Direction.RIGHT -> Pair(this.first + 1, this.second)
        Direction.UP -> Pair(this.first, this.second + 1)
        Direction.DOWN -> Pair(this.first, this.second - 1)
    }
}

data class Move(val position: Pair<Int, Int>, val direction: Direction)

enum class Direction {
    LEFT, RIGHT, UP, DOWN
}