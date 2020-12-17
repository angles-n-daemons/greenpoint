package greenpoint.interpreter 

import java.util.HashMap

import greenpoint.scanner.Token
import greenpoint.scanner.TokenType

class Environment() {
    protected val values: HashMap<String, Any?> = HashMap<String, Any?>()

    fun define(name: Token, value: Any?) {
        values.put(name.lexeme, value)
    }

    fun assign(name: Token, value: Any?) {
        if (!values.containsKey(name.lexeme)) {
            throw RuntimeError("Undefined variable ${name.lexeme}")
        }
        values.put(name.lexeme, value)
    }

    fun get(name: Token): Any? {
        if (name.type != TokenType.IDENTIFIER) {
            throw RuntimeError("Bad interpreter, attempted to retrieve variable with ${name.type}")
        }

        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme)
        }

        throw RuntimeError("Undefined variable '${name.lexeme}'")
    }
}
