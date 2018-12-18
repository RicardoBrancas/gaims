package gaims.search

import java.util.*

fun <State : Comparable<State>, Action> bestFirstGraphSearch(
    problem: SearchProblem<State, Action>,
    f: (Node<State, Action>) -> Int
): Node<State, Action>? {
    var node = Node<State, Action>(problem.initial())
    val frontier = PriorityQueue(Comparator.comparing(f))
    frontier.add(node)
    val explored = HashSet<State>()
    while (!frontier.isEmpty()) {
        node = frontier.poll()
        if (problem.is_goal(node.state))
            return node

        explored.add(node.state)
        node.expand(problem).forEach {
            if (it.state !in explored)
                frontier.add(it)
        }
    }

    return null
}

fun <State : Comparable<State>, Action> astarSearch(
    problem: SearchProblem<State, Action>,
    h: (Node<State, Action>) -> Int
): Node<State, Action>? = bestFirstGraphSearch(problem) { n -> n.cost + h(n)}