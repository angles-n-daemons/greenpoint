package greenpoint.interpreter 

import java.util.HashMap

import greenpoint.scanner.Token
import greenpoint.scanner.TokenType

class Environment(val enclosing: Environment? = null) {
    protected val values: HashMap<String, Any?> = HashMap<String, Any?>()

    fun define(name: Token, value: Any?) {
        if (name.type != TokenType.IDENTIFIER) {
            throw wrongTokenError(name)
        }

        values.put(name.lexeme, value)
    }

    fun assign(name: Token, value: Any?) {
        if (name.type != TokenType.IDENTIFIER) {
            throw wrongTokenError(name)
        }

        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value)
            return
        }

        if (enclosing != null) {
            enclosing.assign(name, value)
            return
        }

        throw RuntimeError("Undefined variable ${name.lexeme}")
    }

    fun get(name: Token): Any? {
        if (name.type != TokenType.IDENTIFIER) {
            throw wrongTokenError(name)
        }

        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme)
        }

        if (enclosing != null) {
            return enclosing.get(name)
        }

        throw RuntimeError("Undefined variable '${name.lexeme}'")
    }

    private fun wrongTokenError(name: Token): RuntimeError {
        return RuntimeError("Bad interpreter, attempted to retrieve variable with ${name.type}")
    }
}
