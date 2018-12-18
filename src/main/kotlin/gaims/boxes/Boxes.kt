package gaims.boxes

import javafx.scene.canvas.Canvas
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.text.Text
import tornadofx.*

class HelloWorld : View() {

    private val colors = listOf(Color.GOLD, Color.SKYBLUE, Color.ORANGERED, Color.VIOLET, Color.DARKBLUE, Color.FORESTGREEN)

    private val canvasSize: Double = 512.0

    private var problem: BoxesProblem? = null
    private var solution: List<Pair<BoxesProblem.BoxesState, Move>>? = null
    private var currentSol = 0
    private lateinit var canv: Canvas
    private lateinit var solText: Text

    override val root = hbox {
        canv = canvas(canvasSize, canvasSize) {
            addEventHandler(MouseEvent.MOUSE_CLICKED) {
                val problem = problem
                if (problem != null) {
                    val i: Int = (it.x / (canvasSize / problem.n)).toInt()
                    val j: Int = ((canvasSize - it.y) / (canvasSize / problem.m)).toInt()

                    problem[i, j] = ((problem[i, j] + 1) % (problem.c + 1)).toByte()
                    update()
                }
            }
        }
        vbox {
            val nInput = textfield {
                promptText = "#Columns"
            }
            val mInput = textfield {
                promptText = "#Rows"
            }
            val cInput = textfield {
                promptText = "#Colors"
            }

            button("Generate") {
                action {
                    problem = BoxesProblem(nInput.text.toInt(), mInput.text.toInt(), cInput.text.toInt())
                    solution = null
                    currentSol = 0
                    solText.text = "N/A"
                    update()
                }
            }

            button("Solve") {
                action {
                    solution = problem?.solve()
                    solText.text = "Step ${currentSol + 1} / ${solution?.size}"
                    update()
                }
            }

            solText = text("N/A")

            button("Next Step") {
                action {
                    currentSol = (currentSol + 1) % (solution?.size ?: 0)
                    solText.text = "Step ${currentSol + 1} / ${solution?.size}"
                    update()
                }
            }
        }
    }

    private fun update() {
        val graphics = canv.graphicsContext2D

        graphics.fill = Color.WHITE
        graphics.clearRect(0.0, 0.0, canvasSize, canvasSize)

        graphics.fill = Color.BLACK

        val problem = problem
        val solution = solution
        var state: BoxesProblem.BoxesState? = null
        var action: Move? = null

        if (problem != null)
            state = problem.initial()

        if (solution != null) {
            state = solution[currentSol].first
            action = solution[currentSol].second
        }

        if (problem != null && state != null) {
            for (i in 0 until problem.n)
                for (j in 0 until problem.m) {
                    graphics.fill = if (state[i, j] != byte_zero) colors[state[i, j] - 1] else Color.TRANSPARENT
                    graphics.fillRect(
                        i * (canvasSize / problem.n),
                        canvasSize - ((j + 1) * (canvasSize / problem.m)),
                        (canvasSize / problem.n),
                        (canvasSize / problem.m)
                    )
                }

            for (i in 1 until problem.n)
                graphics.strokeLine(i * (canvasSize / problem.n), 0.0, i * (canvasSize / problem.n), canvasSize)

            for (j in 1 until problem.m)
                graphics.strokeLine(0.0, j * (canvasSize / problem.m), canvasSize, j * (canvasSize / problem.m))

            if (action != null) {
                val orig = action.position
                val dest = action.position(action.direction)

                graphics.strokeLine(
                    (orig.first + .5) * (canvasSize / problem.n),
                    canvasSize - (orig.second + 1 - .5) * (canvasSize / problem.m),
                    (dest.first + .5) * (canvasSize / problem.n),
                    canvasSize - (dest.second + 1 - .5) * (canvasSize / problem.m)
                )
            }
        }
    }
}

class Boxes : App(HelloWorld::class)

fun main(args: Array<String>) {
    launch<Boxes>(args)
}