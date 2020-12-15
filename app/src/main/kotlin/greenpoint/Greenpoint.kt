/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package greenpoint

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

import greenpoint.interpreter.Interpreter

class Greenpoint {
    var hadError: Boolean = false
    val interpreter = Interpreter()

    fun runPrompt() {
        val input = InputStreamReader(System.`in`)
        val reader = BufferedReader(input)

        println("Hello from beautiful Brooklyn, NY!")
        println("Greenpoint Interpreter: v0.0.0")

        while(true) {
            print("» ")
            val line = reader.readLine()
            if (line == "exit" || line == null) {
                println("\nNow leaving Brooklyn, fuggedaboutit")
                break
            }
            try {
                val value = interpreter.run(line)
                if (value != null) {
                    println(value)
                }
            } catch (e: Exception) {
                println(e.toString())
            }
        }
    }

    fun runFile(filename: String) {
        val bytes = File(filename).readBytes()
        try {
            interpreter.run(String(bytes))
        } catch (e: Exception) {
            println(e.toString())
            System.exit(65)
        }
    }
}

fun main(args: Array<String>) {
    val gp = Greenpoint()
    if (args.size > 1) {
        println("Usage: greenpoint [script]")
    } else if (args.size == 1) {
        gp.runFile(args[0])
    } else {
        gp.runPrompt()
    }
}
