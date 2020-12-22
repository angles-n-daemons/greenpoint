package greenpoint.interpreter.function

import greenpoint.interpreter.Interpreter
import greenpoint.interpreter.Environment

import greenpoint.scanner.Token
import greenpoint.scanner.TokenType

fun seedEnvironmentWithNatives(environment: Environment) {
    environment.define(
        Token(TokenType.IDENTIFIER, "clock", null, 0),
        Clock(),
    )
}

class Clock(): Callable {
    override fun arity(): Int { return 0 }

    override fun call(interpreter: Interpreter, args: List<Any?>): Any? {
        return System.currentTimeMillis() / 1000.0
    }

    override fun toString(): String { return "<native fn>" }
}
