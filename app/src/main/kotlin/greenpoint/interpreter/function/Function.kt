package greenpoint.interpreter.function

import greenpoint.grammar.Stmt

import greenpoint.interpreter.Interpreter
import greenpoint.interpreter.Environment


class Return(val value: Any?): RuntimeException(null, null, false, false)


interface Callable {
    fun arity(): Int
    fun call(interpreter: Interpreter, args: List<Any?>): Any?
}


class Func(
    val func: Stmt.Func,
    val closure: Environment,
): Callable {
    override fun arity(): Int {
        return func.params.size
    }

    override fun call(interpreter: Interpreter, args: List<Any?>): Any? {
        val environment = Environment(closure)

        for (i in 0..(func.params.size-1)) {
            environment.define(func.params.get(i), args.get(i))
        }

        try {
            interpreter.executeBlock(func.body, environment)
        } catch (returnValue: Return) {
            return returnValue.value
        }
        return null
    }

    override fun toString(): String {
        return "<fn ${func.name.lexeme}>"
    }
}
