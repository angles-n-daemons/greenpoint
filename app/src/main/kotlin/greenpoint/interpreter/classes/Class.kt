package greenpoint.interpreter.classes

import greenpoint.scanner.Token
import greenpoint.interpreter.Interpreter
import greenpoint.interpreter.RuntimeError
import greenpoint.interpreter.function.Callable

class Class(
    val name: String,
): Callable {
    override fun arity(): Int {
        return 0
    }

    override fun call(
        interpreter: Interpreter,
        args: List<Any?>,
    ): Any? {
        return Instance(this)
    }

    override fun toString(): String {
        return name
    }
}

class Instance(
    val klass: Class,
) {
    private val fields = mutableMapOf<String, Any?>()

    fun get(name: Token): Any? {
        if (fields.contains(name.lexeme)) {
            return fields.get(name.lexeme)
        }

        throw RuntimeError("Undefined property ${name.lexeme}.")
    }

    fun set(name: Token, obj: Any?) {
        fields.put(name.lexeme, obj)
    }

    override fun toString(): String {
        return "${klass.name} instance"
    }
}
