package gaims.search

interface SearchProblem<State : Comparable<State>, Action> {

    fun initial() : State

    fun actions(state: State) : Iterator<Action>

    fun result(state: State, action: Action) : State

    fun is_goal(state: State) : Boolean

    fun cost(c: Int, x: State, a: Action, y: State) : Int

}

class Node<State : Comparable<State>, Action>(var state: State, var parent: Node<State, Action>? = null, var action: Action? = null, var cost: Int = 0) {

    var depth : Int = parent?.depth ?: 0

    operator fun compareTo(other : Node<State, Action>) : Int {
        return this.state.compareTo(other.state)
    }

    fun expand(problem: SearchProblem<State, Action>) : Iterator<Node<State, Action>> {
        return sequence {
            val actions = problem.actions(state)

            while (actions.hasNext()) {
                yield(child(problem, actions.next()))
            }
        }.iterator()
    }

    fun child(problem: SearchProblem<State, Action>, action: Action) : Node<State, Action> {
        val next = problem.result(state, action)
        return Node(next, this, action, problem.cost(cost, this.state, action, next))
    }

    fun solution() : Iterable<Action?> {
        return path().map { it.action }
    }

    fun path() : Iterable<Node<State, Action>> {
        val nodes = ArrayList<Node<State, Action>>()

        var node : Node<State, Action>? = this
        while (node != null) {
            nodes.add(node)
            node = node.parent
        }

        return nodes.reversed()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Node<*, *>

        if (state != other.state) return false

        return true
    }

    override fun hashCode(): Int {
        return state.hashCode()
    }


}