package greenpoint.interpreter.function

import greenpoint.grammar.Stmt

import greenpoint.scanner.Token
import greenpoint.scanner.TokenType
import greenpoint.interpreter.Interpreter
import greenpoint.interpreter.classes.Instance
import greenpoint.interpreter.Environment


class Return(val value: Any?): RuntimeException(null, null, false, false)


interface Callable {
    fun arity(): Int
    fun call(interpreter: Interpreter, args: List<Any?>): Any?
}


class Func(
    val func: Stmt.Func,
    val closure: Environment,
    val isInitializer: Boolean,
): Callable {
    private val thisToken = Token(TokenType.THIS, "this", null, 0)

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
            if (isInitializer) return closure.getAt(0, thisToken)
            return returnValue.value
        }

        if (isInitializer) return closure.getAt(0, thisToken)
        return null
    }

    override fun toString(): String {
        return "<fn ${func.name.lexeme}>"
    }

    fun bind(instance: Instance): Func {
        val environment = Environment(closure)
        environment.define(thisToken, instance)
        return Func(func, environment, isInitializer)
    }
}
