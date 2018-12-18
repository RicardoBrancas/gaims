package gaims

import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.OutputStream

abstract class GridProblem<S> : Problem<S> {

    val n : Int
    val m : Int
    val grid: ByteArray

    constructor(n: Int, m: Int) {
        this.n = n
        this.m = m
        grid = ByteArray(n * m)
    }

    constructor(input: InputStream) {
        val i = ObjectInputStream(input)
        n = i.readInt()
        m = i.readInt()
        grid = i.readObject() as ByteArray
    }

    operator fun get(i: Int, j: Int) : Byte = grid[i * m + j]

    open operator fun set(i: Int, j: Int, v: Byte) {
        grid[i * m + j] = v
    }

    override fun save(output: OutputStream) {
        val o = ObjectOutputStream(output)
        o.writeInt(n)
        o.writeInt(m)
        o.writeObject(grid)
        o.flush()
    }
}