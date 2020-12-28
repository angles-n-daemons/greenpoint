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

        if (values.containsKey(name.lexeme)) {
            throw RuntimeError("Variable '${name.lexeme}' already defined")
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

    fun getAt(dist: Int, name: Token): Any? {
        if (name.type != TokenType.IDENTIFIER) {
            throw wrongTokenError(name)
        }

        val env = ancestor(dist)
        if (env == null) {
            throw RuntimeError("fatal: environment lookup failure on ancestor")
        }

        if (env.values.containsKey(name.lexeme)) {
            return env.values.get(name.lexeme)
        }

        throw RuntimeError("Undefined variable '${name.lexeme}' at dist $dist")
    }

    fun assignAt(dist: Int, name: Token, value: Any?) {
        if (name.type != TokenType.IDENTIFIER) {
            throw wrongTokenError(name)
        }

        val env = ancestor(dist)
        if (env == null) {
            throw RuntimeError("fatal: environment lookup failure on ancestor")
        }

        if (env.values.containsKey(name.lexeme)) {
            env.values.put(name.lexeme, value)
            return
        }

        throw RuntimeError("Undefined variable '${name.lexeme}' at dist $dist")
    }

    fun ancestor(dist: Int): Environment? {
        var env: Environment? = this
        for (i in 0..dist-1) {
            if (env == null) {
                break
            }
            env = env.enclosing
        }
        return env
    }

    private fun wrongTokenError(name: Token): RuntimeError {
        return RuntimeError("Bad interpreter, attempted to retrieve variable with ${name.type}")
    }

    private fun printEnvironmentStack(depth: Int=0) {
        println("Env $depth")
        println(values)
        println("\n")

        if (enclosing != null) {
            enclosing.printEnvironmentStack(depth+1)
        }
    }
}
