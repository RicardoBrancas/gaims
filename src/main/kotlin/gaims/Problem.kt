package gaims

import java.io.OutputStream
import java.io.Serializable

interface Problem<S> : Serializable {

    fun solve() : S?

    fun save(output: OutputStream)

}